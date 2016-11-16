package is.mjuk.sockets.meetup;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;
import java.util.NoSuchElementException;
import java.text.ParseException;

/**
* Stores a list of appointment times and the id:s used to generate the
* meetings
*/
public class MeetingStore {
    private ArrayList<Meeting> meeting_points;
    private Set<Long> id = new TreeSet<Long>();

    /**
     * Create a meeting store from a single id
     */
    public MeetingStore(long id, ArrayList<Meeting> meeting_points) {
        this.id.add(id);
        this.meeting_points = meeting_points;
    }

    /**
     * Create an empty meeting store
     */
    public MeetingStore(long id) {
        this.id.add(id);
        this.meeting_points = new ArrayList<Meeting>();
    }

    /**
     * Create a meeting store from a existing set of ids
     */
    public MeetingStore(Set<Long> id, ArrayList<Meeting> meeting_points) {
        this.id = id;
        this.meeting_points = meeting_points;
    }

    /**
     * Read netformat
     */
    public MeetingStore(String[] lines) {
        boolean readtimes = false;
        this.meeting_points = new ArrayList<Meeting>();

        for (String line : lines) {
            if (!readtimes) {
                if (line.charAt(0) == '#') {
                    readtimes = true;
                } else {
                    this.id.add(Long.parseLong(line));
                }
            } else {
                if (line.length() >= "2016-11-13 13:10".length()) {
                    try {
                        meeting_points.add(new Meeting(line));
                    } catch (ParseException e) {
                        // Skip
                    }
                }
            }
        }
    }

    /**
     * Merge this (sorted) MeetingStore removing elements not present in both lists
     * in O(n) (where n is the number of elements in the larger of the MeetingStores)
     *
     * @param other The target meeting store to combine with this
     */
    public synchronized void merge(MeetingStore other) {
        if (other == null) {
            return;
        }

        if (this.meeting_points.size() == 0) {
            return;
        }
    
        Iterator<Meeting> orig = this.meeting_points.iterator();
        Iterator<Meeting> target = other.getMeetings().iterator();

        Meeting self_head = null;
        Meeting other_head = null;
        try {
            self_head = orig.next();
            other_head = target.next();
        } catch (NoSuchElementException e) {
            this.meeting_points = new ArrayList<Meeting>();
            this.id.addAll(other.getIds());
            return;
        }

        ArrayList<Meeting> common = new ArrayList<Meeting>();

        /* For every iteration, either of the lists in the set (or both) are
         * strictly stronger, and thus this loop will eventually terminate in
         * O(N+M) */
        while (true) {
            try {
                if (self_head.equals(other_head)) {
                    /* Date exists in both lists, store and move on */
                    common.add(self_head);
                    self_head = orig.next();
                    other_head = target.next();
                } else if (self_head.getDate().before(other_head.getDate())) {
                    /* Drop current head on existing list */
                    self_head = orig.next();
                } else {
                    /* Drop current head on merging list */
                    other_head = target.next();
                }
            } catch (NoSuchElementException e) {
                break;
            }
        }

        this.meeting_points = common;
        this.id.addAll(other.getIds());
    }

    /**
     * Merge based on a temporary meeting store object
     */
    public synchronized void merge(long id, ArrayList<Meeting> others) {
        merge(new MeetingStore(id, others));
    }

    /**
     * Converts the current meeting store to a network transferable entity
     * 
     * @return Network transferable representation of the object
     */
    public String netformat() {
        StringBuilder sb = new StringBuilder();

        Set<Long> ids = this.id;
        ArrayList<Meeting> meetings = this.meeting_points;

        for (Long id : ids) {
            sb.append(id);
            sb.append("\n");
        }

        sb.append("#\n");

        for (Meeting m : meetings) {
            sb.append(m.toString());
            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * Return the set of IDs which are applied to the current list of meetings
     *
     * @return IDs merged into the MeetingStore
     */
    public Set<Long> getIds() {
        return this.id;
    }

    /**
     * Return a list of Meeting with the current available times
     *
     * @return List of available times
     */
    public ArrayList<Meeting> getMeetings() {
        return this.meeting_points;
    }
}
