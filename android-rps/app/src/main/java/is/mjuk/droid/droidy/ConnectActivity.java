package is.mjuk.droid.droidy;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class ConnectActivity extends AppCompatActivity {
    public final static String EXTRA_ERROR_MESSAGE = "is.mjuk.droidy.ERROR_MESSAGE";
    public final static String EXTRA_HOSTNAME = "is.mjuk.droidy.HOSTNAME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
    }

    public void pressConnect(View view) {
        EditText editText = (EditText) findViewById(R.id.connectHostname);
        String hostname = editText.getText().toString();
        if (hostname.isEmpty()) {
            String error = "Hostname cannot be empty. Please provide a hostname for the server";
            cannotConnect(error);
            return;
        }

        Intent intent = new Intent(this, TicTacToeActivity.class);
        intent.putExtra(EXTRA_HOSTNAME, hostname);
        startActivity(intent);
    }

    public void cannotConnect(String error) {
        Intent intent = new Intent(this, ErrorHandlingActivity.class);
        intent.putExtra(EXTRA_ERROR_MESSAGE, error);
        startActivity(intent);
    }
}
