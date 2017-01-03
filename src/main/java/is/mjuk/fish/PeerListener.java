package is.mjuk.fish;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
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
                socket.accept();
            } catch (IOException e) {
                Helpers.print_err("PeerListener socket error", e.toString());
            }

            // TODO Receive download request

            // TODO Lookup file

            // TODO Send file or Did Not Find
        }
    }
}
