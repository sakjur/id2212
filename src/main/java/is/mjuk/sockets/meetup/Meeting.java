package is.mjuk.sockets.meetup;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;

/**
 * Storage object for a ISO 8601-like datetimestamp
 */
public class Meeting {
    private SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private Date date;

    /**
     * Create a Meeting object storing a date
     *
     * @param dt Datetime in format "2016-11-13 17:40"
     */
    public Meeting(String dt) throws ParseException {
        this.date = dateformat.parse(dt);
    }

    /**
     * Check for equality between two Meetings
     * 
     * @param m Other Meeting to check for equality to
     * @return true if equal, false otherwise
     */
    public boolean equals(Meeting m) {
        return this.date.equals(m.getDate());
    }

    /**
     * Return the datetime in a Date object
     *
     * @return Date in datetime format 
     */
    public Date getDate() {
        return this.date;
    }

    /**
     * Get the meeting in a string format
     *
     * @return Date in format yyyy-MM-dd hh:mm
     */
    public String toString() {
        return dateformat.format(date);
    }
}
