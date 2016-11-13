package is.mjuk.sockets.meetup;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;

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

    public boolean equals(Meeting m) {
        return this.date.equals(m.getDate());
    }

    public Date getDate() {
        return this.date;
    }

    public String toString() {
        return dateformat.format(date);
    }
}
