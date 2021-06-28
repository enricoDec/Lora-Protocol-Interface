package htw.ai.protocoll;

import htw.ai.application.controller.ChatsController;
import htw.ai.lora.Log;
import htw.ai.lora.Logger;
import htw.ai.protocoll.message.*;
import javafx.scene.paint.Color;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author : Enrico Gamil Toros de Chadarevian
 * Project name : LoraProtocolInterface
 * @version : 1.0
 * @since : 12-06-2021
 **/
public class MessageRequest implements Runnable {
    private final Message message;
    private final AtomicBoolean isRunning = new AtomicBoolean(true);
    private final AodvController aodvController;
    private final AtomicBoolean gotReply = new AtomicBoolean(false);
    private byte destination = 0;
    // Logger
    private Logger logger = Logger.getInstance();

    public MessageRequest(Message message, AodvController aodvController) {
        if (message.getTYPE() != Type.RREQ && message.getTYPE() != Type.RREP && message.getTYPE() != Type.SEND_TEXT_REQUEST)
            throw new IllegalArgumentException("Message type " + message.getTYPE() + " invalid");
        this.message = message;
        this.aodvController = aodvController;
    }

    @Override
    public void run() {
        int MAX_TRIES = 3;
        while (isRunning.get() && !gotReply.get()) {
            try {
                int DELAY_IN_SECONDS = 5;
                int RREQ_DELAY_IN_SECONDS = 30;
                for (int tries = 1; tries <= MAX_TRIES; tries++) {
                    if (gotReply.get()) {
                        return;
                    }
                    // Increase Seq Number
                    if (message.getTYPE() == Type.RREQ) {
                        RREQ rreq = (RREQ) message;
                        if (tries > 1) {
                            byte seqNumber = aodvController.incrementSeqNumber();
                            rreq.setOriginSequenceNumber(seqNumber);
                        }
                        this.destination = rreq.getDestinationAddress();
                        aodvController.getMessagesQueue().put(rreq);
                        logger.addToLog(new Log(null, "REQ try: " + tries));

                        try {
                            Thread.sleep(RREQ_DELAY_IN_SECONDS * 1000);
                        } catch (InterruptedException e) {
                            // Ignore
                        }
                    } else if (message.getTYPE() == Type.RREP) {
                        RREP rrep = (RREP) message;
                        this.destination = rrep.getDestinationAddress();
                        aodvController.getMessagesQueue().put(rrep);
                        logger.addToLog(new Log(null, "RREP try: " + tries));
                        try {
                            Thread.sleep(DELAY_IN_SECONDS * 1000);
                        } catch (InterruptedException e) {
                            // Ignore
                        }
                    } else if (message.getTYPE() == Type.SEND_TEXT_REQUEST) {
                        SEND_TEXT_REQUEST sendTextRequest = (SEND_TEXT_REQUEST) message;
                        this.destination = sendTextRequest.getDestinationAddress();
                        sendTextRequest.setMessageSequenceNumber(aodvController.incrementMessageId());
                        aodvController.getMessagesQueue().put(sendTextRequest);
                        logger.addToLog(new Log(null, "SEND TEXT try: " + tries));

                        try {
                            Thread.sleep(DELAY_IN_SECONDS * 1000);
                        } catch (InterruptedException e) {
                            // Ignore
                        }
                    }
                }
                this.gotReply.set(true);
                this.isRunning.set(false);
                logger.addToLog(new Log(Color.DARKRED, "No Reply"));
                if (destination != 0)
                    aodvController.removeMessageRequest(destination);
                return;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Call if RREP to RREQ is received
     */
    public synchronized void gotACK() {
        this.gotReply.set(true);
        this.isRunning.set(false);
    }

    public AtomicBoolean getIsRunning() {
        return isRunning;
    }
}
