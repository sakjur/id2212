package is.mjuk.sockets.net;

import is.mjuk.sockets.meetup.MeetupCallbackInterface;
import is.mjuk.sockets.meetup.MeetupRunner;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class PeerCode implements Runnable, MeetupCallbackInterface {
    private PeerState state = PeerState.WAITING;
    private InetSocketAddress ip;
    private ServerSocket socket;
    private MeetupRunner meetup_runner;
    private String peerlist;

    private ArrayList<HostInfo> peers;

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
            this.state = PeerState.READY;
        } else if (cb == MeetupRunner.CallbackType.DONE) {
            this.state = PeerState.DONE;
        }
    }

    public PeerState getState() {
        return this.state;
    }

    public void run() {
        this.peers = ReadPeerList.read(this.peerlist);

        try {
            this.socket = new ServerSocket(ip.getPort(), 64, ip.getAddress());
            System.out.format("[...] Opening listening socket on %s:%s\n",
                ip.getAddress(), ip.getPort()
            );

            while (this.state == PeerState.WAITING) {
                try {
                    Thread.sleep(30);
                } catch (InterruptedException e) {
                    // pass
                }
            }

            for (HostInfo peer : this.peers) {
                Thread tc = new Thread(new MeetupClient(this, peer, this.meetup_runner));
                tc.start();
            }

            while (true) {
                Socket conn = socket.accept();
                Thread t = new Thread(new PeerConnection(this, conn, meetup_runner));
                t.start();
            }
        } catch (IOException e) {
            // Maybe port was unavailable?
            // ...bailing out!
            if (this.state == PeerState.DONE) {
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
