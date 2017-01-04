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
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.HashMap;
import java.util.TreeSet;

public class Server {
    private ServerSocket socket;
    private HashMap<String, ClientStore> connected_clients =
        new HashMap<String, ClientStore>();
    private HashMap<String, TreeSet<ClientStore>> filedb =
        new HashMap<String, TreeSet<ClientStore>>();

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
        String welcome = Helpers.YELLOW +
            "Server started listening\n" +
            Helpers.RESET +
            "Connect clients to port " + Helpers.GREEN + "7000\n" +
            Helpers.BLUE +
            "Enjoy " + Helpers.RED + "<3\n" + Helpers.RESET;
        System.out.println(welcome);

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

    /**
     * Possibly a System.out.println-wrapper
     * <p>
     * Wraps System.out.println and may be outcommented (in which case it's a
     * nop) for development purposes.
     *
     * @param str String to (possibly) be displayed to the user
     */
    public void debug(String str) {
        // System.out.println(str);
    }

    public synchronized void add_client(InetAddress remote_address, Thread t) {
        this.connected_clients.put(remote_address.getHostAddress(),
            new ClientStore(remote_address, t));

        debug("Added user " + remote_address.getHostAddress());
    }

    public synchronized void set_port(InetAddress client, int port) {
        ClientStore store = this.connected_clients.get(client.getHostAddress());
        store.port = port;
    }

    public synchronized void add_file(InetAddress client, String filename) {
        ClientStore store = this.connected_clients.get(client.getHostAddress());
        store.files.add(filename);
        
        TreeSet<ClientStore> filedb_listing;
        if ((filedb_listing = this.filedb.get(filename)) == null) {
            filedb_listing = new TreeSet<ClientStore>();
            this.filedb.put(filename, filedb_listing);
        }
        filedb_listing.add(store);

        debug("Added file " + filename);
    }

    public synchronized String[] find_file(String filename) {
        TreeSet<ClientStore> fileset = this.filedb.get(filename);
        if (fileset == null) {
            String[] array = new String[]{"NOTFOUND " + filename + "\r\n"};
            return array; 
        }

        ArrayList<String> tmp = new ArrayList<String>();

        for (ClientStore entry : fileset) {
            tmp.add("FOUND " + filename + " " +
                entry.remote_address.getHostAddress() + " " +
                entry.port.toString() + "\r\n");
        }

        return tmp.toArray(new String[tmp.size()]);
    }

    /**
     * Deletes clients launched by specific thread only
     * <p>
     * When a client crashes, the user might restart the client before the old
     * thread is killed. If that happens, the server doesn't want to remove the
     * database entry of the new client and thus this method deletes the client
     * if and only if the thread which initialized the client is the thread
     * requesting deletion of the client.
     *
     * @param remote_address Key for the database entry
     * @param t Thread of the client connection which wishes to delete the
     * database entry
     * @return false if client entry wasn't deleted
     */
    public synchronized boolean del_client(InetAddress remote_address, Thread t) {
        ClientStore store = this.connected_clients.get(remote_address.getHostAddress());
        if (store.thread.equals(t)) {
            return this.del_client(remote_address);
        }
        return false;
    }

    /**
     * Idempotently deletes a client
     * <p>
     * Ensures that a client is deleted after this method has ran
     *
     * @param remote_address Key for the database entry
     * @return Currently always returns true. False would indicate that the
     * objects is still present in the database
     */
    public synchronized boolean del_client(InetAddress remote_address) {
        ClientStore store = this.connected_clients.get(remote_address.getHostAddress());

        if (store == null) {
            return true;
        }

        for (String filename : store.files) {
            TreeSet<ClientStore> dataset = this.filedb.get(filename);
            dataset.remove(store);
            debug("Removed client " + remote_address.getHostAddress());
            if (dataset.size() == 0) {
                this.filedb.remove(filename);
                debug("Removed file " + filename);
            }
        }

        this.connected_clients.remove(remote_address.getHostAddress());

        debug("Deleted client " + remote_address.getHostAddress());
        return true;
    }

    private class ClientStore implements Comparable<ClientStore> {
        public InetAddress remote_address;
        public Integer port = -1;
        public TreeSet<String> files = new TreeSet<String>(); 
        public Thread thread;

        public ClientStore(InetAddress remote_address, Thread t) {
            this.remote_address = remote_address;
            this.thread = t;
        }

        @Override
        public int compareTo(ClientStore other) {
            return this.remote_address.getHostAddress().compareTo(other.remote_address.getHostAddress());
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
            System.out.println(Helpers.RESET + "Connected to " + Helpers.CYAN +
                this.conn.getInetAddress().getHostName() + Helpers.RESET);

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

            this.parent.add_client(this.conn.getInetAddress(),
                Thread.currentThread());

            while (this.isRunning()) {
                String line;
                try {
                    if ((line = in.readLine()) != null) {
                        if (line.equals("PING")) {
                            // Simply ignore the PINGs
                        } else if (line.startsWith("SHARE ")) {
                            this.parent.add_file(this.conn.getInetAddress(),
                                line.substring(6)); 
                        } else if (line.startsWith("FIND ")) {
                            String[] msgs = this.parent.find_file(line.substring(5));
                            for (String msg : msgs) {
                                this.enqueue(msg);
                            }
                        } else if (line.startsWith("PORT ")) {
                            String port = line.substring(5);
                            this.parent.set_port(this.conn.getInetAddress(),
                                Integer.parseInt(port));
                        } else if (line.startsWith("EXIT")) {
                            System.out.println("Closing connection to " +
                                this.conn.getInetAddress().getHostName());
                            this.parent.del_client(this.conn.getInetAddress());
                            break;
                        } else {
                            System.out.println(line);
                        }
                    }
                } catch (IOException e) {
                }

                if (sender.isRunning() == false) {
                    if (this.parent.del_client(this.conn.getInetAddress(),
                            Thread.currentThread())) {
                        Helpers.print_err("Client did not respond to ping",
                            "Closing client connectiong thread for " +
                            this.conn.getInetAddress().getHostName());
                    }
                    break;
                }
                Thread.currentThread().yield();
            }

            this.running = false;
            sender.exit();
        }
    }
}
