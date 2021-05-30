package htw.ai.lora;

import htw.ai.ChatsController;
import htw.ai.lora.config.Config;
import javafx.scene.paint.Color;


import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author : Enrico Gamil Toros de Chadarevian
 * Project name : Lora-Protocol
 * @version : 1.0
 * @since : 15-05-2021
 **/
public class LoraController implements Runnable {
    private LoraState loraState;
    private LoraDiscovery loraDiscovery;
    Thread loraUART_thread;
    private LoraUART loraUART;
    private final Config config;
    // Queue containing user input data
    private BlockingQueue<String> userInputQueue;
    // Queue containing data to write
    private BlockingQueue<String> writeQueue;
    // Queue containing reply data from lora module
    private BlockingQueue<String> replyQueue;
    // Queue containing any other data (Not AT or LR)
    private BlockingQueue<String> unknownQueue;
    private int timeoutInMillis = 2000;
    private AtomicBoolean running = new AtomicBoolean(false);

    public LoraController(Config config, BlockingQueue<String> userInputQueue, LoraDiscovery loraDiscovery) {
        this.config = config;
        this.userInputQueue = userInputQueue;
        this.loraDiscovery = loraDiscovery;
        this.loraState = LoraState.START;

        this.loraUART = new LoraUART(config, loraDiscovery);
        this.writeQueue = loraUART.getWriteQueue();
        this.replyQueue = loraUART.getReplyQueue();
        this.unknownQueue = loraUART.getUnknownQueue();
    }

    /**
     * Start Thread
     */
    public void start() {
        if (running.get())
            ChatsController.writeToLog("Controller already running!");
        else {
            running.set(true);
        }
    }

    /**
     * Stop Thread
     */
    public void stop() {
        if (!running.get())
            ChatsController.writeToLog("Controller already stopped!");
        else {
            loraUART.stop();
            try {
                loraUART_thread.join();
                ChatsController.writeToLog("UART Thread ended");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            running.set(false);
        }
    }

    /**
     * Start terminal state machine
     */
    @Override
    public void run() {
        // Start UART Thread
        loraUART_thread = new Thread(loraUART, "loraUART_thread");
        loraUART_thread.start();
        start();

        Lora replyCode;

        while (running.get()) {
            System.out.println(loraState.toString());
            switch (loraState) {
                // Start State Menu
                case START:
                    setup();
                    loraState = LoraState.USER_INPUT;
                    break;
                case USER_INPUT:
                    String dataToSend = "";
                    try {
                        // Wait for user Input
                        dataToSend = userInputQueue.take();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    // If no AT command -> AT+SEND
                    if (!dataToSend.startsWith(Lora.AT.getCODE())) {
                        // Get number of bytes to send
                        int bytesToSend = dataToSend.strip().length();
                        try {
                            // Send AT+SEND=bytesToSend
                            writeQueue.put(Lora.AT_SEND.getCODE() + bytesToSend);
                            // Check reply code
                            replyCode = Lora.valueOf(replyQueue.take());
                            checkReplyCode(Lora.AT_SEND, replyCode, Lora.REPLY_OK);
                            loraState = LoraState.SENDING;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else if (dataToSend.startsWith(Lora.AT.getCODE())) {
                        try {
                            writeQueue.put(dataToSend);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        loraState = LoraState.WAIT_REPLY;
                    } else {
                        ChatsController.writeToLog(dataToSend + " not a valid input!");
                        loraState = LoraState.USER_INPUT;
                    }
                    break;
                case WAIT_REPLY:
                    // Wait for reply from serial comm
                    replyCode = Lora.UNKNOWN;
                    try {
                        replyCode = Lora.valueOf(replyQueue.take());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if (replyCode.getCODE().startsWith(Lora.ERR_GENERAL.getCODE())) {
                        ChatsController.writeToLog("Error " + replyCode.getCODE());
                    } else {
                        ChatsController.writeToLog(replyCode.getCODE());
                    }
                    loraState = LoraState.USER_INPUT;
                    break;
                case SENDING:
                    // wait for reply from serial comm
                    boolean equals = false;
                    try {
                        replyCode = Lora.valueOf(replyQueue.take());
                        equals = checkReplyCode(Lora.AT_SEND, replyCode, Lora.REPLY_SENDING);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if (equals)
                        loraState = LoraState.SENDED;
                    else {
                        // TODO: Handle error
                        loraState = LoraState.USER_INPUT;
                    }
                    break;
                case SENDED:
                    // wait for reply from serial comm
                    boolean equal = false;
                    try {
                        replyCode = Lora.valueOf(replyQueue.take());
                        equal = checkReplyCode(Lora.AT_SEND, replyCode, Lora.REPLY_SENDED);
                        loraState = LoraState.USER_INPUT;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if (!equal)
                        // TODO: Handle error
                        ChatsController.writeToLog("Error");
                    loraState = LoraState.USER_INPUT;
                    break;
            }
        }
        ChatsController.writeToLog("Lora Controller Thread ended");
    }

    /**
     * Reset Lora Module and configure
     * 1. Reset AT
     * 2. Set AT Addr
     * 3. Set AT Config
     * 4. Get AT Version
     */
    private void setup() {
        try {
            // Reset check reply AT, OK
            atRST();
            // Set Address check AT, OK
            atSetAddr();
            // Set Configuration
            atConfig();
            // Get Lora version
            atGetVersion();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reset AT. Reset the Lora module. If the lora module does not reply it will timeout and send 250 bytes of random data
     * to the module, since most likely the module is waiting for some data after an AT+SEND that has not been completed.
     * After this it will try again to send an AT Reset command and verify the reply code form the lora module.
     *
     * @throws InterruptedException Thrown when a thread is waiting, sleeping, or otherwise occupied, and the thread is interrupted.
     */
    private void atRST() throws InterruptedException {
        // Send AT+RST to Lora
        String reset = Lora.AT_RST.getCODE();
        ChatsController.writeToLog(reset);
        writeQueue.put(reset);
        // Wait for reply with timeout
        String replyCode = replyQueue.poll(timeoutInMillis, TimeUnit.MILLISECONDS);
        if (replyCode == null) {
            sendRandomData(250);
            atRST();
        }

        boolean reply = checkReplyCode(Lora.AT_RST, Lora.valueOf(replyCode), Lora.REPLY_OK);
        // Model we user does not follow protocol and replies with model and vendor after AT,OK
        if (reply)
            unknownQueue.take();
        else {
            replyQueue.take();
        }
    }

    /**
     * Set the lora Module address to the address specified by the user in the config
     *
     * @throws InterruptedException Thrown when a thread is waiting, sleeping, or otherwise occupied, and the thread is interrupted.
     */
    private void atSetAddr() throws InterruptedException {
        String setAddr = Lora.AT_ADDR_SET.getCODE() + config.getAddress();
        ChatsController.writeToLog(setAddr);
        writeQueue.put(setAddr);
        checkReplyCode(Lora.AT_ADDR_SET, Lora.valueOf(replyQueue.take()), Lora.REPLY_OK);
    }

    /**
     * Set the lora module config to the values declared in the config file
     *
     * @throws InterruptedException Thrown when a thread is waiting, sleeping, or otherwise occupied, and the thread is interrupted.
     */
    private void atConfig() throws InterruptedException {
        String setCfg = Lora.AT_CFG.getCODE() + config.getConfiguration();
        ChatsController.writeToLog(setCfg);
        writeQueue.put(setCfg);
        checkReplyCode(Lora.AT_CFG, Lora.valueOf(replyQueue.take()), Lora.REPLY_OK);
    }

    /**
     * Get the Lora Module Version
     *
     * @throws InterruptedException Thrown when a thread is waiting, sleeping, or otherwise occupied, and the thread is interrupted.
     */
    private void atGetVersion() throws InterruptedException {
        String getVersion = Lora.AT_VER.getCODE();
        ChatsController.writeToLog(getVersion);
        writeQueue.put(getVersion);
        replyQueue.take();
    }

    /**
     * @param numOfBytes number of bytes to send
     */
    private void sendRandomData(int numOfBytes) {
        ChatsController.writeToLog("Sending " + numOfBytes + " bytes of random data");
        byte[] array = new byte[numOfBytes];
        new Random().nextBytes(array);
        String generatedString = new String(array, StandardCharsets.UTF_8);
        try {
            writeQueue.put(generatedString);
            replyQueue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Check if reply code equals the expected reply code
     * If not log to console
     *
     * @param cmd               the executed lora command
     * @param actualReplyCode   The reply code
     * @param expectedReplyCode The expected reply code
     * @return true if the reply code is equal, else false
     */
    private boolean checkReplyCode(Lora cmd, Lora actualReplyCode, Lora expectedReplyCode) {
        boolean equals = true;
        if (!actualReplyCode.equals(expectedReplyCode)) {
            ChatsController.writeToLog("Unexpected return code after " + cmd.getCODE() + ": " + actualReplyCode, Color.DARKRED);
            equals = false;
        }
        return equals;
    }
}