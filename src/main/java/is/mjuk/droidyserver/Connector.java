package is.mjuk.droidyserver;

import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.ThreadLocalRandom;

public class Connector implements Runnable {
    private Socket conn;
    private Integer player_score = 0;
    private Integer computer_score = 0;

    public Connector(Socket conn) {
        this.conn = conn;
        System.out.println("New connection established!");
    }

    public void run() {
        try {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(this.conn.getInputStream()));
            PrintWriter out =
                new PrintWriter(this.conn.getOutputStream(), true);

                String line;
                String output;

                while ((line = in.readLine()) != null) {
                    if (line.startsWith("EXIT")) {
                        this.conn.close();
                        break;
                    }
                    System.out.println(line);
                    output = this.processData(line);
                    out.println(output);
                }
        } catch (IOException e) {
            System.err.println("Something went wrong in the client connector");
        }
    }

    private enum Winner {
        LEFT,
        RIGHT,
        DRAW
    }

    private enum RPS {
        ROCK,
        PAPER,
        SCISSOR
    }

    private String processData(String input) {
        int move = ThreadLocalRandom.current().nextInt(3);
        RPS cpu_move = RPS.ROCK;
        if (move == 1) {
            cpu_move = RPS.PAPER;
        } else if (move == 2) {
            cpu_move = RPS.SCISSOR;
        }
        String cpu_move_s = moveToString(cpu_move);

        RPS player_move = stringToMove(input);
        String winner = processMove(player_move, cpu_move);

        if (winner.equals("PLAYER")) {
            this.player_score = this.player_score + 1;
        }
        if (winner.equals("COMPUTER")) {
            this.computer_score = this.computer_score + 1;
        }

        return cpu_move_s + " " + winner + " " +
            Integer.toString(player_score) + " " +
            Integer.toString(computer_score); 
    }

    private String moveToString(RPS move) {
        if (move == RPS.ROCK) {
            return "ROCK";
        } else if (move == RPS.PAPER) {
            return "PAPER";
        } else {
            return "SCISSOR";
        }
    }

    private String processMove(RPS l, RPS r) {
        if (l == r) {
            return "DRAW"; 
        } else if (l == RPS.ROCK && r == RPS.SCISSOR) {
            return "PLAYER";
        } else if (l == RPS.PAPER && r == RPS.ROCK) {
            return "PLAYER";
        } else if (l == RPS.SCISSOR && r == RPS.PAPER) {
            return "PLAYER";
        }
        return "COMPUTER";
    }

    private RPS stringToMove(String input) {
        if (input.startsWith("R")) {
            return RPS.ROCK;
        } else if (input.startsWith("P")) {
            return RPS.PAPER;
        } else {
            return RPS.SCISSOR;
        }
    }
}
