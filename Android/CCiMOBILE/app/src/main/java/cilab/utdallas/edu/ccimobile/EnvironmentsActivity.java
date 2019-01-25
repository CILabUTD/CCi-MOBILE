package cilab.utdallas.edu.ccimobile;

import android.content.Context;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

public class EnvironmentsActivity extends AppCompatActivity {

    public Context this_context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_environments);
        this_context = getApplicationContext();
    }

    public void classroom(View view) {
        midToast("Classroom Environment Selected");
    }

    void midToast(String str) {
        Toast toast = Toast.makeText(this_context, str, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();
    }

    public void conversation(View view) {
        midToast("Conversational Environment Selected");
    }

    public void leisure(View view) {
        midToast("Leisure Environment Selected");
    }

    public void market(View view) {
        midToast("Market Environment Selected");
    }

    public void tv(View view) {
        midToast("TV Environment Selected");
    }

    public void driving(View view) {
        midToast("Driving Environment Selected");
    }
}
