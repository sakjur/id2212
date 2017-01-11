package is.mjuk.fish;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Handles multicasting between peers
 */
public class DatagramHandler implements ConnectorInterface, Runnable {
    private int port = -1;
    private MulticastSocket m_socket;
    private Client parent;
    private boolean running = true;
    private InetAddress address;
    private int m_port = 7000;

    /**
     * Creates a socket for multicasting and joins the FISH group
     *
     * @param parent The {@link is.mjuk.fish.Client} which initialized this
     * object
     */
    public DatagramHandler(Client parent) {
        this.parent = parent;
        try {
            this.address = InetAddress.getByName("239.10.10.10");
            this.m_socket = new MulticastSocket(m_port);
            this.m_socket.joinGroup(address);
        } catch (Exception e) {
            Helpers.print_err("Could not join multicast group", e.toString());
            this.running = false;
        }
    }

    /**
     * Sets the "sharing"-port
     * <p>
     * This value is presented to other peers when sharing information about
     * an available file. The value should be a port number which is available
     * as a {@link is.mjuk.fish.PeerListener} (over TCP) and a
     * {@link is.mjuk.fish.UnicastListener} (over UDP)
     *
     * @param port New value for the port variable
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Returns true if the datagram handler hasn't exited 
     * @return True if there's a running datagram handler connected to the
     * object
     */
    public boolean isRunning() {
        return this.running;
    }

    /**
     * Send data to the multicast group
     * <p>
     * Name is slightly confusing as enqueue was used for the TCP-send queue.
     * On UDP multicasting, whatever is received here is sent over UDP
     * immediately as there is no need to wait for an ACK.
     *
     * @param data Data to be sent to the FISH multicast group. Max 4096 bytes
     */
    public void enqueue(byte[] data) {
        if (data.length > 4096) {
            Helpers.print_err("Could not send datagram",
                    "Content too large " + String.valueOf(data.length) + " bytes",
                    "Expected max 4096 bytes");
            return;
        }
        DatagramPacket dp = new DatagramPacket(data, data.length, address, m_port);

        try {
            m_socket.send(dp);
        } catch (IOException e) {
            Helpers.print_err("Could not send datagram", e.toString());
        }
    }

    /**
     * Main function for running in thread
     * <p>
     * Listens for multicast messages to the FISH multicast group. If a
     * FIND "port" "file"
     * message is received, looks up if the parent {@link is.mjuk.fish.Client}
     * has that file in it's sharing directory and if so sends a unicast UDP message to
     * the port indicated by the FIND message containing information about
     * the shared port for this {@link is.mjuk.fish.Client} and which file
     * it is talking about.
     */
    public void run() {
        while(this.isRunning()) {
            byte[] buffer = new byte[4096];
            DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
            try {
                m_socket.receive(dp);
            } catch (IOException e) {
                Helpers.print_err("Could not receieve datagram", e.toString());
            }

            try {
                String str = new String(buffer, "UTF-8");
                if (str.startsWith("FIND ")) {
                    String[] s_data = str.substring(5).trim().split(" ");
                    Integer port = Integer.valueOf(s_data[0]);
                    String filename = String.join(" ",
                            Arrays.copyOfRange(s_data, 1, s_data.length));

                    for (File f : this.parent.getFiles()) {
                        if (f.getName().equals(filename)) {
                            String data_s = Integer.toString(this.port) + " " + filename;
                            byte[] data = data_s.getBytes("UTF-8");
                            DatagramPacket response = new DatagramPacket(data,
                                    data.length, dp.getAddress(), port);
                            DatagramSocket socket = new DatagramSocket(0);
                            Thread.sleep(ThreadLocalRandom.current().nextInt(1250));
                            socket.send(response);
                        }
                    }
                } else {
                    System.out.println(str);
                }
            } catch (Exception e) {
                // Ignore for now
            }
        }
    }
}
