package htw.ai.protocoll;

import htw.ai.application.controller.ChatsController;
import htw.ai.application.model.ClientMessage;
import htw.ai.application.model.LoraDiscovery;
import htw.ai.lora.LoraController;
import htw.ai.lora.config.Config;
import htw.ai.protocoll.message.Message;
import htw.ai.protocoll.message.RREQ;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author : Enrico Gamil Toros de Chadarevian
 * Project name : LoraProtocolInterface
 * @version : 1.0
 * @since : 09-06-2021
 **/
public class AodvController implements Runnable {
    private int sequenceNumber;
    private final int MAX_TRIES = 5;
    private final int DELAY_IN_SECONDS = 5;
    private final int ROUTE_LIFETIME_IN_SECONDS = 180;

    private BlockingQueue<String> atQueue;
    private BlockingQueue<ClientMessage> lrQueue;
    private BlockingQueue<String> messages = new ArrayBlockingQueue<>(20);
    private AtomicBoolean isRunning = new AtomicBoolean(false);
    private LoraController loraController;
    private Config config;
    private LoraDiscovery loraDiscovery;
    Thread lora_thread;

    public AodvController(BlockingQueue<String> atQueue, Config config, LoraDiscovery loraDiscovery) {
        this.atQueue = atQueue;
        this.config = config;
        this.loraDiscovery = loraDiscovery;
    }

    @Override
    public void run() {
        while (isRunning.get()) {
            if (atQueue.peek() != null) {
                // If client want to send message start process
                System.out.println("User wants to send new Message!");
            } else if (lrQueue.peek() != null) {
                // If message received to smth
                System.out.println("New Message received!");
            }
        }
    }

    public boolean initialize() {
        if (!isRunning.get()) {
            isRunning.set(true);
            loraController = new LoraController(config, loraDiscovery, messages);
            lora_thread = new Thread(loraController, "Lora_Thread");
            boolean portOpened = loraController.initialize();
            if (!portOpened) {
                stop();
                return false;
            }
            lora_thread.start();
            lrQueue = loraController.getLrQueue();
        }
        return true;
    }

    public void stop() {
        if (isRunning.get()) {
            isRunning.set(false);
            loraController.stop();
            try {
                lora_thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ChatsController.writeToLog("Lora Controller Thread ended");
        }
    }

    public AtomicBoolean getIsRunning() {
        return isRunning;
    }

    private void routeDiscovery(int id) {
        sequenceNumber++;

    }


    private void rreqResponse(RREQ rreq) {
        // Reply to RREQ if I'm destination
        // TODO: ????
        sequenceNumber = Integer.MAX_VALUE;
        rreq.setDestinationSequenceNumber(Byte.MAX_VALUE);


    }
}
