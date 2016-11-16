package is.mjuk.sockets.net;
import is.mjuk.sockets.meetup.MeetupRunner;
import is.mjuk.sockets.meetup.MeetingStore;
import java.net.Socket;
import java.util.ArrayList;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MeetupClient implements Runnable {
    private ArrayList<HostInfo> peers;
    private MeetupRunner meetup_runner;
    private PeerCode parent;

    public MeetupClient(PeerCode parent, ArrayList<HostInfo> peers, MeetupRunner meetup_runner) {
        this.parent = parent;
        this.peers = peers;
        this.meetup_runner = meetup_runner;
    }

    public void run () {
        ArrayList<String> lines = new ArrayList<String>();
        while(true) {
            for (HostInfo peer : peers) {
                try {
                    Socket socket = new Socket(peer.address.getHostName(), peer.address.getPort());
                    InputStream in = socket.getInputStream();
                    BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));

                    if (parent.getState() == PeerState.READY) {
                        out.write("DATA\n\n".getBytes());
                        out.flush();
                        for (String s = reader.readLine(); s != null; s = reader.readLine()) {
                            lines.add(s);

                            if (s.length() == 0) {
                                break;
                            }
                        }

                        MeetingStore m = new MeetingStore(lines.toArray(new String[lines.size()]));
                        meetup_runner.add_to_mergequeue(m);
                    }

                    if (parent.getState() == PeerState.DONE) {
                        out.write("DONE\n".getBytes());
                        out.write(meetup_runner.netformat().getBytes());
                        out.flush();
                    }

                    socket.close();
                } catch (IOException e) {
                    continue;
                }
            }
            try {
                Thread.sleep(40);
            } catch (InterruptedException e) {
                // Ignore
            }
        }
    }

}

