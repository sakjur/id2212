/**
 * Main class for the FISH file sharing client for ID2212
 *
 * (c) 2016-2017 Emil Tullstedt <emiltu(a)kth.se>
 */
package is.mjuk.fish;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.stream.Stream;

public class Client {
    private String path;
    private InetSocketAddress server;
    private File[] files;
    private ServerConnector conn;

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
    }

    public Client(String path, InetSocketAddress server) {
        this.path = path;
        this.server = server;
        this.conn = new ServerConnector(server);
        Thread t = new Thread(this.conn, "Server Connector");
        t.start();
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
}

