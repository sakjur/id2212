package is.mjuk.sockets.meetup;
import java.util.LinkedList;
import java.util.concurrent.ThreadLocalRandom;
import java.io.FileNotFoundException;
import java.text.ParseException;

public class MeetupRunner implements Runnable {
    public static enum CallbackType {
        READY, DONE
    };

    private String filename;
    private int peers;
    private MeetingStore store;
    private LinkedList<MeetingStore> mergequeue = new LinkedList<MeetingStore>();
    private LinkedList<MeetupCallbackInterface> callbackqueue =
        new LinkedList<MeetupCallbackInterface>();
    private LinkedList<MeetupCallbackInterface> callbacklisteners =
        new LinkedList<MeetupCallbackInterface>();
    private long id;

    public MeetupRunner(int peers, String filename) {
        this.filename = filename;
        this.peers = peers;
    }

    public void run() {
        this.id = ThreadLocalRandom.current().nextLong();
        System.out.format("[...] Reading file %s\n", this.filename);
        ReadMeetupFile fr;
        try {
            fr = new ReadMeetupFile(this.filename);
        } catch (FileNotFoundException e) {
            System.err.format("[ERR] File not found %s\n", this.filename);
            return;
        }
        System.out.format("[DONE] Read file %s\n", this.filename);


        this.store = new MeetingStore(id, fr.getDatetimes());

        while (true) {
            MeetingStore head = this.mergequeue.poll();
            MeetupCallbackInterface callback_head = this.callbackqueue.poll();

            if (this.store.getIds().size() == peers ||
                    this.store.getMeetings().size() == 0) {
                System.out.format("[DONE] Found common meeting times for %s participants\n",
                    peers);
                this.print_meeting_times();

                for (MeetupCallbackInterface listener : callbacklisteners) {
                    listener.MeetupCallback(CallbackType.DONE);
                }

                break;
            }

            if (callback_head != null) {
                callback_head.MeetupCallback(CallbackType.READY);
                callbacklisteners.add(callback_head);
                callback_head = null;
            }

            if (head == null) {
                // TODO: Request from the net stack
                try {
                    Thread.sleep(30);
                } catch (InterruptedException e) {
                    // pass
                }
            } else {
                this.store.merge(head);
            }
        }
    }

    public void add_to_mergequeue(MeetingStore m) {
        this.mergequeue.add(m);
    }

    public void add_to_callbackqueue(MeetupCallbackInterface o) {
        this.callbackqueue.add(o);
    }

    public void set_state_empty() {
        this.mergequeue.add(new MeetingStore(0));
    }

    public long getId() {
        return this.id;
    }

    public void print_meeting_times() {
        if (this.store.getMeetings().size() == 0) {
            System.out.println("No common times found :(");
        }
        // System.out.println(this.store.netformat());
        for (Meeting m : this.store.getMeetings()) {
            System.out.println(m.toString());
        }
    }
}
