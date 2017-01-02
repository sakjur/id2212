package is.mjuk.fish;
import java.util.concurrent.LinkedBlockingQueue;

public interface ConnectorInterface {
    public boolean isRunning();
    default public void enqueue(String str) {
        this.enqueue(str.getBytes());
    };
    public void enqueue(byte[] data);
}
