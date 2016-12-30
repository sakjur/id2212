package is.mjuk.fish;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.NumberFormatException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Connects the client to a server
 * <p>
 * Connects a {@link is.mjuk.fish.Client} to a server for communication.
 * Utilizes message queues based on LinkedBlockingQueues for incoming and
 * outgoing messages
 */
public class ServerConnector implements Runnable {
    private InetSocketAddress addr;
    private LinkedBlockingQueue<byte[]> sending_queue = new
        LinkedBlockingQueue<byte[]>();
    private boolean running = true;

    public ServerConnector(InetSocketAddress addr) {
        this.addr = addr;
    }

    /**
     * Add a string to be sent to the server
     */
    public void enqueue(String str) {
        this.enqueue(str.getBytes());
    }

    /**
     * Add an byte array to the queue to be sent to the server
     */
    public void enqueue(byte[] bytes) {
        try {
            this.sending_queue.put(bytes);
        } catch (InterruptedException e) {
            Helpers.print_err("Failed adding object to queue",
                e.toString());
        }
    }

    public void exit() {
        this.running = false;
    }

    public boolean isRunning() {
        return this.running;
    }

    public void run() {
        Socket socket = new Socket();
        BufferedReader in;
        BufferedOutputStream out;
        try {
            socket.connect(addr, 500);
            in = new BufferedReader(new InputStreamReader(
                socket.getInputStream(), "UTF-8"));
            out = new BufferedOutputStream(socket.getOutputStream()); 

            this.enqueue("HELLO\r\n");
        } catch (IOException e) {
            Helpers.print_err("Could not connect to server", e.toString());
            this.running = false;
            return;
        }

        // Separate sender from the main (receiving thread) in order to be able
        // to block on the outgoing queue without having to semaphore incoming
        // traffic
        Sender sender = new Sender(this.sending_queue, out);
        Thread sender_t = new Thread(sender, "Sender");
        sender_t.start();

        // This thread just sends ping to the server once in a while
        // If this is here when you are reading this (whoever you are) that's
        // probably not a very good sign. I should probably remove this.
        // 
        // Not going to care though.
        Thread pinger_t = new Thread(new Pinger(this), "Pinger");
        pinger_t.start();

        while(this.running) {
            // TODO Read the incoming stream 
            if (sender.isRunning() == false) {
                this.running = false;
            }
            Thread.currentThread().yield();
        }

        try {
            socket.close();
        } catch (IOException e) {
            Helpers.print_err("Could not close socket (wat?)",
                e.toString());
        }
        sender.exit();
        pinger_t.interrupt();
    }

    /**
     * The class that goes PING
     */
    private class Pinger implements Runnable {
        ServerConnector conn; 

        public Pinger(ServerConnector conn) {
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

    private class Sender implements Runnable {
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
}

