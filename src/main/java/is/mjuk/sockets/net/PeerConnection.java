package is.mjuk.sockets.net;

import is.mjuk.sockets.meetup.Meeting;
import is.mjuk.sockets.meetup.MeetingStore;
import is.mjuk.sockets.meetup.MeetupRunner;
import java.net.Socket;
import java.text.ParseException;
import java.util.ArrayList;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Thread to take care of a single peer-connection
 */
public class PeerConnection implements Runnable {
    private Socket socket;
    private MeetupRunner meetup_runner;
    private PeerCode parent;

    private enum ClientState {
        WAITING,
        DATA 
    }

    public PeerConnection(PeerCode parent, Socket socket, MeetupRunner meetup_runner) {
        this.parent = parent;
        this.socket = socket;
        this.meetup_runner = meetup_runner;
    }

    public void run() {
        ArrayList<String> lines = new ArrayList<String>();
        String[] stringarray = null;
        ClientState state = ClientState.WAITING;

        try {
            InputStream in = socket.getInputStream();
            BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));

            for (String s = reader.readLine(); s != null; s = reader.readLine()) {
                if (s.equals("DATA")) {
                    state = ClientState.DATA;
                    out.write(meetup_runner.netformat().getBytes());
                    out.flush();
                    continue;
                } else if (s.equals("DONE")) {
                    state = ClientState.DATA;
                    continue;
                }

                if (state == ClientState.DATA) {
                    lines.add(s);
                }

                if (s.length() == 0) {
                    break;
                }
            }

            if (state == ClientState.DATA) {
                MeetingStore m = new MeetingStore(lines.toArray(new String[lines.size()]));
                meetup_runner.add_to_mergequeue(m);
            }

            socket.close();
        } catch (IOException e) {
            // Ignore
        }
    }
}

