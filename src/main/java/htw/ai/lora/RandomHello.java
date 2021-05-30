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
    private LoraUART loraUART;
    private boolean stop = false;

    public RandomHello(LoraUART loraUART) {
        this.loraUART = loraUART;
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
        loraUART.getReplyQueue().clear();
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
                BlockingQueue<String> writeQueue = loraUART.getWriteQueue();
                BlockingQueue<String> replyCode = loraUART.getReplyQueue();
                writeQueue.put(Lora.AT_SEND.CODE + "5");
                replyCode.take();
                writeQueue.put("Hello");
                replyCode.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

}
