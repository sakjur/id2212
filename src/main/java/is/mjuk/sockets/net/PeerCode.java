package is.mjuk.sockets.net;

import is.mjuk.sockets.meetup.MeetupCallbackInterface;
import is.mjuk.sockets.meetup.MeetupRunner;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class PeerCode implements Runnable, MeetupCallbackInterface {
    private boolean ready = false;
    private InetSocketAddress ip;
    private ServerSocket socket;
    private MeetupRunner meetup_runner;
    private String peerlist;

    public PeerCode(String ip, int port, String peerlist, MeetupRunner m) {
        try {
            this.ip = new InetSocketAddress(InetAddress.getByName(ip), port);
        } catch (UnknownHostException e) {
            // pass
        }

        m.add_to_callbackqueue(this);
        this.meetup_runner = m;
        this.peerlist = peerlist;
    }

    public void MeetupCallback(MeetupRunner.CallbackType cb) {
        if (cb == MeetupRunner.CallbackType.READY) {
            this.ready = true;
        } else if (cb == MeetupRunner.CallbackType.DONE) {
            if (this.socket != null) {
                try {
                    this.ready = false;
                    this.socket.close();
                } catch (IOException e) {
                }
            }
            System.out.println("[DONE] Closing listening socket");
            Thread.currentThread().stop();
        }
    }

    public void run() {
        try {
            this.socket = new ServerSocket(ip.getPort(), 64, ip.getAddress());
            System.out.format("[...] Opening listening socket on %s:%s\n",
                ip.getAddress(), ip.getPort()
            );

            while (!this.ready) {
                try {
                    Thread.sleep(30);
                } catch (InterruptedException e) {
                    // pass
                }
            }

            while (true) {
                Socket conn = socket.accept();
                Thread t = new Thread(new PeerConnection(conn, meetup_runner));
                t.start();
            }
        } catch (IOException e) {
            // Maybe port was unavailable?
            // ...bailing out!
            if (!this.ready) {
                System.exit(0);
                return;
            }

            System.err.format("[ERR] Could not establish a persistent socket on port %d\n",
                ip.getPort());
            System.err.println("[ERR] Bailing...");
            System.exit(3);
        }

    }
}
