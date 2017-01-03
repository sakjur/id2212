package is.mjuk.fish;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
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
public class ServerConnector implements Runnable, ConnectorInterface {
    private InetSocketAddress addr;
    private LinkedBlockingQueue<byte[]> sending_queue = new
        LinkedBlockingQueue<byte[]>();
    private boolean running = true;

    public ServerConnector(InetSocketAddress addr) {
        this.addr = addr;
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
        this.enqueue("EXIT\r\n");
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

        while(this.running) {
            String line;
            try {
                if ((line = in.readLine()) != null) {
                    if (line.equals("PING")) {
                        // Ignore PINGs 
                    } else {
                        System.out.println("\n" + line);
                    }
                }
            } catch (IOException e) {
            }


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
    }
}

