package is.mjuk.sockets.meetup;
import java.util.LinkedList;
import java.io.FileNotFoundException;
import java.text.ParseException;

public class MeetupRunner implements Runnable {
    private String filename;
    private int peers;
    private MeetingStore store;
    private LinkedList<MeetingStore> mergequeue = new LinkedList<MeetingStore>();

    public MeetupRunner(int peers, String filename) {
        this.filename = filename;
        this.peers = peers;
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

        while (true) {
            MeetingStore head = this.mergequeue.poll();

            if (this.store.getIds().size() == peers ||
                    this.store.getMeetings().size() == 0) {
                System.out.format("[DONE] Found common meeting times for %s participants\n",
                    peers);
                this.print_meeting_times();
                return;
            }

            if (head == null) {
                continue;
            } else {
                this.store.merge(head);
            }
        }
    }

    public void add_to_mergequeue(MeetingStore m) {
        this.mergequeue.add(m);
    }

    public void print_meeting_times() {
        if (this.store.getMeetings().size() == 0) {
            System.out.println("No matching times found :(\n");
        }
        for (Meeting m : this.store.getMeetings()) {
            System.out.println(m.toString());
        }
    }
}
