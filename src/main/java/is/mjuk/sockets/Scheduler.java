package is.mjuk.sockets;

import is.mjuk.sockets.meetup.MeetupRunner;

/**
 * Networked meeting scheduling
 */
public class Scheduler {

    public static String IP_ADDRESS = "127.0.0.1";
    public static String PORT = "7777";
    public static String NUMBER_OF_PEERS = null;
    public static String FILENAME = "./schedule.txt";

    /**
     * List of arguments parsable by the command-line interface
     */
    private static enum CLI {
        ARG, HOSTNAME, PORT, FILENAME
    }

    public static void main(String[] argv) {
        arguments_to_state(argv);

        if (NUMBER_OF_PEERS == null) {
           System.err.println("Usage: [--host <HOSTNAME>] [--port <PORT>] N");
           System.exit(1);
        }

        System.out.format("[...] Opening listening socket on %s:%s\n",
            IP_ADDRESS, PORT
        );

        System.out.format("[...] Finding a common meeting time for %s participants\n",
            NUMBER_OF_PEERS
        );

        MeetupRunner ms = new MeetupRunner(FILENAME);
        Thread t = new Thread(ms);
        t.start();
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
                } else if (NUMBER_OF_PEERS == null) {
                    NUMBER_OF_PEERS = arg;
                } else {
                    System.err.format("Unknown command-line argument: %s\n", arg);
                    System.exit(1);
                }
            } else {
                if (expect == CLI.HOSTNAME) {
                    IP_ADDRESS = arg;
                } else if (expect == CLI.PORT) {
                    PORT = arg;
                } else if (expect == CLI.FILENAME) {
                    FILENAME = arg;
                }
                expect = CLI.ARG;
            }
        }
    }
}