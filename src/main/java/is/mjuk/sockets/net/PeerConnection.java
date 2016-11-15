package is.mjuk.sockets.net;

import is.mjuk.sockets.meetup.Meeting;
import is.mjuk.sockets.meetup.MeetingStore;
import is.mjuk.sockets.meetup.MeetupRunner;
import java.net.Socket;
import java.text.ParseException;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Thread to take care of a single peer-connection
 */
public class PeerConnection implements Runnable {
    Socket socket;
    MeetupRunner meetup_runner;

    public PeerConnection(Socket socket, MeetupRunner meetup_runner) {
        this.socket = socket;
        this.meetup_runner = meetup_runner;
    }

    public void run() {
        ArrayList<String> lines = new ArrayList<String>();
        String[] stringarray = null;

        try {
            InputStream in = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));

            for (String s = reader.readLine(); s != null; s = reader.readLine()) {
                lines.add(s);

                if (s.length() == 0) {
                    break;
                }
            }
        } catch (IOException e) {
            // Ignore
        }

        MeetingStore m = new MeetingStore(lines.toArray(new String[lines.size()]));

        meetup_runner.add_to_mergequeue(m);
        try {
            socket.close();
        } catch (IOException e) {
            // Ignore
        }
    }
}

