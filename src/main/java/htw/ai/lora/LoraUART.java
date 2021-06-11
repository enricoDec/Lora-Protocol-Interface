package htw.ai.lora;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortInvalidPortException;
import htw.ai.application.controller.ChatsController;
import htw.ai.application.model.ClientMessage;
import htw.ai.application.model.LoraDiscovery;
import htw.ai.lora.config.Config;
import javafx.scene.paint.Color;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
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
    private final LoraDiscovery loraDiscovery;
    private final SerialPort comPort;
    // message Queue contains payload (after AT+SEND=x)
    private BlockingQueue<byte[]> messageQueue = new ArrayBlockingQueue<>(20);
    // writeQueue contains AT Commands
    private BlockingQueue<String> commandQueue = new ArrayBlockingQueue<>(20);
    private BlockingQueue<String> replyQueue = new ArrayBlockingQueue<>(20);
    private BlockingQueue<String> unknownQueue = new ArrayBlockingQueue<>(20);
    private BlockingQueue<ClientMessage> lrQueue = new ArrayBlockingQueue<>(20);

    LoraUART(Config config, LoraDiscovery loraDiscovery) throws SerialPortInvalidPortException {
        comPort = SerialPort.getCommPort(config.getPort());
        comPort.setBaudRate(config.getBaudRate());
        comPort.setParity(config.getParity());
        comPort.setFlowControl(config.getFlowControl());
        comPort.setNumStopBits(config.getNumberOfStopBits());
        comPort.setNumDataBits(config.getNumberOfDataBits());

        this.config = config;
        this.loraDiscovery = loraDiscovery;
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
            running.set(true);
            comPort.openPort();
            if (!comPort.openPort()) {
                ChatsController.writeToLog("Could not open port " + config.getPort() + " !", Color.DARKRED);
                ChatsController.writeToLog("Wrong port or blocked by other process.", Color.DARKRED);
                return false;
            }
        }
        return true;
    }

    /**
     * Stop the thread
     */
    void stop() {
        if (!running.get())
            ChatsController.writeToLog("LoraUART already stopped!");
        else {
            running.set(false);
            comPort.closePort();
        }
    }

    @Override
    public void run() {
        // Important only one thread per port should be running at any time
        // Thread constantly checks if new data is available at Serial Port
        // Only if new data needs to be written to Serial Port it will switch to write
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
     * Read data to Serial Port
     */
    private synchronized void read() {
        String data = "";
        byte[] byteData;
        // Not sure about timeout
        comPort.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0, 0);
        // Check if any bytes are available to be read else close port and check if new data available to be written
        if (comPort.bytesAvailable() > 0) {
            // Keep reading until EOF is reached
            while (!data.contains(Lora.EOF.CODE)) {
                byteData = new byte[comPort.bytesAvailable()];
                comPort.readBytes(byteData, byteData.length);
                data = data.concat(new String(byteData, StandardCharsets.US_ASCII));
            }
            // Split reply codes from incoming messages
            // Reply codes
            // Remove EOF
            data = data.substring(0, data.length() - 2);
            if (data.startsWith(Lora.AT.CODE)) {
                try {
                    replyQueue.put(data);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            // Incoming messages
            else if (data.startsWith(Lora.LR.CODE)) {
                ChatsController.writeToLog(data, Color.CYAN);
                ClientMessage message = new ClientMessage(data);
                loraDiscovery.newClient(message);
                try {
                    lrQueue.put(message);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } // Unknown messages
            else {
                ChatsController.writeToLog("Unknown data read: " + data, Color.DARKRED);
                try {
                    unknownQueue.put(data);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Send data to serial Port
     *
     * @param data Data to be send. <b>Has to be UTF-8 encoded without new line or carriage return.</b>
     */
    private synchronized void write(String data) {
        byte[] dataWithEOF = (data + Lora.EOF.CODE).getBytes(StandardCharsets.UTF_8);
        comPort.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0, 0);
        comPort.writeBytes(dataWithEOF, dataWithEOF.length);

        ChatsController.writeToLog(data, Color.YELLOW);
    }

    /**
     * Send data to serial Port
     *
     * @param data Data to be send. <b>Has to be UTF-8 encoded without new line or carriage return.</b>
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
     * Get the the write queue
     * The write queue contains all the data to be written to the lora
     * To write data get the reference of the write queue and call put()
     *
     * @return reference to the write queue
     */
    BlockingQueue<String> getCommandQueue() {
        return commandQueue;
    }

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

    BlockingQueue<ClientMessage> getLrQueue() {
        return lrQueue;
    }
}
