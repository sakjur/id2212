package is.mjuk.sockets.net;

import is.mjuk.sockets.meetup.Meeting;
import is.mjuk.sockets.meetup.MeetingStore;
import is.mjuk.sockets.meetup.MeetupRunner;
import java.net.Socket;
import java.text.ParseException;
import java.util.ArrayList;

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
        ArrayList<Meeting> al = new ArrayList<Meeting>();
        try {
            al.add(new Meeting("2016-12-24 15:00"));
            al.add(new Meeting("2017-01-18 14:34"));
        } catch (ParseException e) {
            return;
        }
        MeetingStore m = new MeetingStore(2, al);

        meetup_runner.add_to_mergequeue(m);
        socket.close();
    }
}

