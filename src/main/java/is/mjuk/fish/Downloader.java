package is.mjuk.fish;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

/**
 * Thread which download requested files in the background
 */
public class Downloader implements Runnable {
    private Client parent;
    private LinkedBlockingQueue<String> hoststrings = new LinkedBlockingQueue<String>();
    private TreeSet<String> download_targets = new TreeSet<String>();

    /**
     * @param parent The {@link is.mjuk.fish.Client} that initialized this
     * downloader
     */
    public Downloader(Client parent) {
        this.parent = parent;
    }

    public void add_file(String filename) {
        download_targets.add(filename);
    }

    public void add_hoststring(String hoststring) {
        try {
            this.hoststrings.put(hoststring);
        } catch (InterruptedException e) {
            // Ignored on purpose. I don't really *care* about edge-cases or
            // race conditions in this application.
        }
    }

    public void run() {
        String[] s_data;
        while(true) {
            try {
                s_data = hoststrings.take().trim().split(" ");
            } catch (InterruptedException e) {
                return;
            }
            String hostname = s_data[0];
            Integer port = Integer.valueOf(s_data[1]);
            String filename = String.join(" ",
                Arrays.copyOfRange(s_data, 2, s_data.length));

            if (download_targets.contains(filename) == false) {
                continue;
            }

            InetSocketAddress addr = new InetSocketAddress(hostname, port);
            Socket socket = new Socket();
            InputStream in;
            BufferedOutputStream out;
            FileOutputStream out_file;
            try {
                socket.connect(addr, 1500);
                in = socket.getInputStream();
                out = new BufferedOutputStream(socket.getOutputStream());
                File file_dest = new File(this.parent.getDestination() + "/" + filename);
                if (!file_dest.createNewFile()) {
                    Helpers.print_err("Couldn't create file",
                            "File already exists or can't be created");
                    download_targets.remove(filename);
                    continue;
                }
                out_file = new FileOutputStream(file_dest);

                String download_cmd = "DOWNLOAD " + filename + "\r\n";
                out.write(download_cmd.getBytes());
                out.flush();

                byte[] status = new byte[5];
                if (in.read(status) != 5) {
                    Helpers.print_err("Reading status failed",
                            "Host: " + addr.getHostName());
                    continue;
                };

                if (new String(status, "UTF-8").equals("E_DNF")) {
                    Helpers.print_err("Could not find file on host",
                            "Host: " + addr.getHostName());
                    continue;
                }

                int read = 0;
                byte[] bytes = new byte[4096];
                while((read = in.read(bytes)) != -1) {
                    out_file.write(bytes, 0, read);
                }
                download_targets.remove(filename);
                this.parent.share();
                System.out.println("Downloaded " + filename);
                socket.close();
            } catch (IOException e) {
                Helpers.print_err("File Download Error", e.toString());
            }
        }
    }
}

