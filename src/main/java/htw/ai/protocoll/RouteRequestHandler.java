package htw.ai.protocoll;

import htw.ai.application.controller.ChatsController;
import htw.ai.protocoll.message.RREQ;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author : Enrico Gamil Toros de Chadarevian
 * Project name : LoraProtocolInterface
 * @version : 1.0
 * @since : 12-06-2021
 **/
public class RouteRequestHandler implements Runnable{
    private final RREQ rreq;
    private final AtomicBoolean isRunning = new AtomicBoolean(true);
    private final AodvController aodvController;
    private final AtomicBoolean gotRREP = new AtomicBoolean(false);

    public RouteRequestHandler(RREQ rreq, AodvController aodvController) {
        this.rreq = rreq;
        this.aodvController = aodvController;
    }

    @Override
    public void run() {
        int MAX_TRIES = 3;
        while (isRunning.get() && !gotRREP.get()) {
            try {
                int DELAY_IN_SECONDS = 30;
                for (int tries = 1; tries <= MAX_TRIES; tries++) {
                    if (gotRREP.get()){
                        ChatsController.writeToLog("Got RREP");
                        return;
                    }
                    ChatsController.writeToLog("Sending RREQ tries: " + tries);
                    aodvController.getMessagesQueue().put(rreq);
                    Thread.sleep(DELAY_IN_SECONDS * 1000);
                }
                ChatsController.writeToLog("Got no RREP");
                return;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Call if RREP to RREQ is received
     */
    public void gotRREP() {
        this.gotRREP.set(true);
        this.isRunning.set(false);
    }

    public RREQ getRreq() {
        return rreq;
    }
}
