package cilab.utdallas.edu.ccimobile;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
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
        midToast("Classroom Environment Selected", Toast.LENGTH_SHORT);
    }

    void midToast(String str, int showTime) {
        Toast toast = Toast.makeText(this_context, str, showTime);
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();
    }

    public void conversation(View view) {
        midToast("Conversational Environment Selected", Toast.LENGTH_SHORT);
    }

    public void leisure(View view) {
        midToast("Leisure Environment Selected", Toast.LENGTH_SHORT);
    }

    public void market(View view) {
        midToast("Market Environment Selected", Toast.LENGTH_SHORT);
    }

    public void tv(View view) {
        midToast("TV Environment Selected", Toast.LENGTH_SHORT);
    }

    public void driving(View view) {
        midToast("Driving Environment Selected", Toast.LENGTH_SHORT);
    }
}
