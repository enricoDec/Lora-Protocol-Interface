package htw.ai.protocoll;

import com.fazecast.jSerialComm.SerialPortInvalidPortException;
import htw.ai.application.controller.ChatsController;
import htw.ai.application.model.ChatsDiscovery;
import htw.ai.application.model.ClientMessage;
import htw.ai.application.model.UserMessage;
import htw.ai.lora.LoraController;
import htw.ai.lora.config.Config;
import htw.ai.protocoll.message.*;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author : Enrico Gamil Toros de Chadarevian
 * Project name : LoraProtocolInterface
 * @version : 1.0
 * @since : 09-06-2021
 **/
public class AodvController implements Runnable {
    private final byte ROUTE_LIFETIME_IN_SECONDS = (byte) 180;
    private byte sequenceNumber = 0;
    private byte rreqID = 0;

    // Routing table of all valid routes
    private HashMap<Integer, Route> routingTable = new HashMap<>();
    // Queue of user input Messages
    private BlockingQueue<UserMessage> userMessagesQueue;
    // Queue of incoming Messages from other Modules
    private BlockingQueue<byte[]> lrQueue;
    // Queue of outgoing Messages to other Modules
    private BlockingQueue<Message> messagesQueue = new ArrayBlockingQueue<>(20);
    // Thread state
    private AtomicBoolean isRunning = new AtomicBoolean(false);
    // List of all message Requests
    private HashMap<Byte, MessageRequest> messageRequests = new HashMap<>();
    // List of all the message Request threads
    private LinkedList<Thread> messageRequestThreads = new LinkedList<>();
    // List of all messages pending route to be send
    private HashMap<Integer, LinkedList<UserMessage>> pendingRouteMessages = new HashMap<>();
    // List of all messages send and pending for ack
    private HashMap<Byte, LinkedList<Message>> pendingACKMessages = new HashMap<>();

    private LoraController loraController;
    private Config config;
    private ChatsDiscovery chatsDiscovery;
    Thread lora_thread;

    public AodvController(BlockingQueue<UserMessage> userMessagesQueue, Config config, ChatsDiscovery chatsDiscovery) {
        this.userMessagesQueue = userMessagesQueue;
        this.config = config;
        this.chatsDiscovery = chatsDiscovery;
    }


    /**
     * Increment the Sequence Number
     *
     * @return the new value of the sequence Number
     */
    public synchronized byte incrementSeqNumber() {
        return sequenceNumber++;
    }

    /**
     * Create a RREQ
     *
     * @param rreq RREQ to be send
     */
    private void createRouteRequest(RREQ rreq) {
        sequenceNumber++;
        rreqID++;
        MessageRequest messageRequest = new MessageRequest(rreq, this);
        Thread thread = new Thread(messageRequest);
        thread.start();
        messageRequests.put(rreq.getDestinationAddress(), messageRequest);
        messageRequestThreads.add(thread);
    }

    /**
     * Handle an incoming RREQ
     *
     * @param rreq RREQ to be handled
     */
    private void handleRouteRequest(RREQ rreq) {
        // Don't handle own RREQs
        if (rreq.getOriginAddress() == config.getAddress())
            return;

        if (rreq.getDestinationAddress() != (byte) config.getAddress()) {
            // Am Intermediate Node

        } else if (rreq.getDestinationAddress() == (byte) config.getAddress()) {
            // Am Destination Node
        }
    }

    /**
     * Create a RREP
     */
    private void createRouteReply(RREP rrep) {
        sequenceNumber = Byte.MAX_VALUE;
        try {
            messagesQueue.put(rrep);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handle an incoming RREP
     *
     * @param rrep rrep to be handled
     */
    private void handleRouteReply(RREP rrep) {

    }

    /**
     * Create a RERR and send
     *
     * @param rerr RERR
     */
    private void createRouteError(RERR rerr) {

    }

    /**
     * Handle an incoming RERR
     *
     * @param rerr RERR
     */
    private void handleRouteError(RERR rerr) {

    }

    /**
     * Create a RREP ACK
     *
     * @param rrepAck RREP ACK
     */
    private void createRouteReplyACK(RREP_ACK rrepAck) {
        try {
            messagesQueue.put(rrepAck);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handle an incoming RREP ACK
     *
     * @param rrepAck RREP ACK
     */
    private void handleRouteReplyACK(RREP_ACK rrepAck) {

    }

    /**
     * Create a SEND TEXT REQUEST
     *
     * @param message Payload
     * @param route   Route to destination
     */
    private void createTextRequest(SEND_TEXT_REQUEST message, Route route) {
        try {
            messagesQueue.put(message);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (pendingACKMessages.containsKey(message.getDestinationAddress()))
            pendingACKMessages.put(message.getDestinationAddress(), new LinkedList<>(Collections.singletonList(message)));
        else
            pendingACKMessages.get(message.getDestinationAddress()).add(message);
    }

    /**
     * Handle an incoming Text Request
     *
     * @param message Send Text Request
     */
    private void handleTextRequest(SEND_TEXT_REQUEST message) {

    }

    /**
     * Create a SEND HOP ACK
     *
     * @param sendHopAck SEND HOP ACK
     */
    private void createHopACK(SEND_HOP_ACK sendHopAck) {

    }

    /**
     * Handle an incoming SEND HOP ACK
     *
     * @param sendHopAck SEND HOP ACK
     */
    private void handleHopACK(SEND_HOP_ACK sendHopAck) {

    }

    /**
     * Create a SEND TEXT REQUEST ACK
     *
     * @param sendTextRequestAck Send Text Request ACK
     */
    private void createTextRequestACK(SEND_TEXT_REQUEST_ACK sendTextRequestAck) {

    }

    /**
     * Create a SEND TEXT REQUEST ACK
     *
     * @param sendTextRequestAck Send Text Request ACK
     */
    private void handleTextRequestACK(SEND_TEXT_REQUEST_ACK sendTextRequestAck) {

    }

    /**
     * Route a Message if route to destination node is present in the routing table send TEXT REQUEST else create RREQ to destination
     *
     * @param userMessage Message to be send
     */
    private void route(UserMessage userMessage) {
        // If Valid route for destination already present send SEND_TEXT_REQUEST
        Route routeToDestination = routingTable.get(userMessage.getDestinationAddress());
        if (routeToDestination != null && routeToDestination.isValidRoute()) {
            ChatsController.writeToLog("Valid route found in table for " + userMessage.getDestinationAddress() + ", sending Text Req");
            // Create Send Text Request
            SEND_TEXT_REQUEST send_text_request = new SEND_TEXT_REQUEST(String.valueOf(routeToDestination.getNextHop()), (byte) config.getAddress(), (byte) userMessage.getDestinationAddress(), (byte) 0, userMessage.getData());
            createTextRequest(send_text_request, routeToDestination);
            // Put Message in pending Messages until ACK is received
            LinkedList<UserMessage> userMessages = pendingRouteMessages.get(userMessage.getDestinationAddress());
            if (userMessages == null)
                pendingRouteMessages.put(userMessage.getDestinationAddress(), new LinkedList<>(Collections.singletonList(userMessage)));
            else
                userMessages.add(userMessage);
        } else {
            // If no Valid route known RREQ
            ChatsController.writeToLog("No valid route found, sending RREQ");
            // Check if RREQ for destination is already created
            if (messageRequests.get(userMessage.getDestinationAddress()) != null && messageRequests.get(userMessage.getDestinationAddress()).getIsRunning().get()) {
                // Put message in pending Route Queue
                pendingRouteMessages.get(userMessage.getDestinationAddress()).add(userMessage);
            } else
                createRouteRequest(new RREQ((byte) 1, (byte) 0, rreqID, (byte) config.getAddress(), (byte) sequenceNumber, (byte) userMessage.getDestinationAddress(), (byte) 0));
        }
    }

    @Override
    public void run() {
        while (isRunning.get()) {
            if (userMessagesQueue.peek() != null) {
                // If client want to send message start process
                UserMessage userMessage;
                try {
                    userMessage = userMessagesQueue.take();
                    ChatsController.writeToLog("User want to send a Message, routing...");
                    route(userMessage);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else if (lrQueue.peek() != null) {
                // If message received
                try {
                    byte[] data = lrQueue.take();
                    Message message = decode(data);
                    if (message == null) {
                        ChatsController.writeToLog("Bad Packet received, could not parse.");
                    } else {
                        switch (message.getTYPE()) {
                            case Type.RREP:
                                RREP rrep = (RREP) message;
                                // If we are the origin address of RREQ, RREP is meant for us -> Insert in routing table
                                if (rrep.getOriginAddress() == config.getAddress() && messageRequests.containsKey(rrep.getDestinationAddress())) {
                                    messageRequests.get(rrep.getDestinationAddress()).gotACK();
                                    // RREP-ACK
                                    RREP_ACK rrepAck = new RREP_ACK(String.valueOf(rrep.getPrevHop()));
                                    createRouteReplyACK(rrepAck);
                                    // Route Found add to table and send
                                    routingTable.put((int) rrep.getDestinationAddress(), new Route(rrep.getDestinationAddress(), rrep.getDestinationSequenceNumber(), true, rrep.getHopCount(), rrep.getPrevHop(), ROUTE_LIFETIME_IN_SECONDS));
                                    LinkedList<UserMessage> userMessages = pendingRouteMessages.get((int) rrep.getDestinationAddress());

                                    // Send Text Message for each pending message
                                    if (userMessages != null) {
                                        for (UserMessage userMessage : userMessages) {
                                            createTextRequest(new SEND_TEXT_REQUEST(String.valueOf(routingTable.get(userMessage.getDestinationAddress()).getNextHop()),
                                                    (byte) config.getAddress(), (byte) userMessage.getDestinationAddress(), sequenceNumber, userMessage.getData()), routingTable.get(userMessage.getDestinationAddress()));
                                        }
                                    }
                                }
                                break;
                            case Type.RREP_ACK:
                                RREP_ACK rrepAck = (RREP_ACK) message;
                                break;
                            case Type.SEND_TEXT_REQUEST:
                                SEND_TEXT_REQUEST sendTextRequest = (SEND_TEXT_REQUEST) message;
                                // If we the destination
                                if (sendTextRequest.getDestinationAddress() == config.getAddress()) {
                                    chatsDiscovery.newClient(new ClientMessage(sendTextRequest.getPayload(), sendTextRequest.getOriginAddress(), sendTextRequest.getDestinationAddress()));
                                }
                                break;
                            case Type.SEND_TEXT_REQUEST_ACK:
                                SEND_TEXT_REQUEST_ACK sendTextRequestAck = (SEND_TEXT_REQUEST_ACK) message;
                                // TODO: 19.06.2021 Implement ACK
                                break;
                            case Type.RERR:
                                RERR rerr = (RERR) message;
                                break;
                            case Type.RREQ:
                                // 1. Reverse Route
                                // 2. Check seq
                                // 3. Hop++ der RREQ
                                // 4. Create entry or update
                                // 5. Forward route if not destination or invalid route or none in table
                                // 6. If intermediate or destination RREP

                                RREQ rreq = (RREQ) message;
                                // Update routing table
                                routingTable.forEach((key, value) -> {
                                    if (value.getLifetime().isBefore(LocalDateTime.now()))
                                        value.setValidRoute(false);
                                });
                                // If RREQ not for me, check if I know route else forward
                                if (rreq.getDestinationAddress() != config.getAddress()) {
                                    Route route = routingTable.get((int) rreq.getDestinationAddress());
                                    // Got no route forward REQ
                                    if (route == null) {
                                        // TODO: 22.06.2021 Test
                                        handleRouteRequest(rreq);
                                    } else {
                                        createRouteReply(new RREP(String.valueOf(route.getNextHop()), route.getHopCount(), rreq.getOriginAddress(),
                                                rreq.getDestinationAddress(), route.getDestinationSequenceNumber(), (byte) Duration.between(route.getLifetime(), LocalDateTime.now()).toSeconds()));
                                    }
                                } else {
                                    // If RREQ destination is me reply with RREP
                                    createRouteReply(new RREP(String.valueOf(rreq.getPrevHop()), (byte) (rreq.getHopCount() + 1), rreq.getOriginAddress(), rreq.getDestinationAddress(), sequenceNumber, ROUTE_LIFETIME_IN_SECONDS));
                                }
                                break;
                            case Type.SEND_HOP_ACK:
                                SEND_HOP_ACK sendHopAck = (SEND_HOP_ACK) message;
                                break;
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * Decode an incoming message to wrapper.
     * Returns null if message malformed or invalid.
     *
     * @param bytes received bytes
     * @return wrapped Message
     */
    private Message decode(byte[] bytes) {
        // LR , ADDR , NUMB_BYTES ,  ......
        // 01 2 3456 7 89         10 11-x
        // First 12 bytes are atPacket
        if (bytes.length < 10)
            return null;
        // Copy LR,ADDR,NUMB_BYTES,
        byte[] atPacketBytes = Arrays.copyOfRange(bytes, 0, 10);
        // Get ADDR
        String at = new String(atPacketBytes, StandardCharsets.US_ASCII);
        String[] atPacket = at.split(",");
        byte prevHop = (byte) Integer.parseInt(atPacket[1]);

        // Copy AODV Message without Lora header
        byte[] data = Arrays.copyOfRange(bytes, 11, bytes.length);

        ChatsController.writeToLog("Got Message from: " + prevHop);
        for (Byte b : data) {
            System.out.print(b + " ");
        }
        System.out.println();

        switch ((int) data[0]) {
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
                RREP rrep = new RREP("", data[1], data[2], data[3], data[4], data[5]);
                rrep.setPrevHop(prevHop);
                return rrep;
            }
            case (Type.RERR): {
                if (data.length != 6)
                    return null;
                RERR rrer = new RERR("", data[1], data[2], data[3], data[4], data[5]);
                rrer.setPrevHop(prevHop);
                return rrer;
            }
            case (Type.RREP_ACK): {
                if (data.length != 1)
                    return null;
                RREP_ACK rrepAck = new RREP_ACK("");
                rrepAck.setPrevHop(prevHop);
                return rrepAck;
            }
            case (Type.SEND_TEXT_REQUEST): {
                if (data.length < 5 || data.length > 34)
                    return null;
                byte[] payloadBytes = Arrays.copyOfRange(data, 4, data.length);
                String payload = new String(payloadBytes, StandardCharsets.US_ASCII);
                SEND_TEXT_REQUEST sendTextRequest = new SEND_TEXT_REQUEST("", data[1], data[2], data[3], payload);
                sendTextRequest.setPrevHop(prevHop);
                return sendTextRequest;
            }
            case (Type.SEND_HOP_ACK): {
                if (data.length != 2)
                    return null;
                SEND_HOP_ACK sendHopAck = new SEND_HOP_ACK("", data[1]);
                sendHopAck.setPrevHop(prevHop);
                return sendHopAck;
            }
            case (Type.SEND_TEXT_REQUEST_ACK): {
                if (data.length != 4)
                    return null;
                SEND_TEXT_REQUEST_ACK sendTextRequestAck = new SEND_TEXT_REQUEST_ACK("", data[1], data[2], data[3]);
                sendTextRequestAck.setPrevHop(prevHop);
                return sendTextRequestAck;
            }
        }
        return null;
    }

    /**
     * Initialize thread. <b>Need to be called before thread.start()!</b>
     *
     * @return true if the port to the lora module was opened successfully else false
     */
    public boolean initialize() {
        if (!isRunning.get()) {
            isRunning.set(true);
            try {
                loraController = new LoraController(config, chatsDiscovery, messagesQueue);
            } catch (SerialPortInvalidPortException e) {
                e.printStackTrace();
                return false;
            }
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

    /**
     * Stop the Thread and all the child threads
     */
    public void stop() {
        if (isRunning.get()) {
            isRunning.set(false);
            if (loraController != null) {
                loraController.stop();
                try {
                    lora_thread.join();
                    ChatsController.writeToLog("Lora Controller ended");
                    // Stop all route Requests threads that are still running
                    AtomicInteger i = new AtomicInteger();
                    messageRequestThreads.forEach(t -> {
                        if (t.isAlive()) {
                            t.interrupt();
                            i.getAndIncrement();
                        }
                    });
                    ChatsController.writeToLog(i.get() + " Message Request Thread(s) were interrupted.");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public AtomicBoolean getIsRunning() {
        return isRunning;
    }

    public BlockingQueue<Message> getMessagesQueue() {
        return messagesQueue;
    }


}
