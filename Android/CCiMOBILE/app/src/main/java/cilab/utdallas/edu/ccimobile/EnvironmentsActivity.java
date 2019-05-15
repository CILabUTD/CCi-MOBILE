package cilab.utdallas.edu.ccimobile;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

public class EnvironmentsActivity extends AppCompatActivity {

    public Context this_context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_environments);
        this_context = getApplicationContext();
    }

    public void classroom(View view) {
        Snackbar.make(findViewById(R.id.envs), "Classroom Environment Selected", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
    }

    public void conversation(View view) {
        Snackbar.make(findViewById(R.id.envs), "Conversational Environment Selected", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
    }

    public void leisure(View view) {
        Snackbar.make(findViewById(R.id.envs), "Leisure Environment Selected", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
    }

    public void market(View view) {
        Snackbar.make(findViewById(R.id.envs), "Market Environment Selected", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
    }

    public void tv(View view) {
        Snackbar.make(findViewById(R.id.envs), "TV Environment Selected", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
    }

    public void driving(View view) {
        Snackbar.make(findViewById(R.id.envs), "Driving Environment Selected", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
    }
}
