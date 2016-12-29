/**
 * Main class for the FISH file sharing client for ID2212
 *
 * (c) 2016-2017 Emil Tullstedt <emiltu(a)kth.se>
 */
package is.mjuk.fish;

import java.lang.NumberFormatException;
import java.io.File;
import java.net.InetSocketAddress;

public class Client {
    private String path;
    private InetSocketAddress server;

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
        c.run();
    }

    public Client(String path, InetSocketAddress server) {
        this.path = path;
        this.server = server;
    }

    public void run() {
        System.out.format("%s -> %s:%d\n",
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
        }
        if (f.isDirectory()) {
            // TODO Create a structure of the containing files if directory,
            // otherwise create a structure with a single file
        }
    }

}

