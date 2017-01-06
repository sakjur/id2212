package is.mjuk.fish;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.HashMap;

public class DatagramHandler implements ConnectorInterface, Runnable {
    private HashMap<String, ArrayList<InetSocketAddress>> download_pending;
    private int port = -1;
    private MulticastSocket m_socket;
    private Client parent;
    private boolean running = true;
    private InetAddress address;
    private int m_port = 7000;

    public DatagramHandler(HashMap<String, ArrayList<InetSocketAddress>> download_pending,
            Client parent) {
        this.download_pending = download_pending;
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

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isRunning() {
        return this.running;
    }
    
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
                System.out.println(new String(buffer, "UTF-8"));
            } catch (Exception e) {
                // Ignore for now
            }
        }
    }
}
