package is.mjuk.sockets;

import is.mjuk.sockets.meetup.MeetupRunner;
import is.mjuk.sockets.net.PeerCode;

/**
 * Networked meeting scheduling
 */
public class Scheduler {

    public static String IP_ADDRESS = "127.0.0.1";
    public static Integer PORT = 7777;
    public static Integer NUMBER_OF_PEERS = null;
    public static String FILENAME = "./schedule.txt";
    public static String PEERLIST = "./peerlist.txt";

    /**
     * List of arguments parsable by the command-line interface
     */
    private static enum CLI {
        ARG, HOSTNAME, PORT, FILENAME, PEERLIST
    }

    public static void main(String[] argv) {
        arguments_to_state(argv);

        if (NUMBER_OF_PEERS == null) {
           System.err.println("Usage: [--host <HOSTNAME>] [--port <PORT>] N");
           System.exit(1);
        }

        System.out.format("[...] Finding a common meeting time for %s participants\n",
            NUMBER_OF_PEERS
        );

        MeetupRunner ms = new MeetupRunner(NUMBER_OF_PEERS, FILENAME);
        Thread t1 = new Thread(ms);
        t1.start();

        PeerCode net = new PeerCode(IP_ADDRESS, PORT, PEERLIST, ms);
        Thread t2 = new Thread(net);
        t2.start();
    }

    /**
     * Parse the argument list to global state
     *
     * @param argv List of strings representing the incoming arguments
     */
    public static void arguments_to_state(String[] argv) {
        CLI expect = CLI.ARG;
        for (String arg : argv) {
            if (expect == CLI.ARG) {
                if (arg.equals("--host") || arg.equals("-h")) {
                    expect = CLI.HOSTNAME;
                } else if (arg.equals("--port") || arg.equals("-p")) {
                    expect = CLI.PORT;
                } else if (arg.equals("--file") || arg.equals("-f")) {
                    expect = CLI.FILENAME;
                } else if (arg.equals("--peers") || arg.equals("--peerlist")) {
                    expect = CLI.PEERLIST;
                } else if (NUMBER_OF_PEERS == null) {
                    NUMBER_OF_PEERS = Integer.parseInt(arg);
                } else {
                    System.err.format("Unknown command-line argument: %s\n", arg);
                    System.exit(1);
                }
            } else {
                if (expect == CLI.HOSTNAME) {
                    IP_ADDRESS = arg;
                } else if (expect == CLI.PORT) {
                    PORT = Integer.parseInt(arg);
                } else if (expect == CLI.FILENAME) {
                    FILENAME = arg;
                } else if (expect == CLI.PEERLIST) {
                    PEERLIST = arg;
                }
                expect = CLI.ARG;
            }
        }
    }
}
