package htw.ai.lora;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortInvalidPortException;
import htw.ai.application.Client;
import htw.ai.application.controller.ChatsController;
import htw.ai.application.model.ChatsDiscovery;
import htw.ai.lora.config.Config;
import javafx.scene.paint.Color;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * @author : Enrico Gamil Toros de Chadarevian
 * Project name : Lora-Protocol
 * @version : 1.0
 * @since : 14-05-2021
 **/
public class LoraUART implements Runnable {
    private AtomicBoolean running = new AtomicBoolean(false);
    private final Config config;
    private final ChatsDiscovery chatsDiscovery;
    // message Queue contains payload (e.g. after AT+SEND=x)
    private BlockingQueue<byte[]> messageQueue = new ArrayBlockingQueue<>(20);
    // writeQueue contains AT Commands (e.g. AT+CMD)
    private BlockingQueue<String> commandQueue = new ArrayBlockingQueue<>(20);
    // contains reply codes from lora (e.g. AT,OK)
    private BlockingQueue<String> replyQueue = new ArrayBlockingQueue<>(20);
    // contains everything else (e.g. MODULE:HIMO-01M(V0.4))
    private BlockingQueue<String> unknownQueue = new ArrayBlockingQueue<>(20);
    // contains received messages from other lora modules
    private BlockingQueue<byte[]> lrQueue = new ArrayBlockingQueue<>(20);
    private Client client;

    LoraUART(Config config, ChatsDiscovery chatsDiscovery) throws SerialPortInvalidPortException {
        this.config = config;
        this.chatsDiscovery = chatsDiscovery;
        try {
            this.client = new Client(new URI("ws://localhost:8001/11"), this);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    /**
     * Start UART, returns false if port could not be opened
     *
     * @return false if port not opened, else true
     */
    boolean initialize() {
        if (running.get())
            ChatsController.writeToLog("LoraUART already running!");
        else {
            client.connect();
        }
        return true;
    }

    /**
     * Stop the thread if not already running
     */
    void stop() {
        if (!running.get())
            ChatsController.writeToLog("LoraUART already stopped!");
        else {
            running.set(false);
            client.close();
        }
    }

    @Override
    public void run() {
        // Important only one thread per port should be running at any time
        // Thread constantly checks if new data is available at Serial Port
        // In theory port should not be blocked if not reading or writing at the moment (not tested)
        while (running.get()) {
            // If no data to be written continue to poll Serial Port
            if (!commandQueue.isEmpty()) {
                try {
                    write(commandQueue.take());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (!messageQueue.isEmpty()) {
                try {
                    write(messageQueue.take());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            read();
        }
    }

    /**
     * Read data from Serial Port
     */
    private synchronized void read() {

    }

    public void decode(String data) {
        byte[] byteDataNoEOF = data.getBytes(StandardCharsets.US_ASCII);

        if (data.startsWith(Lora.AT.CODE)) {
            try {
                replyQueue.put(data);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Incoming messages
        else if (data.startsWith(Lora.LR.CODE)) {
            try {
                lrQueue.put(byteDataNoEOF);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Unknown messages
        else {
            ChatsController.writeToLog("Unknown data read: " + data, Color.DARKRED);
            try {
                unknownQueue.put(data);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Write data to serial Port, will be encoded as ASCII
     *
     * @param data Data to be send. <b>New line and carriage return will be appended by this method</b>
     */
    private synchronized void write(String data) {
        byte[] dataWithEOF = (data + Lora.EOF.CODE).getBytes(StandardCharsets.US_ASCII);

        this.client.send(dataWithEOF);
        ChatsController.writeToLog(data, Color.YELLOW);
    }

    /**
     * Send data to serial Port.
     *
     * @param data Data to be send. <b>New line and carriage return will be appended by this method</b>
     */
    private synchronized void write(byte[] data) {
        byte[] eof = Lora.EOF.CODE.getBytes(StandardCharsets.US_ASCII);
        byte[] buffer = new byte[data.length + eof.length];
        System.arraycopy(data, 0, buffer, 0, data.length);
        System.arraycopy(eof, 0, buffer, data.length, eof.length);

        this.client.send(buffer);

        System.out.print("\033[1;36m");
        for (Byte b : data) {
            System.out.print(b + " ");
        }
        System.out.println();
        System.out.print("\033[0m");
    }

    /**
     * Get the the write queue. The command write queue should only contain AT commands
     *
     * @return reference to the write queue
     */
    BlockingQueue<String> getCommandQueue() {
        return commandQueue;
    }

    /**
     * Get the the write queue. The command write queue should only contain Messages to be send with AT+SEND=xx
     *
     * @return reference to the write queue
     */
    public BlockingQueue<byte[]> getMessageQueue() {
        return messageQueue;
    }

    /**
     * Get the reply queue
     * The reply queue contains all the data received from the lora that starts with "AT"
     * To read data get the reference of the reply queue and call take()
     *
     * @return reference to the reply queue
     */
    BlockingQueue<String> getReplyQueue() {
        return replyQueue;
    }

    /**
     * Get the unknown queue
     * The unknown queue contains all the data received from the lora that does not start with "AT" OR "LR"
     * To read data get the reference of the unknown queue and call take()
     *
     * @return reference to the unknown queue
     */
    BlockingQueue<String> getUnknownQueue() {
        return unknownQueue;
    }

    /**
     * Get the reply queue
     * The reply queue contains all the data received from the lora that starts with "LR"
     * To read data get the reference of the reply queue and call take()
     *
     * @return reference to the reply queue
     */
    BlockingQueue<byte[]> getLrQueue() {
        return lrQueue;
    }
}
