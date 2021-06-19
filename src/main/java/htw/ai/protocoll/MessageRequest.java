package htw.ai.protocoll;

import htw.ai.application.controller.ChatsController;
import htw.ai.protocoll.message.Message;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author : Enrico Gamil Toros de Chadarevian
 * Project name : LoraProtocolInterface
 * @version : 1.0
 * @since : 12-06-2021
 **/
public class MessageRequest implements Runnable{
    private final Message message;
    private final AtomicBoolean isRunning = new AtomicBoolean(true);
    private final AodvController aodvController;
    private final AtomicBoolean gotReply = new AtomicBoolean(false);

    public MessageRequest(Message message, AodvController aodvController) {
        if (message.getTYPE() != 1 && message.getTYPE() != 2 && message.getTYPE() != 5)
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
                    if (gotReply.get()){
                        ChatsController.writeToLog("Got ACK");
                        return;
                    }
                    ChatsController.writeToLog("Sending Message tries: " + tries);
                    aodvController.getMessagesQueue().put(message);
                    Thread.sleep(DELAY_IN_SECONDS * 1000);
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
