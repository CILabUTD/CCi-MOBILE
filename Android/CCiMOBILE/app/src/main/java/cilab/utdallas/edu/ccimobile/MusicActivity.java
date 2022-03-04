package cilab.utdallas.edu.ccimobile;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

public class MusicActivity extends AppCompatActivity {

    public Context this_context;
    Spinner instrumentSpinner, numberSpinner, complexitySpinner;
    Button music_play_button;
    MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);
        this_context = getApplicationContext();

        instrumentSpinner = findViewById(R.id.instrument_spinner);
        ArrayAdapter<CharSequence> instrumentAdapter = ArrayAdapter.createFromResource(this, R.array.instruments_spinner, android.R.layout.simple_spinner_item);
        instrumentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        instrumentSpinner.setAdapter(instrumentAdapter);

        numberSpinner = findViewById(R.id.numberInstrumentSpinner);
        ArrayAdapter<CharSequence> numberAdapter = ArrayAdapter.createFromResource(this, R.array.numberofinstruments_spinner, android.R.layout.simple_spinner_item);
        numberAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        numberSpinner.setAdapter(numberAdapter);

        complexitySpinner = findViewById(R.id.complexitySpinner);
        ArrayAdapter<CharSequence> complexityAdapter = ArrayAdapter.createFromResource(this, R.array.complexity_spinner, android.R.layout.simple_spinner_item);
        complexityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        complexitySpinner.setAdapter(complexityAdapter);

        music_play_button = findViewById(R.id.music_button);

//        mediaPlayer = new MediaPlayer();
//        mediaPlayer.setAudioAttributes(
//                new AudioAttributes.Builder()
//                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
//                .setUsage(AudioAttributes.USAGE_MEDIA)
//                .build()
//        );

    }

    public void playMusic(View view) {
//        String filename = "clarinet_";
//
//        switch(numberSpinner.getSelectedItem().toString()) {
//            case "1":
//                filename += "1_";
//                break;
//            case "2":
//                filename += "2_";
//                break;
//        }
//
//        switch(complexitySpinner.getSelectedItem().toString()) {
//            case "Low":
//                filename += "low.wav";
//                break;
//            case "Medium":
//                filename += "medium.wav";
//                break;
//            case "High":
//                filename += "high.wav";
//                break;
//        }

        if (numberSpinner.getSelectedItem().toString().equals("1"))
            if (complexitySpinner.getSelectedItem().toString().equals("Low"))
                mediaPlayer = MediaPlayer.create(this_context, R.raw.clarinet_1_low);
            else if (complexitySpinner.getSelectedItem().toString().equals("Medium"))
                mediaPlayer = MediaPlayer.create(this_context, R.raw.clarinet_1_medium);
            else
                mediaPlayer = MediaPlayer.create(this_context, R.raw.clarinet_1_high);
        else
            if (complexitySpinner.getSelectedItem().toString().equals("Low"))
                mediaPlayer = MediaPlayer.create(this_context, R.raw.clarinet_2_low);
            else if (complexitySpinner.getSelectedItem().toString().equals("Medium"))
                mediaPlayer = MediaPlayer.create(this_context, R.raw.clarinet_2_medium);
            else
                mediaPlayer = MediaPlayer.create(this_context, R.raw.clarinet_2_high);

        mediaPlayer.start();

//        try {
//            AssetFileDescriptor afd = getAssets().openFd(filename);
//            mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
//            afd.close();
//            mediaPlayer.setOnPreparedListener((MediaPlayer.OnPreparedListener) this);
//            mediaPlayer.prepareAsync();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

//    public void onPrepared(MediaPlayer player) {
//        player.start();
//    }

}
