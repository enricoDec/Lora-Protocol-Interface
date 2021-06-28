package htw.ai.lora;

import javafx.scene.paint.Color;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author : Enrico Gamil Toros
 * Project name : LoraProtocollInterface
 * @version : 1.0
 * @since : 28.06.21
 **/
public class Logger implements Runnable {
    private BlockingQueue<Log> logQueue = new ArrayBlockingQueue<>(25);
    private AtomicBoolean isRunning = new AtomicBoolean(true);
    private static final Logger logger = new Logger();

    public static Logger getInstance() {
        return logger;
    }

    public synchronized void addToLog(Log log) {
        try {
            logQueue.put(log);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void printLog(Log log) {
        Color color = log.getColor();
        if (color == Color.DARKRED)
            System.out.print("\033[1;31m");
        else if (color == Color.CYAN)
            System.out.print("\033[1;36m");
        else if (color == Color.YELLOW)
            System.out.print("\033[1;33m");
        else
            System.out.print("\033[0m");

        System.out.println(log.getMessage());
        System.out.print("\033[0m");
    }

    @Override
    public void run() {
        while (isRunning.get()) {
            if (!logQueue.isEmpty()) {
                try {
                    printLog(logQueue.take());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
