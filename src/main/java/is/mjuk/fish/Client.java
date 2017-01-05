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
    private InetSocketAddress server;
    private File[] files;
    private ServerConnector conn;
    private HashMap<String, ArrayList<InetSocketAddress>> download_pending =
        new HashMap<String, ArrayList<InetSocketAddress>>();
    private String destination = "/tmp";

    public static void main(String[] argv) {
        Integer port = 7000;

        String path = Helpers.get(argv, 0, "./fish");
        String host = Helpers.get(argv, 1, "localhost");
        try {
            port = Integer.parseInt(Helpers.get(argv, 2, "7000"));
        } catch (NumberFormatException e) {
            Helpers.print_err("Could not resolve port number",
                "Integer between 0 and 65535", argv[2]);
            System.exit(-1);
        }


        Client c = new Client(path, new InetSocketAddress(host, port));
        c.share();
        c.cli_loop();
    }

    public Client(String path, InetSocketAddress server) {
        this.path = path;
        this.server = server;
        this.conn = new ServerConnector(server, download_pending);
        Thread t = new Thread(this.conn, "Server Connector");
        t.start();
    }

    public String getDestination() {
        return this.destination;
    }

    public void share() {
        System.out.format(Helpers.CYAN + "%s" + Helpers.RESET +
                " -> " + Helpers.PURPLE + "%s:%d\n" + Helpers.RESET,
            this.path,
            server.getHostString(),
            server.getPort()
        );

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

        for (File file : files) {
            conn.enqueue("SHARE " + file.getName() + "\r\n");
        }

        PeerListener p = new PeerListener(this);
        Thread peer_t = new Thread(p, "Peer Listener");
        peer_t.start();
        int peer_port = -1;
        while (peer_port == -1) {
            peer_port = p.getPort();
        }
        conn.enqueue("PORT " + Integer.valueOf(peer_port) + "\r\n");
    }

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

        Downloader downloader = new Downloader(download_pending, this);
        Thread downloader_t = new Thread(downloader, "Downloader");
        downloader_t.start();

        while (true) {
            System.out.print("||| ");
            try {
                line = in.readLine();
                
                if (line.startsWith("exit")) {
                    this.conn.exit();
                    downloader_t.interrupt();
                    System.exit(0);
                }
                if (line.startsWith("find ")) {
                    this.conn.enqueue("FIND " + line.substring(5) + "\r\n");
                }

                if (line.startsWith("download ")) {
                    String target = line.substring(9);
                    this.conn.enqueue("FIND " + target + "\r\n");
                    this.download_pending.put(target, new ArrayList<InetSocketAddress>());
                }

                if (line.startsWith("destination ")) {
                    this.destination = line.substring(12);
                    System.out.println("New download destination " + this.destination);
                }
            } catch (IOException e) {
                Helpers.print_err("Failed parsing", e.toString());
            }
        }
    }
}

