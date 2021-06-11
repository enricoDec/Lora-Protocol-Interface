package htw.ai.protocoll;

import htw.ai.application.controller.ChatsController;
import htw.ai.application.model.ClientMessage;
import htw.ai.application.model.LoraDiscovery;
import htw.ai.application.model.UserMessage;
import htw.ai.lora.Lora;
import htw.ai.lora.LoraController;
import htw.ai.lora.config.Config;
import htw.ai.protocoll.message.Message;
import htw.ai.protocoll.message.RREQ;
import htw.ai.protocoll.message.SEND_TEXT_REQUEST;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
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

    private HashMap<Integer, Route> routingTable = new HashMap<>();
    private BlockingQueue<UserMessage> atQueue;
    private BlockingQueue<ClientMessage> lrQueue;
    private BlockingQueue<Message> messages = new ArrayBlockingQueue<>(20);
    private AtomicBoolean isRunning = new AtomicBoolean(false);
    private LoraController loraController;
    private Config config;
    private LoraDiscovery loraDiscovery;
    Thread lora_thread;

    public AodvController(BlockingQueue<UserMessage> atQueue, Config config, LoraDiscovery loraDiscovery) {
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
                UserMessage userMessage = null;
                try {
                    userMessage = atQueue.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // If Valid route for destination already present send SEND_TEXT_REQUEST
                if (routingTable.containsKey(userMessage.getDestinationAddress())) {
                    SEND_TEXT_REQUEST send_text_request = new SEND_TEXT_REQUEST((byte) config.getAddress(), (byte) userMessage.getDestinationAddress(), (byte) 0, userMessage.getData());
                    try {
                        UserMessage finalUserMessage = userMessage;
                        messages.put(new Message((byte) 0) {
                            @Override
                            public byte[] toMessage() {
                                return null;
                            }
                            @Override
                            public String toString() {
                                return Lora.AT_DEST.CODE + finalUserMessage.getDestinationAddress();
                            }
                        });

                        messages.put(send_text_request);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
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
