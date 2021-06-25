package htw.ai.protocoll;

import htw.ai.application.controller.ChatsController;
import htw.ai.protocoll.message.Message;
import htw.ai.protocoll.message.RREP;
import htw.ai.protocoll.message.RREQ;
import htw.ai.protocoll.message.Type;

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
                int DELAY_IN_SECONDS = 30;
                for (int tries = 1; tries <= MAX_TRIES; tries++) {
                    // Increase Seq Number
                    if (tries > 1 && message.getTYPE() == Type.RREQ) {
                        RREQ rreq = (RREQ) message;
                        byte seqNumber = aodvController.incrementSeqNumber();
                        rreq.setOriginSequenceNumber(seqNumber);
                    }

                    if (gotReply.get()) {
                        ChatsController.writeToLog("Got ACK");
                        return;
                    }
                    ChatsController.writeToLog("Sending Message tries: " + tries);
                    aodvController.getMessagesQueue().put(message);
                    try {
                        Thread.sleep(DELAY_IN_SECONDS * 1000);
                    } catch (InterruptedException e) {
                        // Ignore
                    }
                }
                ChatsController.writeToLog("Got no ACK");
                gotReply.set(true);
                return;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Call if RREP to RREQ is received
     */
    public void gotACK() {
        this.gotReply.set(true);
        this.isRunning.set(false);
    }

    public AtomicBoolean getIsRunning() {
        return isRunning;
    }
}
