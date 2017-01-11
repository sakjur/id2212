package is.mjuk.fish;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * An interface for a thread receiving and sending messages
 */
public interface ConnectorInterface {
    /**
     * Is the thread receiving messages?
     */
    public boolean isRunning();

    /**
     * By default, convert strings to byte arrays and send them to the enqueue
     * with byte array as input
     *
     * @param str A string which is converted to a byte array and then sent to
     * the enqueue method as implemented by the implemenational class for
     * byte arrays
     */
    default public void enqueue(String str) {
        this.enqueue(str.getBytes());
    };

    /**
     * Put data in a send queue
     *
     * @param data Data to be sent to one or more remote units
     */
    public void enqueue(byte[] data);
}
