package is.mjuk.sockets.meetup;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;
import java.util.NoSuchElementException;

/**
* Stores a list of appointment times and the id:s used to generate the
* meetings
*/
public class MeetingStore {
    private ArrayList<Meeting> meeting_points;
    private Set<Long> id = new TreeSet<Long>();

    public MeetingStore(long id, ArrayList<Meeting> meeting_points) {
        this.id.add(id);
        this.meeting_points = meeting_points;
    }

    /**
     * Merge this (sorted) MeetingStore removing elements not present in both lists
     * in O(n) (where n is the number of elements in the larger of the MeetingStores)
     *
     * @param other The target meeting store to combine with this
     */
    public synchronized void merge(MeetingStore other) {
        if (this.meeting_points.size() == 0) {
            return;
        }

        boolean new_info = false;
        for (Long i : other.getIds()) {
            if (!this.id.contains(i)) {
                new_info = true;
                break;
            }
        }
        if (!new_info) {
            return;
        }

        Iterator<Meeting> orig = this.meeting_points.iterator();
        Iterator<Meeting> target = other.getMeetings().iterator();

        Meeting self_head = orig.next();
        Meeting other_head = target.next();

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

    public synchronized void merge(long id, ArrayList<Meeting> others) {
        merge(new MeetingStore(id, others));
    }

    /**
     * Converts the current meeting store to a network transferable entity
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

    public Set<Long> getIds() {
        return this.id;
    }

    public ArrayList<Meeting> getMeetings() {
        return this.meeting_points;
    }
}
