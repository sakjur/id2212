package is.mjuk.sockets.meetup;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;
import java.util.NoSuchElementException;

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
    public void merge(MeetingStore other) {
        if (this.meeting_points.size() == 0) {
            return;
        }

        boolean new_info = false;
        for (Long i : other.getIds()) {
            if (this.id.contains(i)) {
                continue;
            } else {
                new_info = true;
                break;
            }
        }
        if (!new_info) {
            return;
        }

        Iterator<Meeting> selfit = this.meeting_points.iterator();
        Iterator<Meeting> otherit = other.getMeetings().iterator();

        Meeting self_head = selfit.next();
        Meeting other_head = otherit.next();

        ArrayList<Meeting> common = new ArrayList<Meeting>();

        while (true) {
            try {
                if (self_head.equals(other_head)) {
                    /* Date exists in both lists, store and move on */
                    common.add(self_head);
                    self_head = selfit.next();
                    other_head = otherit.next();
                } else if (self_head.getDate().before(other_head.getDate())) {
                    /* Drop current head on existing list */
                    self_head = selfit.next();
                } else {
                    /* Drop current head on merging list */
                    other_head = otherit.next();
                }
            } catch (NoSuchElementException e) {
                break;
            }
        }

        this.meeting_points = common;
        this.id.addAll(other.getIds());
    }

    public void merge(long id, ArrayList<Meeting> others) {
        merge(new MeetingStore(id, others));
    }

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
