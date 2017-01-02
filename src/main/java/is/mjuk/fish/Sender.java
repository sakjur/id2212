package is.mjuk.fish;

import java.io.BufferedOutputStream;
import java.util.concurrent.LinkedBlockingQueue;

public class Sender implements Runnable {
    private LinkedBlockingQueue<byte[]> queue;
    private BufferedOutputStream out;
    private boolean running = true;

    public Sender(LinkedBlockingQueue<byte[]> queue, BufferedOutputStream out) {
        this.queue = queue;
        this.out = out;
    }

    public void exit() {
        this.running = false;
    }

    public boolean isRunning() {
        return this.running;
    }

    public void run() {
        while (this.running) {
            try {
                byte[] outgoing = queue.take();
                out.write(outgoing);
                out.flush();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception e) {
                Helpers.print_err("Cannot send message to server",
                    e.toString());
                this.running = false;
                return;
            }
        }
    }
}
