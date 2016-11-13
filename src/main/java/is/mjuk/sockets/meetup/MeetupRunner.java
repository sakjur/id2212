package is.mjuk.sockets.meetup;
import java.util.ArrayList;
import java.io.FileNotFoundException;
import java.text.ParseException;

public class MeetupRunner implements Runnable {
    private String filename;
    private MeetingStore store;
    private boolean flag = false;

    public MeetupRunner(String filename) {
        this.filename = filename;
    }

    public void run() {
        System.out.format("[...] Reading file %s\n", this.filename);
        FileReader fr;
        try {
            fr = new FileReader(this.filename);
        } catch (FileNotFoundException e) {
            System.err.format("[ERR] File not found %s\n", this.filename);
            return;
        }
        System.out.format("[DONE] Read file %s\n", this.filename);


        this.store = new MeetingStore(1, fr.getDatetimes());
        flag = true;
        this.print_meeting_times();

        System.out.println("MERGING WITH 2016-11-13 13:07");

        ArrayList<Meeting> al = new ArrayList<Meeting>();
        ArrayList<Meeting> al2 = new ArrayList<Meeting>();
        try {
            al.add(new Meeting("2016-12-24 15:00"));
            al.add(new Meeting("2017-01-18 14:34"));
            al2.add(new Meeting("2017-01-18 14:34"));
        } catch (ParseException e) {
            return;
        }

        MeetingStore m = new MeetingStore(3, al2);
        m.merge(2, al);

        this.store.merge(2, al);
        this.store.merge(m);
        this.print_meeting_times();

        for (long n : this.store.getIds()) {
            System.out.println(n);
        }
    }

    public void print_meeting_times() {
        for (Meeting m : this.store.getMeetings()) {
            System.out.println(m.toString());
        }
    }
}
