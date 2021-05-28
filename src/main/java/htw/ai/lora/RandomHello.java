package htw.ai.lora;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author : Enrico Gamil Toros de Chadarevian
 * Project name : Lora-Protocol
 * @version : 1.0
 * @since : 20-05-2021
 **/
public class RandomHello implements Runnable {
    private LoraUARTController loraUARTController;
    private boolean stop = false;

    public RandomHello(LoraUARTController loraUARTController) {
        this.loraUARTController = loraUARTController;
    }

    public void start() {
        Thread thread = new Thread(this, "RandomHello_Thread");
        thread.start();
    }

    @Override
    public void run() {
        sendRandomHello();
    }

    public void stop() {
        stop = true;
        loraUARTController.getReplyQueue().clear();
    }

    private void sendRandomHello() {
        while (!stop) {
            Long startMillis = System.currentTimeMillis();
            // Random interval between 5sec and 10sec
            int randomInterval = ThreadLocalRandom.current().nextInt(5000, 10000);
            try {
                Thread.sleep(randomInterval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            try {
                BlockingQueue<String> writeQueue = loraUARTController.getWriteQueue();
                BlockingQueue<String> replyCode = loraUARTController.getReplyQueue();
                writeQueue.put(Lora.AT_SEND.getCODE() + "5");
                replyCode.take();
                writeQueue.put("Hello");
                replyCode.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

}
