package htw.ai.protocoll;

import htw.ai.protocoll.message.RREQ;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author : Enrico Gamil Toros de Chadarevian
 * Project name : LoraProtocolInterface
 * @version : 1.0
 * @since : 12-06-2021
 **/
public class RouteRequestHandler implements Runnable{
    private int tries = 0;
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
        while (isRunning.get()) {
            try {
                aodvController.getMessagesQueue().put(rreq);
                int MAX_TRIES = 5;
                while (!gotRREP.get() || tries < MAX_TRIES) {
                    int DELAY_IN_SECONDS = 5;
                    Thread.sleep(DELAY_IN_SECONDS * 1000);
                    tries++;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void gotRREP() {
        this.gotRREP.set(true);
        this.isRunning.set(false);
    }
}
