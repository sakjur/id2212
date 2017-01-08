/**
 * Main class for the FISH file sharing client for ID2212
 *
 * (c) 2016-2017 Emil Tullstedt <emiltu(a)kth.se>
 */
package is.mjuk.fish;

import java.io.File;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Stream;

public class Client {
    private String path;
    private File[] files;
    private Thread conn_t;
    private DatagramHandler conn;
    private String destination = "/tmp";
    private Downloader downloader;
    private Thread downloader_t;

    public static void main(String[] argv) {
        Integer port = 7000;

        String path = Helpers.get(argv, 0, "./fish");

        Client c = new Client(path);
        c.cli_loop();
    }

    public Client(String path) {
        this.path = path;
        this.conn = new DatagramHandler(this);
        this.conn_t = new Thread(this.conn, "Server Connector");
        this.conn_t.start();

        this.downloader = new Downloader(this);
        this.downloader_t = new Thread(downloader, "Downloader");
        this.downloader_t.start();

        System.out.format("Sharing " + Helpers.CYAN + "%s" + Helpers.RESET + "\n",
            this.path
        );
        this.share();
    }

    /**
     * Get the position where downloaded files will be stored
     */
    public String getDestination() {
        return this.destination;
    }

    /**
     * Update the list of shared files
     */
    public void share() {
        File f = new File(this.path);
        if (f.exists() != true) {
            Helpers.print_err("File does not exist",
                String.format("Could not find file %s", this.path));
            System.exit(-2);
        }
        if (f.canRead() != true) {
            Helpers.print_err("Permission denied",
                String.format("File %s is not readable", this.path));
            System.exit(-2);
        }

        this.files = this.get_filelist(f);
    }

    /**
     * Get the list of files shared by this client
     */
    public File[] getFiles() {
        return this.files;
    }

    /**
     * Gets a list of files in a directory or the file itself in an array
     * <p>
     * Filters out regular files from the directory f (or f itself if a regular
     * file) which are read-able by the current user and returns them in a list
     * of {@link java.io.File} objects
     *
     * @param f Location of the input file or directory
     * @return Array of regular files within the directory pointed to by f or
     * an array containing only f if f is a regular file
     */
    private static File[] get_filelist(File f) {
        File[] files;
        if (f.isDirectory()) {
            Stream<File> file_stream = Arrays.stream(f.listFiles())
                .filter(x -> x.isFile())
                .filter(x -> x.canRead());
            files = file_stream.toArray(File[]::new);
        } else {
            files = new File[]{f};
        }
        return files;
    }

    private void cli_loop() {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String line;

        PeerListener p = new PeerListener(this);
        Thread peer_t = new Thread(p, "Peer Listener");
        peer_t.start();
        int peer_port = -1;
        while (peer_port == -1) {
            peer_port = p.getPort();
        }

        this.conn.setPort(peer_port);
        UnicastListener response_server = new UnicastListener(peer_port, this.downloader);
        Thread response_server_t = new Thread(response_server, "Unicast Server");
        response_server_t.start();

        while (true) {
            System.out.print("||| ");
            try {
                line = in.readLine();

                if (line.startsWith("exit")) {
                    this.conn_t.interrupt();
                    downloader_t.interrupt();
                    response_server_t.interrupt();
                    peer_t.interrupt();
                    System.exit(0);
                }
                if (line.startsWith("find ")) {
                    this.conn.enqueue("FIND " + String.valueOf(peer_port) + " " +
                            line.substring(5) + "\r\n");
                }

                if (line.startsWith("download ")) {
                    String target = line.substring(9);
                    this.conn.enqueue("FIND " + String.valueOf(peer_port) + " " +
                            target + "\r\n");
                    this.downloader.add_file(target);
                }

                if (line.startsWith("destination ")) {
                    this.destination = line.substring(12);
                    System.out.println("New download destination " + this.destination);
                }

                if (line.equals("destination")) {
                    System.out.println("Download destination " + this.destination);
                }
            } catch (IOException e) {
                Helpers.print_err("Failed parsing", e.toString());
            }
        }
    }
}

