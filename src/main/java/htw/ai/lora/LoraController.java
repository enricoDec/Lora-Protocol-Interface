package htw.ai.lora;

import htw.ai.ChatsController;
import htw.ai.lora.config.Config;
import javafx.beans.property.*;

import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author : Enrico Gamil Toros de Chadarevian
 * Project name : Lora-Protocol
 * @version : 1.0
 * @since : 15-05-2021
 **/
public class LoraController implements Runnable {
    // JavaFX
    private LoraState loraState;
    private SimpleIntegerProperty state = new SimpleIntegerProperty();
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
    private BooleanProperty running = new SimpleBooleanProperty(false);

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
            loraUART_thread = new Thread(loraUART, "loraUART_thread");
            loraUART_thread.start();
            boolean portOpened = loraUART.start();

            if (!portOpened) {
                stop();
            }
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
            ChatsController.writeToLog("Lora Controller Thread ended");
        }
    }

    /**
     * Start terminal state machine
     */
    @Override
    public void run() {
        // Start UART Thread
        start();

        Lora replyCode;
        while (running.get()) {
            state.set(loraState.ordinal());
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
                    if (!dataToSend.startsWith(Lora.AT.CODE)) {
                        // Get number of bytes to send
                        int bytesToSend = dataToSend.strip().length();
                        try {
                            // Send AT+SEND=bytesToSend
                            writeQueue.put(Lora.AT_SEND.CODE + bytesToSend);
                            // Check reply code
                            replyCode = Lora.valueOfCode(replyQueue.take());
                            checkReplyCode(Lora.AT_SEND, replyCode, Lora.REPLY_OK);
                            writeQueue.put(dataToSend.strip());
                            loraState = LoraState.SENDING;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else if (dataToSend.startsWith(Lora.AT.CODE)) {
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
                        replyCode = Lora.valueOfCode(replyQueue.take());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if (replyCode.CODE.startsWith(Lora.ERR_GENERAL.CODE)) {
                        ChatsController.writeToLog("Error " + replyCode.CODE);
                    } else {
                        ChatsController.writeToLog(replyCode.CODE);
                    }
                    loraState = LoraState.USER_INPUT;
                    break;
                case SENDING:
                    // wait for reply from serial comm
                    boolean equals = false;
                    try {
                        replyCode = Lora.valueOfCode(replyQueue.take());
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
                        replyCode = Lora.valueOfCode(replyQueue.take());
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
            // Set Destination Address
            atDestAddr();
            // Send something
            String hello = "Client " + config.getAddress() + " started.";
            writeQueue.put(Lora.AT_SEND.CODE + hello.length());
            replyQueue.take();
            writeQueue.put(hello);
            replyQueue.take();
            replyQueue.take();
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
        String reset = Lora.AT_RST.CODE;
        ChatsController.writeToLog(reset);
        writeQueue.put(reset);
        // Wait for reply with timeout
        int timeoutInMillis = 5000;
        String replyCode = replyQueue.poll(timeoutInMillis, TimeUnit.MILLISECONDS);
        if (replyCode == null) {
            sendRandomData(250);
            // AT,SENDING
            replyQueue.take();
            // AT,SENDED
            replyQueue.take();
            atRST();
        }

        if (checkReplyCode(Lora.AT_RST, Lora.valueOfCode(replyCode), Lora.REPLY_OK)) {
            // Himalaya Vendor etc..
            unknownQueue.take();
        } else {
            // TODO: Handle error
        }
    }

    /**
     * Set the lora Module address to the address specified by the user in the config
     *
     * @throws InterruptedException Thrown when a thread is waiting, sleeping, or otherwise occupied, and the thread is interrupted.
     */
    private void atSetAddr() throws InterruptedException {
        String setAddr = Lora.AT_ADDR_SET.CODE + config.getAddress();
        ChatsController.writeToLog(setAddr);
        writeQueue.put(setAddr);
        checkReplyCode(Lora.AT_ADDR_SET, Lora.valueOfCode(replyQueue.take()), Lora.REPLY_OK);
    }

    /**
     * Set the lora module config to the values declared in the config file
     *
     * @throws InterruptedException Thrown when a thread is waiting, sleeping, or otherwise occupied, and the thread is interrupted.
     */
    private void atConfig() throws InterruptedException {
        String setCfg = Lora.AT_CFG.CODE + config.getConfiguration();
        ChatsController.writeToLog(setCfg);
        writeQueue.put(setCfg);
        checkReplyCode(Lora.AT_CFG, Lora.valueOfCode(replyQueue.take()), Lora.REPLY_OK);
    }

    /**
     * Get the Lora Module Version
     *
     * @throws InterruptedException Thrown when a thread is waiting, sleeping, or otherwise occupied, and the thread is interrupted.
     */
    private void atGetVersion() throws InterruptedException {
        String getVersion = Lora.AT_VER.CODE;
        ChatsController.writeToLog(getVersion);
        writeQueue.put(getVersion);
        replyQueue.take();
    }

    /**
     * Set the destination address to FFFF
     *
     * @throws InterruptedException Thrown when a thread is waiting, sleeping, or otherwise occupied, and the thread is interrupted.
     */
    private void atDestAddr() throws InterruptedException {
        String setDestinationAddr = Lora.AT_DEST.CODE + "FFFF";
        ChatsController.writeToLog(setDestinationAddr);
        writeQueue.put(setDestinationAddr);
        replyQueue.take();
    }

    /**
     * Send some random data
     *
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
            equals = false;
        }
        return equals;
    }

    public boolean isRunning() {
        return running.get();
    }

    public BooleanProperty runningProperty() {
        return running;
    }

    public SimpleIntegerProperty stateProperty() {
        return state;
    }

}
