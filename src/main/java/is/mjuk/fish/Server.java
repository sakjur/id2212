/**
 * Main class for the FISH file sharing server for ID2212
 *
 * (c) 2017 Emil Tullstedt <emiltu(a)kth.se>
 */

package is.mjuk.fish;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

public class Server {
    private ServerSocket socket;

    public Server() {
        try {
            this.socket = new ServerSocket(7000);
        } catch (IOException e) {
            Helpers.print_err("Could not establish server socket", e.toString());
            System.exit(-1);
        }
    }

    public static void main(String[] args) {
        Server s = new Server();
        s.run();
    }

    public void run() {
        while (true) {
            try {
                Socket conn = socket.accept();
                Thread client_t = new Thread(new ClientConnector(conn, this));
                client_t.start();
            } catch (IOException e) {
                Helpers.print_err("Could not make client connection", e.toString());
            }
        }
    }

    public class ClientConnector implements Runnable, ConnectorInterface {
        private LinkedBlockingQueue<byte[]> sending_queue = new
            LinkedBlockingQueue<byte[]>();
        private Socket conn;
        private Server parent;
        private boolean running = true;

        public ClientConnector(Socket conn, Server parent) {
            this.conn = conn;
            this.parent = parent;
        }
       
        public boolean isRunning() {
            return this.running;
        }

        public void enqueue(byte[] bytes) {
            try {
                this.sending_queue.put(bytes);
            } catch (InterruptedException e) {
                Helpers.print_err("Failed adding object to queue",
                    e.toString());
            }
        }

        public void run() {
            System.out.println("Connected to " + Helpers.CYAN +
                this.conn.getInetAddress().toString() + Helpers.RESET);

            BufferedReader in;
            BufferedOutputStream out;
            try {
                in = new BufferedReader(new InputStreamReader(
                    conn.getInputStream(), "UTF-8"));
                out = new BufferedOutputStream(conn.getOutputStream()); 
            } catch (IOException e) {
                Helpers.print_err("Failed to connect to client", e.toString());
                return;
            }

            // Separate sender from the main (receiving thread) in order to be able
            // to block on the outgoing queue without having to semaphore incoming
            // traffic
            Sender sender = new Sender(this.sending_queue, out);
            Thread sender_t = new Thread(sender, "Sender");
            sender_t.start();

            Thread pinger_t = new Thread(new Pinger(this), "Pinger");
            pinger_t.start();

            while (this.isRunning()) {
                String line;
                try {
                    if ((line = in.readLine()) != null) {
                        if (line.equals("PING")) {
                            this.enqueue("PONG\r\n");
                        } else {
                            System.out.println(line);
                        }
                    }
                } catch (IOException e) {
                }

                if (sender.isRunning() == false) {
                    Helpers.print_err("Client did not respond to ping",
                        "Closing client connectiong thread for " +
                        this.conn.getInetAddress().toString());
                    this.running = false;
                }
                Thread.currentThread().yield();
            }
        }
    }
}
