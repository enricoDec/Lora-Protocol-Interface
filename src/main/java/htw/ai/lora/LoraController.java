package htw.ai.lora;

import com.fazecast.jSerialComm.SerialPortInvalidPortException;
import htw.ai.application.controller.ChatsController;
import htw.ai.application.model.Chats;
import htw.ai.application.model.ChatsDiscovery;
import htw.ai.lora.config.Config;
import htw.ai.protocoll.message.Message;
import javafx.beans.property.SimpleIntegerProperty;
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
    // Current state of the Statemachine
    private LoraState loraState;
    // State Property used by view
    private SimpleIntegerProperty state = new SimpleIntegerProperty();

    Thread loraUART_thread;
    private LoraUART loraUART;

    private final Config config;

    // Queue containing user input data
    private BlockingQueue<Message> messagesQueue;
    // Queue containing commands to write
    private BlockingQueue<String> writeQueue;
    private BlockingQueue<byte[]> payloadQueue;
    // Queue containing reply data from lora module
    private BlockingQueue<String> replyQueue;
    // Queue containing any other data (Not AT or LR)
    private BlockingQueue<String> unknownQueue;
    private AtomicBoolean isRunning = new AtomicBoolean(false);

    public LoraController(Config config, ChatsDiscovery chatsDiscovery, BlockingQueue<Message> messagesQueue) throws SerialPortInvalidPortException {
        this.config = config;
        this.messagesQueue = messagesQueue;
        this.loraState = LoraState.START;
        this.loraUART = new LoraUART(config, chatsDiscovery);
        this.writeQueue = loraUART.getCommandQueue();
        this.payloadQueue = loraUART.getMessageQueue();
        this.replyQueue = loraUART.getReplyQueue();
        this.unknownQueue = loraUART.getUnknownQueue();
    }

    /**
     * Start Thread
     */
    public boolean initialize() {
        if (isRunning.get())
            ChatsController.writeToLog("Controller already running!");
        else {
            isRunning.set(true);
            loraUART_thread = new Thread(loraUART, "loraUART_thread");
            boolean portOpened = loraUART.initialize();
            loraUART_thread.start();
            return portOpened;
        }
        return true;
    }

    /**
     * Stop Thread
     */
    public void stop() {
        if (!isRunning.get())
            ChatsController.writeToLog("Controller already stopped!");
        else {
            loraUART.stop();
            try {
                loraUART_thread.join();
                ChatsController.writeToLog("UART Thread ended");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            isRunning.set(false);
        }
    }

    /**
     * Start terminal state machine
     */
    @Override
    public void run() {
        Lora replyCode;
        while (isRunning.get()) {
            state.set(loraState.ordinal());
            switch (loraState) {
                // Start State Menu
                case START:
                    setup();
                    loraState = LoraState.USER_INPUT;
                    break;
                case USER_INPUT:
                    byte[] messageBytes = null;
                    Message message = null;
                    try {
                        // Wait for user Input
                        message = messagesQueue.poll(100, TimeUnit.MILLISECONDS);
                        if (message == null)
                            break;
                        messageBytes = message.toMessage();
                        ChatsController.writeToLog(new String(message.toMessage(), StandardCharsets.US_ASCII), Color.YELLOW);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    // If no AT command -> AT+SEND
                    if (messageBytes != null) {
                        // Get number of bytes to send
                        int bytesToSend = messageBytes.length;
                        try {
                            // Send AT+SEND=bytesToSend
                            if (!message.getDestination().isEmpty())
                                setAtDestAddr(message.getDestination());
                            writeQueue.put(Lora.AT_SEND.CODE + bytesToSend);
                            // Check reply code
                            replyCode = Lora.valueOfCode(replyQueue.take());
                            checkReplyCode(replyCode, Lora.REPLY_OK);
                            payloadQueue.put(messageBytes);
                            loraState = LoraState.SENDING;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        ChatsController.writeToLog("Message ignored, bad Type or malformed.");
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
                    }
                    loraState = LoraState.USER_INPUT;
                    break;
                case SENDING:
                    // wait for reply from serial comm
                    boolean equals = false;
                    try {
                        replyCode = Lora.valueOfCode(replyQueue.take());
                        equals = checkReplyCode(replyCode, Lora.REPLY_SENDING);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if (equals)
                        loraState = LoraState.SENDED;
                    else {
                        ChatsController.writeToLog("Unexpected reply: AT,SENDING expected.");
                        loraState = LoraState.USER_INPUT;
                    }
                    break;
                case SENDED:
                    // wait for reply from serial comm
                    boolean equal = false;
                    try {
                        replyCode = Lora.valueOfCode(replyQueue.take());
                        equal = checkReplyCode(replyCode, Lora.REPLY_SENDED);
                        loraState = LoraState.USER_INPUT;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if (!equal)
                        ChatsController.writeToLog("Unexpected reply: AT,SENDED expected.");
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
            setAtDestAddr("FFFF");
            // Set Sending mode
            setAtRX();
            System.out.println("Setup done");
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

        if (checkReplyCode(Lora.valueOfCode(replyCode), Lora.REPLY_OK)) {
            // Himalaya Vendor etc..
            unknownQueue.take();
        } else {
            ChatsController.writeToLog("Unexpected reply: AT,OK expected.");
        }
    }

    /**
     * Set the lora Module address to the address specified by the user in the config
     *
     * @throws InterruptedException Thrown when a thread is waiting, sleeping, or otherwise occupied, and the thread is interrupted.
     */
    private void atSetAddr() throws InterruptedException {
        String setAddr = Lora.AT_ADDR_SET.CODE + config.getAddress();
        writeQueue.put(setAddr);
        checkReplyCode(Lora.valueOfCode(replyQueue.take()), Lora.REPLY_OK);
    }

    /**
     * Set the lora module config to the values declared in the config file
     *
     * @throws InterruptedException Thrown when a thread is waiting, sleeping, or otherwise occupied, and the thread is interrupted.
     */
    private void atConfig() throws InterruptedException {
        String setCfg = Lora.AT_CFG.CODE + config.getConfiguration();
        writeQueue.put(setCfg);
        checkReplyCode(Lora.valueOfCode(replyQueue.take()), Lora.REPLY_OK);
    }

    /**
     * Get the Lora Module Version
     *
     * @throws InterruptedException Thrown when a thread is waiting, sleeping, or otherwise occupied, and the thread is interrupted.
     */
    private void atGetVersion() throws InterruptedException {
        String getVersion = Lora.AT_VER.CODE;
        writeQueue.put(getVersion);
        replyQueue.take();
    }

    /**
     * Set the destination address to FFFF
     *
     * @throws InterruptedException Thrown when a thread is waiting, sleeping, or otherwise occupied, and the thread is interrupted.
     */
    private synchronized void setAtDestAddr(String destination) throws InterruptedException {
        String setDestinationAddr = Lora.AT_DEST.CODE + destination;
        writeQueue.put(setDestinationAddr);
        replyQueue.take();
    }

    private void setAtRX() throws InterruptedException {
        String atRX = Lora.AT_RX.CODE;
        writeQueue.put(atRX);
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
        String generatedString = new String(array, StandardCharsets.US_ASCII);
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
     * @param actualReplyCode   The reply code
     * @param expectedReplyCode The expected reply code
     * @return true if the reply code is equal, else false
     */
    private boolean checkReplyCode(Lora actualReplyCode, Lora expectedReplyCode) {
        boolean equals = true;
        if (!actualReplyCode.equals(expectedReplyCode)) {
            equals = false;
        }
        return equals;
    }

    public BlockingQueue<byte[]> getLrQueue() {
        return loraUART.getLrQueue();
    }
}
