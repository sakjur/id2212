package is.mjuk.sockets.meetup;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;

public class FileReader {

    private ArrayList<Meeting> meeting_points = new ArrayList<Meeting>();

    public FileReader(String filename) throws FileNotFoundException {
        StringBuilder sb = new StringBuilder();
        int next_char;
        FileInputStream in;
        try {
            in = new FileInputStream(filename);

            while ((next_char = in.read()) != -1) {
                sb.append((char) next_char);
                if (sb.length() == "2016-11-13 13:10\n".length()) {
                    meeting_points.add(new Meeting(sb.toString()));
                    sb = new StringBuilder();
                }
            }

            in.close();
        } catch (FileNotFoundException e) {
            throw e;
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
