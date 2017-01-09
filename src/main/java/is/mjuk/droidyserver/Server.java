package is.mjuk.droidyserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Server for a very, very bad Android rock-paper-scissor game
 */
public class Server {
    ServerSocket socket;

    public static void main(String[] args) {
        Server s = new Server();
        s.run();
    }

    public Server() {
        try {
            this.socket = new ServerSocket(7001); 
        } catch (IOException e) {
            System.err.println("Could not open server on port 7001...");
            System.exit(-1);
        }
    }

    public void run() {
        while (true) {
            try {
                Socket conn = this.socket.accept();
                Connector client = new Connector(conn);
                Thread client_t = new Thread(client);
                client_t.start();
            } catch (IOException e) {
                System.err.println("An error occurred with the socket");
            }
        }
    }
}
