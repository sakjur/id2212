package is.mjuk.fish;

/**
 * The class that goes PING
 */
public class Pinger implements Runnable {
    ConnectorInterface conn; 

    public Pinger(ConnectorInterface conn) {
        this.conn = conn;
    }

    public void run() {
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
        while(conn.isRunning()) {
            conn.enqueue("PING\r\n");
            try {
                Thread.sleep(3000); 
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}
