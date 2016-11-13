package is.mjuk.sockets.meetup;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import org.junit.Test;
import java.text.ParseException;

public class MeetupTest {

    @Test
    public void ParseMeeting() throws ParseException {
        Meeting m = new Meeting("2016-11-12 18:11");
        assertTrue("Input: 2016-11-12 18:11", m.toString().equals("2016-11-12 18:11"));
        String base = "1994-02-26 14:45";
        Meeting n = new Meeting(base);
        assertTrue(String.format("Input: %s", base), base.toString().equals(base));
    }

    @Test
    public void CompareToSelf() throws ParseException {
        Meeting m = new Meeting("1994-02-26 14:45");
        assertTrue("Equal to self", m.equals(m));
    }

    @Test
    public void CompareToMeeting() throws ParseException {
        String base = "1994-02-26 14:45";
        Meeting m = new Meeting(base);
        Meeting n = new Meeting(base);
        Meeting o = new Meeting("2016-11-12 18:11");
        Meeting p = new Meeting("1994-02-26 18:11");
        Meeting q = new Meeting("2016-11-12 14:45");
        assertTrue("Equal meetings", m.equals(n));
        assertFalse("Inequal meetings", m.equals(o));
        assertFalse("Inequal meetings: Wrong time, correct date", m.equals(p));
        assertFalse("Inequal meetings: Right time, incorrect date", m.equals(q));
    }

}
