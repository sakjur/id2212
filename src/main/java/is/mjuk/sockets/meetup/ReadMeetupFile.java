package is.mjuk.sockets.meetup;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;

public class ReadMeetupFile {

    private ArrayList<Meeting> meeting_points = new ArrayList<Meeting>();

    public ReadMeetupFile(String filename) throws FileNotFoundException {
        String line;
        BufferedReader buff;
        try {
            buff = new BufferedReader(new FileReader(filename));

            while ((line = buff.readLine()) != null) {
                if (line.length() >= "2016-11-13 13:10".length()) {
                    meeting_points.add(new Meeting(line));
                }
            }

            buff.close();
        } catch (FileNotFoundException e) {
            System.err.format("[ERR] file not found: %s\n", filename);
            System.exit(2);
        } catch (IOException e) {
            System.err.format("[ERR] i/o error: %s\n", e);
            System.exit(2);
        } catch (ParseException e) {
            System.err.format("[ERR] Error parsing file\n");
            System.exit(2);
        }
    }

    public ArrayList<Meeting> getDatetimes() {
        return this.meeting_points;
    }

}
