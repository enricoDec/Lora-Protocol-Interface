package htw.ai.protocoll;

import htw.ai.application.controller.ChatsController;
import htw.ai.application.model.ChatsDiscovery;
import htw.ai.application.model.ClientMessage;
import htw.ai.application.model.UserMessage;
import htw.ai.lora.Lora;
import htw.ai.lora.LoraController;
import htw.ai.lora.config.Config;
import htw.ai.protocoll.message.*;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
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
    private final int ROUTE_LIFETIME_IN_SECONDS = 180;
    private long sequenceNumber = 0;
    private byte rreqID = 0;

    private HashMap<Integer, Route> routingTable = new HashMap<>();
    private BlockingQueue<UserMessage> atQueue;
    private BlockingQueue<byte[]> lrQueue;
    private BlockingQueue<Message> messagesQueue = new ArrayBlockingQueue<>(20);
    private AtomicBoolean isRunning = new AtomicBoolean(false);
    private LoraController loraController;
    private Config config;
    private ChatsDiscovery chatsDiscovery;
    Thread lora_thread;
    private HashMap<Byte, Byte> rreqBuffer = new HashMap<>();
    private HashMap<Byte, RouteRequestHandler> routeRequestHandlerHashMap = new HashMap<>();
    private HashMap<Integer, LinkedList<UserMessage>> pendingMessages = new HashMap<>();

    public AodvController(BlockingQueue<UserMessage> atQueue, Config config, ChatsDiscovery chatsDiscovery) {
        this.atQueue = atQueue;
        this.config = config;
        this.chatsDiscovery = chatsDiscovery;
    }

    private void createRouteRequest(RREQ rreq) {
        incrementSeqNumber();
        rreqID++;
        setDestination("FFFF");
        rreqBuffer.put(rreq.getRreqID(), rreq.getDestinationAddress());
        RouteRequestHandler routeRequestHandler = new RouteRequestHandler(rreq, this);
        Thread thread = new Thread(routeRequestHandler);
        thread.start();
        routeRequestHandlerHashMap.put(rreq.getDestinationAddress(), routeRequestHandler);
    }

    private void handleRouteRequest(RREQ rreq) {
        if (rreq.getDestinationAddress() != (byte) config.getAddress()) {
            // Am Intermediate Node

        }
    }

    private void createRouteReply() {
        sequenceNumber = Integer.MAX_VALUE;
    }

    private void sendTextRequest(SEND_TEXT_REQUEST message, Route route) {
        setDestination(String.valueOf(route.getDestinationAddress()));
        try {
            messagesQueue.put(message);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void incrementSeqNumber() {
        if (sequenceNumber == 4294967295L)
            sequenceNumber = 0;
        else if (sequenceNumber == 2147483647L)
            sequenceNumber++;
        else
            sequenceNumber++;
    }


    private void route(UserMessage userMessage) {
        // If Valid route for destination already present send SEND_TEXT_REQUEST
        if (routingTable.containsKey(userMessage.getDestinationAddress())) {
            System.out.println("Valid route found in table, sending Text Req");
            SEND_TEXT_REQUEST send_text_request = new SEND_TEXT_REQUEST((byte) config.getAddress(), (byte) userMessage.getDestinationAddress(), (byte) 0, userMessage.getData());
            sendTextRequest(send_text_request, routingTable.get(userMessage.getDestinationAddress()));
            LinkedList<UserMessage> userMessages = pendingMessages.get(userMessage.getDestinationAddress());
            if (userMessages == null)
                pendingMessages.put(userMessage.getDestinationAddress(), new LinkedList<>(Collections.singletonList(userMessage)));
            else
                userMessages.add(userMessage);
        } else {
            // If no Valid route known RREQ
            System.out.println("No valid route found, sending RREQ");
            createRouteRequest(new RREQ((byte) 1, (byte) 0, rreqID, (byte) config.getAddress(), (byte) sequenceNumber, (byte) userMessage.getDestinationAddress(), (byte) 0));
        }
    }

    @Override
    public void run() {
        while (isRunning.get()) {
            if (atQueue.peek() != null) {
                // If client want to send message start process
                UserMessage userMessage = null;
                try {
                    userMessage = atQueue.take();
                    System.out.println("User want to send a Message, routing...");
                    route(userMessage);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else if (lrQueue.peek() != null) {
                // If message received do smth
                try {
                    byte[] data = lrQueue.take();
                    for (byte b : data) {
                        System.out.println(b);
                    }
                    Message message = decode(data);
                    if (message == null)
                        ChatsController.writeToLog("Bad Packet received");
                    else {
                        // do shit
                        switch (message.getTYPE()) {
                            case Type.RREP:
                                RREP rrep = (RREP) message;
                                if (rrep.getOriginAddress() == config.getAddress() && routeRequestHandlerHashMap.containsKey(rrep.getDestinationAddress())) {
                                    routeRequestHandlerHashMap.get(rrep.getDestinationAddress()).gotRREP();
                                    // RREP-ACK
                                    RREP_ACK rrepAck = new RREP_ACK();
                                    setDestination(String.valueOf(rrepAck.getPrevHop()));
                                    messagesQueue.put(rrepAck);
                                    // Route Found add to table and send
                                    routingTable.put((int) rrep.getDestinationAddress(), new Route(rrep.getDestinationAddress(), rrep.getDestinationSequenceNumber(), true, rrep.getHopCount(), rrep.getPrevHop(), ROUTE_LIFETIME_IN_SECONDS));
                                    LinkedList<UserMessage> userMessages = pendingMessages.get((int) rrep.getDestinationAddress());
                                    if (userMessages != null) {
                                        for (UserMessage userMessage : userMessages) {
                                            sendTextRequest(new SEND_TEXT_REQUEST((byte) config.getAddress(), (byte) userMessage.getDestinationAddress(), (byte) sequenceNumber, userMessage.getData()), routingTable.get(userMessage.getDestinationAddress()));
                                        }
                                    }
                                }
                                break;
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void setDestination(String destination) {
        try {
            messagesQueue.put(new Message(Type.CUSTOM) {
                @Override
                public byte[] toMessage() {
                    return null;
                }

                @Override
                public String toString() {
                    return Lora.AT_DEST.CODE + destination;
                }
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private Message decode(byte[] bytes) {
        // LR , ADDR , NUMB_BYTES ,  ......
        // 01 2 3456 7 89         10 11-x
        // First 12 bytes are atPacket
        if (bytes.length < 11)
            return null;
        byte[] atPacketBytes = Arrays.copyOfRange(bytes, 0, 10);
        byte[] data = Arrays.copyOfRange(bytes, 11, bytes.length);
        String at = new String(atPacketBytes, StandardCharsets.US_ASCII);
        String[] atPacket = at.split(",");
        byte prevHop = (byte) Integer.parseInt(atPacket[1]);

        switch (data[0]) {
            case (Type.CUSTOM): {
                break;
            }
            case (Type.RREQ): {
                if (data.length != 8)
                    return null;
                RREQ rreq = new RREQ(data[1], data[2], data[3], data[4], data[5], data[6], data[7]);
                rreq.setPrevHop(prevHop);
                return rreq;
            }
            case (Type.RREP): {
                if (data.length != 6)
                    return null;
                RREP rrep = new RREP(data[1], data[2], data[3], data[4], data[5]);
                rrep.setPrevHop(prevHop);
                return rrep;
            }
            case (Type.RERR): {
                if (data.length != 6)
                    return null;
                RERR rrer = new RERR(data[1], data[2], data[3], data[4], data[5]);
                rrer.setPrevHop(prevHop);
                return rrer;
            }
            case (Type.RREP_ACK): {
                if (data.length != 1)
                    return null;
                RREP_ACK rrepAck = new RREP_ACK();
                rrepAck.setPrevHop(prevHop);
                return rrepAck;
            }
            case (Type.SEND_TEXT_REQUEST): {
                if (data.length < 5 || data.length > 34)
                    return null;
                byte[] payloadBytes = Arrays.copyOfRange(data, 4, data.length);
                String payload = new String(payloadBytes, StandardCharsets.US_ASCII);
                SEND_TEXT_REQUEST sendTextRequest = new SEND_TEXT_REQUEST(data[1], data[2], data[3], payload);
                sendTextRequest.setPrevHop(prevHop);
                return sendTextRequest;
            }
            case (Type.SEND_HOP_ACK): {
                if (data.length != 2)
                    return null;
                SEND_HOP_ACK sendHopAck = new SEND_HOP_ACK(data[1]);
                sendHopAck.setPrevHop(prevHop);
                return sendHopAck;
            }
            case (Type.SEND_TEXT_REQUEST_ACK): {
                if (data.length != 4)
                    return null;
                SEND_TEXT_REQUEST_ACK sendTextRequestAck = new SEND_TEXT_REQUEST_ACK(data[1], data[2], data[3]);
                sendTextRequestAck.setPrevHop(prevHop);
                return sendTextRequestAck;
            }
        }
        return null;
    }

    public boolean initialize() {
        if (!isRunning.get()) {
            isRunning.set(true);
            loraController = new LoraController(config, chatsDiscovery, messagesQueue);
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
                routeRequestHandlerHashMap.forEach((k, v) -> v.gotRREP());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ChatsController.writeToLog("Lora Controller Thread ended");
        }
    }

    public AtomicBoolean getIsRunning() {
        return isRunning;
    }

    public BlockingQueue<Message> getMessagesQueue() {
        return messagesQueue;
    }
}
