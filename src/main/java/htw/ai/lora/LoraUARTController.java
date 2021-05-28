package htw.ai.lora;

import com.fazecast.jSerialComm.SerialPort;
import htw.ai.ChatsController;
import htw.ai.lora.config.Config;
import javafx.scene.paint.Color;

import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


/**
 * @author : Enrico Gamil Toros de Chadarevian
 * Project name : Lora-Protocol
 * @version : 1.0
 * @since : 14-05-2021
 **/
public class LoraUARTController implements Runnable {
    private boolean stop = false;
    private final Config config;
    private final LoraDiscovery loraDiscovery;
    private final SerialPort comPort;
    private final LinkedList<Message> messages = new LinkedList<>();
    private final ChatsController chatsController;
    private BlockingQueue<String> writeQueue = new ArrayBlockingQueue<>(20);
    private BlockingQueue<String> replyQueue = new ArrayBlockingQueue<>(20);
    private BlockingQueue<String> unknownQueue = new ArrayBlockingQueue<>(20);


    public LoraUARTController(Config config, ChatsController chatsController, LoraDiscovery loraDiscovery) {
        comPort = SerialPort.getCommPort(config.getPort());
        comPort.setBaudRate(config.getBaudRate());
        comPort.setParity(config.getParity());
        comPort.setFlowControl(config.getFlowControl());
        comPort.setNumStopBits(config.getNumberOfStopBits());
        comPort.setNumDataBits(config.getNumberOfDataBits());

        this.config = config;
        this.chatsController = chatsController;
        this.loraDiscovery = loraDiscovery;
    }

    /**
     * Start Lora UART thread
     */
    public void start() {
        Thread t1 = new Thread(this, "LoraAPI_Thread");
        t1.start();
    }

    @Override
    public void run() {
        // Important only one thread should be running at any time

        // Thread constantly checks if new data is available at Serial Port
        // Only if new data needs to be written to Serial Port it will switch to write
        if (!comPort.openPort()) {
            chatsController.writeToLog("Could not open port " + config.getPort() + " !", Color.DARKRED);
            chatsController.writeToLog("Wrong port or blocked by other process.", Color.DARKRED);
            System.exit(-1);
        }

        while (!stop) {
            // If no data to be written continue to poll Serial Port
            if (writeQueue.isEmpty()) {
                try {
                    read();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                // If new data to write switch to writing
                String dataToWrite = "";
                try {
                    // Get data and convert to byte[]
                    dataToWrite = this.writeQueue.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                write(dataToWrite);
            }
        }
        System.out.println("UART Thread dead.");
    }

    /**
     * Write data to Serial Port
     *
     * @throws InterruptedException Thrown when a thread is waiting, sleeping, or otherwise occupied, and the thread is interrupted, either before or during the activity.
     */
    private synchronized void read() throws InterruptedException {
        String data = "";
        byte[] byteData;
        // Not sure about timeout
        comPort.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0, 0);
        // Check if any bytes are available to be read else close port and check if new data available to be written
        if (comPort.bytesAvailable() > 0) {
            // Keep reading until EOF is reached
            while (!data.contains(Lora.EOF.getCODE())) {
                byteData = new byte[comPort.bytesAvailable()];
                comPort.readBytes(byteData, byteData.length);
                data = data.concat(new String(byteData, StandardCharsets.US_ASCII));
            }
            // Split reply codes from incoming messages
            // Reply codes
            if (data.startsWith(Lora.AT.getCODE())) {
                chatsController.writeToLog(data, Color.YELLOW);
                replyQueue.put(data);
            }
            // Incoming messages
            else if (data.startsWith(Lora.LR.getCODE())) {
                chatsController.writeToLog("\r 📲 " + data, Color.CYAN);
                Message message = new Message(data);
                messages.add(message);
                loraDiscovery.addClientAddress(message.getSourceAddress());
            } // Unknown messages
            else {
                chatsController.writeToLog("Unknown data read: " + data, Color.DARKRED);
                unknownQueue.put(data);
            }
        }
    }

    /**
     * Send data to serial Port
     *
     * @param data Data to be send. <b>Has to be UTF-8 encoded without new line or carriage return.</b>
     */
    private synchronized void write(String data) {
        byte[] dataWithEOF = (data + Lora.EOF.getCODE()).getBytes(StandardCharsets.UTF_8);

        // Not really sure about timeout
        comPort.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0, 0);
        comPort.writeBytes(dataWithEOF, dataWithEOF.length);
    }

    /**
     * Stop the thread
     *
     * @param stop true to stop the thread
     */
    public void setStop(boolean stop) {
        this.stop = stop;
    }

    /**
     * Get the the write queue
     * The write queue contains all the data to be written to the lora
     * To write data get the reference of the write queue and call put()
     *
     * @return reference to the write queue
     */
    public BlockingQueue<String> getWriteQueue() {
        return writeQueue;
    }

    /**
     * Get the reply queue
     * The reply queue contains all the data received from the lora that starts with "AT"
     * To read data get the reference of the reply queue and call take()
     *
     * @return reference to the reply queue
     */
    public BlockingQueue<String> getReplyQueue() {
        return replyQueue;
    }

    /**
     * Get the received queue
     * The message list contains all the data received from the lora that starts with "LR"
     *
     * @return reference to the received queue
     */
    public LinkedList<Message> getMessages() {
        return messages;
    }

    /**
     * Get the unknown queue
     * The unknown queue contains all the data received from the lora that does not start with "AT" OR "LR"
     * To read data get the reference of the unknown queue and call take()
     *
     * @return reference to the unknown queue
     */
    public BlockingQueue<String> getUnknownQueue() {
        return unknownQueue;
    }


    public SerialPort getComPort() {
        return comPort;
    }

    public Config getConfig() {
        return config;
    }

    public ChatsController getChatsController() {
        return chatsController;
    }

    public LoraDiscovery getLoraDiscovery() {
        return loraDiscovery;
    }
}
