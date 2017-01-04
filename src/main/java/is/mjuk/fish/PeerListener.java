package is.mjuk.fish;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ThreadLocalRandom;
import java.util.HashMap;

public class PeerListener implements Runnable {
    private Client parent;
    private Integer port = -1;
    private ServerSocket socket = null;
    private HashMap<String, File> file_list = new HashMap<String, File>();

    public PeerListener(Client parent) {
        this.parent = parent;
        this.port = ThreadLocalRandom.current().nextInt(28000, 28800);
        System.out.format(Helpers.YELLOW +
            "Opening peer listening port on %d\n" + Helpers.RESET,
            port);

        try {
            this.socket = new ServerSocket(this.port);
        } catch (IOException e) {
            Helpers.print_err("Opening peer listener failed", e.toString());
            this.port = -1;
        }

        for (File file : parent.getFiles()) {
            file_list.put(file.getName(), file);
        }
    }

    /**
     * Get the listening port
     *
     * @return Port the peer is listening on
     */
    public int getPort() {
        return this.port;
    }

    public void run() {
        if (this.socket == null) {
            Helpers.print_err("PeerListener socket not defined", "Socket is null");
            return;
        }

        while (true) {
            try {
                Socket conn = socket.accept();
                SinglePeerListener peer = new SinglePeerListener(conn, file_list);
                Thread peer_t = new Thread(peer, "Single Peer Listener");
                peer_t.start();
            } catch (IOException e) {
                Helpers.print_err("PeerListener socket error", e.toString());
            }
        }
    }

    public class SinglePeerListener implements Runnable {
        private Socket conn;
        private HashMap<String, File> file_list;
        public SinglePeerListener(Socket conn, HashMap<String, File> file_list) {
            this.conn = conn;
            this.file_list = file_list;
        }

        public void run() {
            BufferedReader in;
            BufferedOutputStream out;
            try {
                in = new BufferedReader(new InputStreamReader(
                    conn.getInputStream(), "UTF-8"));
                out = new BufferedOutputStream(conn.getOutputStream()); 
            } catch (IOException e) {
                Helpers.print_err("Failed to connect to peer", e.toString());
                return;
            }

            while (true) {
                String line;
                String file_to_send = null;
                try {
                    if ((line = in.readLine()) != null) {
                        if (line.startsWith("DOWNLOAD ")) {
                           file_to_send = line.substring(9); 
                        }
                    }

                    if (file_to_send != null) {
                        File file = file_list.get(file_to_send);
                        if (file == null) {
                            String notfound = "NOTFOUND " + file_to_send + "\r\n";
                            out.write(notfound.getBytes());
                            out.flush();
                            break;
                        }
                        FileInputStream file_io = new FileInputStream(file);
                        byte[] b = new byte[4096];

                        while(file_io.read(b) != -1) {
                            out.write(b);
                            out.flush();
                        }

                        conn.close();
                        break;
                    }
                } catch (IOException e) {
                    Helpers.print_err("Peer listener I/O error", e.toString());
                    return;
                }
            }
        }
    }
}
