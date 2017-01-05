package is.mjuk.fish;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

public class Downloader implements Runnable {
    private HashMap<String, ArrayList<InetSocketAddress>> download_pending;
    private Client parent;

    public Downloader(HashMap<String, ArrayList<InetSocketAddress>> download_pending,
            Client parent) {
        this.download_pending = download_pending;
        this.parent = parent;
    }

    public void run() {
        while(true) {
            for (Map.Entry<String, ArrayList<InetSocketAddress>> file : download_pending.entrySet()) {
                String filename = file.getKey();
                System.out.println("Fetching " + filename);
                for (InetSocketAddress addr : file.getValue()) {
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
                            download_pending.remove(filename);
                            break;
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
                        download_pending.remove(filename);
                        System.out.println("Downloaded " + filename);
                        socket.close();
                        break;
                    } catch (IOException e) {
                        Helpers.print_err("File Download Error", e.toString());
                    }
                }
            }
            try {
                Thread.sleep(2000); 
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}
    
