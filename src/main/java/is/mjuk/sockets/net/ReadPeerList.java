package is.mjuk.sockets.net;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.text.ParseException;
import java.util.ArrayList;

public class ReadPeerList {
    public static ArrayList<HostInfo> read(String filename) {
        String line;
        ArrayList<HostInfo> peers = new ArrayList<HostInfo>();

        try {
            BufferedReader buff = new BufferedReader(new FileReader(filename));

            while ((line = buff.readLine()) != null) {
                String[] ipport = line.split(" ");
                if (ipport.length >= 2) {
                    peers.add(new HostInfo(new InetSocketAddress(
                        InetAddress.getByName(ipport[0]),
                        Integer.parseInt(ipport[1])
                    )));
                }
            }

            buff.close();
        } catch (FileNotFoundException e) {
            System.err.format("[ERR] file not found: %s\n", filename);
            System.exit(2);
        } catch (IOException e) {
            System.err.format("[ERR] i/o error: %s\n", e);
            System.exit(2);
        }

        return peers;
    }
}

