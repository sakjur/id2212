package is.mjuk.droid.droidy;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

public class TicTacToeActivity extends AppCompatActivity {
    private Connector connector;
    private Thread connector_t;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tic_tac_toe);

        Intent intent = getIntent();
        String hostname = intent.getStringExtra(ConnectActivity.EXTRA_HOSTNAME);
        this.connector = new Connector(hostname, this);
        this.connector_t = new Thread(this.connector);
        this.connector_t.start();
    }

    public void setMessage(String message) {
        TextView messageBox = (TextView) findViewById(R.id.gameMessage);
        messageBox.setText(message);
    }

    public void setScore(String newPlayerScore, String newComputerScore) {
        TextView scorePlayer = (TextView) findViewById(R.id.scorePlayerBoard);
        TextView scoreComputer = (TextView) findViewById(R.id.scoreComputerBoard);
        scorePlayer.setText(newPlayerScore);
        scoreComputer.setText(newComputerScore);
    }

    public void sendRock(View view) {
        this.connector.sendMove("ROCK");
    }

    public void sendPaper(View view) {
        this.connector.sendMove("PAPER");
    }

    public void sendScissor(View view) {
        this.connector.sendMove("SCISSOR");
    }

    public void goHome(View view) {
        Intent intent = new Intent(this, WelcomeScreen.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private class Connector implements Runnable {
        String hostname;
        TicTacToeActivity parent;
        LinkedBlockingQueue<String> message_queue = new LinkedBlockingQueue<String>();

        public Connector(String hostname, TicTacToeActivity parent) {
            this.hostname = hostname;
            this.parent = parent;
        }

        public void sendMove(String move) {
            if (move.equals("ROCK") || move.equals("PAPER") || move.equals("SCISSOR")) {
                this.parent.setMessage("Sent a " + move);
                this.message_queue.add(move);
            }
        }

        public void exit() {

        }

        public void run() {
            Socket conn = null;
            try {
                InetAddress addr = InetAddress.getByName(this.hostname);
                conn = new Socket(addr, 7001);
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                BufferedOutputStream out = new BufferedOutputStream(conn.getOutputStream());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        parent.setMessage("Connected to " + hostname + " and ready to play!");
                    }
                });
                while (true) {
                    String msg = this.message_queue.take() + "\n";
                    out.write(msg.getBytes("UTF-8"));
                    out.flush();
                    String input = in.readLine();
                    String[] input_a = input.split(" ");
                    final String computer_move = input_a[0];
                    final String result = input_a[1];
                    final String score_player = input_a[2];
                    final String score_computer = input_a[3];
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            parent.setScore(score_player, score_computer);
                            parent.setMessage("Computer did " + computer_move + ". " + result + "!");
                        }
                    });
                }
            } catch (IOException e) {
                System.err.println("Could not connect to server...");
                this.parent.setMessage("Could not connect to server...");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                try {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            parent.setScore("0", "0");
                            parent.setMessage("Connecting...");
                        }
                    });
                    conn.close();
                } catch (Exception e) {
                    // Pretty fine with whatever. It may crash if it wants to
                }

            }
        }
    }

}
