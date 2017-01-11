package is.mjuk.droid.droidy;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class ErrorHandlingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(this.getResources().getColor(R.color.colorErrorDark));
        window.setNavigationBarColor(this.getResources().getColor(R.color.colorErrorDark));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error_handling);

        Intent intent = getIntent();
        TextView textView = (TextView) findViewById(R.id.errorMessage);
        String message = intent.getStringExtra(ConnectActivity.EXTRA_ERROR_MESSAGE);
        textView.setText(message);
    }

    public void goHome(View view) {
        Intent intent = new Intent(this, WelcomeScreen.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
