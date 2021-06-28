package htw.ai.lora;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortInvalidPortException;
import htw.ai.application.model.ChatsDiscovery;
import htw.ai.lora.config.Config;
import javafx.scene.paint.Color;

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
    private final SerialPort comPort;
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
    // Logger
    private Logger logger = Logger.getInstance();

    LoraUART(Config config, ChatsDiscovery chatsDiscovery) throws SerialPortInvalidPortException {
        comPort = SerialPort.getCommPort(config.getPort());
        comPort.setBaudRate(config.getBaudRate());
        comPort.setParity(config.getParity());
        comPort.setFlowControl(config.getFlowControl());
        comPort.setNumStopBits(config.getNumberOfStopBits());
        comPort.setNumDataBits(config.getNumberOfDataBits());

        this.config = config;
        this.chatsDiscovery = chatsDiscovery;
    }

    /**
     * Start UART, returns false if port could not be opened
     *
     * @return false if port not opened, else true
     */
    boolean initialize() {
        if (running.get())
            logger.addToLog(new Log(Color.DARKRED, "LoraUART already running!"));
        else {
            running.set(true);
            comPort.openPort();
            if (!comPort.openPort()) {
                logger.addToLog(new Log(Color.DARKRED, "Could not open port " + config.getPort() + " !"));
                logger.addToLog(new Log(Color.DARKRED, "Wrong port or blocked by other process."));
                return false;
            }
        }
        return true;
    }

    /**
     * Stop the thread if not already running
     */
    void stop() {
        if (!running.get())
            logger.addToLog(new Log(Color.DARKRED, "LoraUART already stopped!"));
        else {
            running.set(false);
            comPort.closePort();
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
        String data = "";
        byte[] byteData = null;
        // Not sure about timeout
        comPort.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0, 0);
        // Check if any bytes are available to be read else close port and check if new data available to be written
        if (comPort.bytesAvailable() > 0) {
            // Keep reading until EOF is reached
            // TODO: 21.06.2021 Fix bug EOF could be in the middle of the message
            while (!data.contains(Lora.EOF.CODE)) {
                byteData = new byte[comPort.bytesAvailable()];
                comPort.readBytes(byteData, byteData.length);
                data = data.concat(new String(byteData, StandardCharsets.US_ASCII));
            }
            // Split messages to appropriate queue

            // Reply codes
            // Remove EOF
            if (data.length() < 4 || byteData.length < 4)
                return;
            data = data.substring(0, data.length() - 2);

            byte[] byteDataNoEOF = new byte[byteData.length - 2];
            System.arraycopy(byteData, 0, byteDataNoEOF, 0, byteData.length - 2);

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
                //ChatsController.writeToLog("Unknown data read: " + data, Color.DARKRED);
                try {
                    unknownQueue.put(data);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
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
        comPort.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0, 0);
        comPort.writeBytes(dataWithEOF, dataWithEOF.length);

        //ChatsController.writeToLog(data, Color.YELLOW);
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
        comPort.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0, 0);
        comPort.writeBytes(buffer, buffer.length);
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
