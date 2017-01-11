package is.mjuk.fish;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Listens to the peer sharing for information about searches
 * <p>
 * Opens a UDP listening socket which the {@link is.mjuk.fish.DatagramHandler}
 * connects to and sends information about having a file available to.
 *
 * Uses the same port as the {@link is.mjuk.fish.PeerListener} for the
 * particular peer
 */
public class UnicastListener implements Runnable {
    Integer port;
    Downloader downloader;

    public UnicastListener(int port, Downloader downloader) {
        this.port = port;
        this.downloader = downloader;
    }

    public void run() {
        try (DatagramSocket socket = new DatagramSocket(this.port)) {
            while (true) {
                byte[] data = new byte[4096];
                DatagramPacket r = new DatagramPacket(data, 0, data.length);
                socket.receive(r);
                String data_to_string = new String(data, "UTF-8").trim();
                String host_string = r.getAddress().getHostName() + " " +
                    data_to_string;

                System.out.println("\nFOUND " + host_string);
                this.downloader.add_hoststring(host_string);
            }
        } catch (IOException e) {
            Helpers.print_err("UDP unicast server socket crashed...",
                    e.toString());
        }
    }
}
