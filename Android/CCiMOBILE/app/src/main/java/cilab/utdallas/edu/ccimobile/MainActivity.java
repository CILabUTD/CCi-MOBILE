package cilab.utdallas.edu.ccimobile;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.ftdi.j2xx.D2xxManager;
import com.ftdi.j2xx.FT_Device;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

public class MainActivity extends AppCompatActivity implements InitializationResultReceiver.Receiver {

    boolean folderExists;
    boolean settingsMono, settingsLeft, settingsRight, settingsStereo;
    int disabledAlpha = 38; //color for sliders (grey-disabled)

    private InitializationResultReceiver mReceiver;

    public static D2xxManager ftD2xx = null;
    FT_Device ft_device_0;
    FT_Device ft_device_1;
    FT_Device ftDev; //ftDev
    int DevCount = -1;
    int currentPortIndex = -1;
    int portIndex = -1;
    Queue<Integer> availQ;
    readThread readThread1;

    byte[] writeBuffer, nullwriteBuffer, sineBuffer;

    int baudRate = 5000000; // baud rate /// //460800; 921600; //5000000
    byte stopBit = (byte)2; // 1:1stop bits, 2:2 stop bits //
    byte dataBit = (byte)8; // 8:8bit, 7: 7bit //
    byte parity = (byte)0; // 0: none, 1: odd, 2: even, 3: mark, 4: space //
    byte flowControl = (byte)1; // 0:none, 1: CTS/RTS, 2:DTR/DSR, 3:XOFF/XON //
    public Context global_context;
    boolean uart_configured = false;

    final byte XON = 0x11;    // Resume transmission //
    final byte XOFF = 0x13;    // Pause transmission //

    private boolean start = false;
    short sine_stim[];
    short sin_token[]; short null_token[];
    short sine_token[];

    public MAP leftMAP, rightMAP;
    private ACE leftACE, rightACE;

    private double leftScaleFactor, rightScaleFactor;

    Stimuli stimuli;
    Stimuli leftStimuli, rightStimuli;
    D2xxManager.DriverParameters s;

    TextView status, connectionStatus, leftSensitivity, rightSensitivity, leftGain, rightGain;
    ImageView statusImage;
    ToggleButton buttonStartStop;
    SeekBar seekBarLeftSensitivity, seekBarRightSensitivity, seekBarLeftGain, seekBarRightGain;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        global_context = this;
        verifyStoragePermissions(this);
        verifyFolderExists();
        new VeryLongAsyncTask(this).execute();

        status = (TextView) findViewById(R.id.textStatus) ;
        initialize();
        callInitializationService();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_activity, menu);
        return true;
    }

    void verifyFolderExists() {
        // Create a folder in phone storage if one doesn't already exist
        File folder = new File(Environment.getExternalStorageDirectory() +
                File.separator + "CCiMOBILE_files");
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdirs();
        }
        folderExists = success;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuSettings:
                // User chose the "Settings" item, show the app settings UI...
                Intent myIntent = new Intent(MainActivity.this, SettingsActivity.class);
                MainActivity.this.startActivityForResult(myIntent, 1);
                return true;
            case R.id.menuEnvironments:
                // User chose the "Settings" item, show the app settings UI...
                myIntent = new Intent(MainActivity.this, EnvironmentsActivity.class);
                MainActivity.this.startActivityForResult(myIntent, 1);
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                if (leftMAP.exists) {
                    leftMAP.sensitivity = data.getDoubleExtra("leftSensitivity", 0);
                    if (leftMAP.sensitivity > 10) {
                        leftMAP.sensitivity = 10;
                    } else if (leftMAP.sensitivity < 0) {
                        leftMAP.sensitivity = 0;
                    }
                    leftSensitivity.setText("Left Sensitivity: " + leftMAP.sensitivity);
                    seekBarLeftSensitivity.setProgress((int) leftMAP.sensitivity * 10);

                    leftMAP.gain = data.getDoubleExtra("leftGain", 0);
                    if (leftMAP.gain > 50) {
                        leftMAP.gain = 50;
                    } else if (leftMAP.gain < 0) {
                        leftMAP.gain = 0;
                    }
                    leftGain.setText("Left Gain: " + leftMAP.gain + " dB");
                    seekBarLeftGain.setProgress((int) leftMAP.gain);

                    leftMAP.implantGeneration = data.getStringExtra("leftMAPimplantGeneration");
                    leftMAP.stimulationModeCode = data.getIntExtra("leftMAPstimulationModeCode",0);
                    leftMAP.pulsesPerFramePerChannel = data.getIntExtra("leftMAPpulsesPerFramePerChannel",0);
                    leftMAP.pulsesPerFrame = data.getIntExtra("leftMAPpulsesPerFrame",0);
                    leftMAP.interpulseDuration = data.getDoubleExtra("leftMAPinterpulseDuration",0);
                    leftMAP.nRFcycles = data.getIntExtra("leftMAPnRFcycles",0);

                    // need to update pulsewidth and all the other parameters here
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                    leftMAP.stimulationRate = preferences.getInt("Left.stimulationRate", 0);
                    leftMAP.pulseWidth = preferences.getInt("Left.pulseWidth",0);
                    leftMAP.sensitivity = getDouble(preferences,"Left.sensitivity",0);
                    leftMAP.gain = getDouble(preferences,"Left.gain",0);
                    leftMAP.Qfactor = getDouble(preferences,"Left.Qfactor",0);
                    leftMAP.baseLevel = getDouble(preferences,"Left.baseLevel",0);
                    leftMAP.saturationLevel = getDouble(preferences,"Left.saturationLevel",0);
                    leftMAP.nMaxima = preferences.getInt("Left.nMaxima",0);
                    leftMAP.volume = preferences.getInt("Left.volume",0);
                    leftMAP.stimulationOrder = preferences.getString("Left.stimulationOrder","");
                    leftMAP.window = preferences.getString("Left.window","");

                    // re-initialize ACE
                    leftACE = new ACE(leftMAP);
                }
                if (rightMAP.exists) {
                    rightMAP.sensitivity = data.getDoubleExtra("rightSensitivity", 0);
                    if (rightMAP.sensitivity > 10) {
                        rightMAP.sensitivity = 10;
                    } else if (rightMAP.sensitivity < 0) {
                        rightMAP.sensitivity = 0;
                    }
                    rightSensitivity.setText("Right Sensitivity: " + rightMAP.sensitivity);
                    seekBarRightSensitivity.setProgress((int) rightMAP.sensitivity * 10);

                    rightMAP.gain = data.getDoubleExtra("rightGain", 0);
                    if (rightMAP.gain > 50) {
                        rightMAP.gain = 50;
                    } else if (rightMAP.gain < 0) {
                        rightMAP.gain = 0;
                    }
                    rightGain.setText("Left Gain: " + rightMAP.gain + " dB");
                    seekBarRightGain.setProgress((int) rightMAP.gain);

                    rightMAP.implantGeneration = data.getStringExtra("rightMAPimplantGeneration");
                    rightMAP.stimulationModeCode = data.getIntExtra("rightMAPstimulationModeCode",0);
                    rightMAP.pulsesPerFramePerChannel = data.getIntExtra("rightMAPpulsesPerFramePerChannel",0);
                    rightMAP.pulsesPerFrame = data.getIntExtra("rightMAPpulsesPerFrame",0);
                    rightMAP.interpulseDuration = data.getDoubleExtra("rightMAPinterpulseDuration",0);
                    rightMAP.nRFcycles = data.getIntExtra("rightMAPnRFcycles",0);

                    // need to update pulsewidth and all the other parameters here
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                    rightMAP.stimulationRate = preferences.getInt("Right.stimulationRate", 0);
                    rightMAP.pulseWidth = preferences.getInt("Right.pulseWidth",0);
                    rightMAP.sensitivity = getDouble(preferences,"Right.sensitivity",0);
                    rightMAP.gain = getDouble(preferences,"Right.gain",0);
                    rightMAP.Qfactor = getDouble(preferences,"Right.Qfactor",0);
                    rightMAP.baseLevel = getDouble(preferences,"Right.baseLevel",0);
                    rightMAP.saturationLevel = getDouble(preferences,"Right.saturationLevel",0);
                    rightMAP.nMaxima = preferences.getInt("Right.nMaxima",0);
                    rightMAP.volume = preferences.getInt("Right.volume",0);
                    rightMAP.stimulationOrder = preferences.getString("Right.stimulationOrder","");
                    rightMAP.window = preferences.getString("Right.window","");

                    // re-initialize ACE
                    rightACE = new ACE(rightMAP);
                }
                if (leftMAP.exists&&rightMAP.exists){ // both left and right
                    setOutputBuffer(writeBuffer, leftMAP, rightMAP); // update Output Buffer
                }
                else if ((leftMAP.exists) &&(!rightMAP.exists)) { // only left
                    setOutputBuffer(writeBuffer, leftMAP, leftMAP); // zero buffer
                }
                else if ((rightMAP.exists)&&(!leftMAP.exists)){ // only right
                    setOutputBuffer(writeBuffer, rightMAP, rightMAP); // zero buffer
                }
            }
        }
    }

    private class VeryLongAsyncTask extends AsyncTask<Void, Void, Void> {
        private final ProgressDialog progressDialog;

        VeryLongAsyncTask(Context ctx) {
            progressDialog = new CustomProgressDialog(ctx, R.style.MyTheme);
            progressDialog.setCanceledOnTouchOutside(false);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            // sleep for 7 seconds
            try {
                Thread.sleep(7000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            progressDialog.hide();
        }
    }



    private void callInitializationService() {
        mReceiver = new InitializationResultReceiver(new Handler());
        mReceiver.setReceiver(this);
        Intent intent = new Intent(Intent.ACTION_SYNC, null, this, InitializationService.class);
        /* Send optional extras to Download IntentService */
        //intent.putExtra("url", url);
        intent.putExtra("receiver", mReceiver);
        intent.putExtra("requestId", 101);
        startService(intent);
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        String results;
        switch (resultCode) {
            case InitializationService.STATUS_RUNNING:
                setProgressBarIndeterminateVisibility(true);
                break;
            case InitializationService.STATUS_FINISHED:
                /* Hide progress & extract result from bundle */
                setProgressBarIndeterminateVisibility(false);
                results = resultData.getString("result");
                String msg = "Completion Message";
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                break;
            case InitializationService.STATUS_STEP0_FINISHED:
                /* Hide progress & extract result from bundle */
                setProgressBarIndeterminateVisibility(false);
                results = resultData.getString("result");
                connectionStatus.setText("Connecting.  ");
                try {textOut(results);} catch (IOException e) {e.printStackTrace();}
                break;
            case InitializationService.STATUS_STEP1_FINISHED:
                /* Hide progress & extract result from bundle */
                setProgressBarIndeterminateVisibility(false);
                String msg1 = "Step 1 completed";
                status.append(msg1);
                connectionStatus.setText("Connecting.. ");
                break;
            case InitializationService.STATUS_STEP2_FINISHED:
                /* Hide progress & extract result from bundle */
                setProgressBarIndeterminateVisibility(false);
                String msg2 = "Step 2 completed";
                status.append(msg2);
                connectionStatus.setText("Connecting...");
                break;
            case InitializationService.STATUS_STEP3_FINISHED:
                /* Hide progress & extract result from bundle */
                setProgressBarIndeterminateVisibility(false);
                String msg3 = "Step 3 completed";
                status.setText("Ready");
                initializeConnection();
                buttonStartStop.setEnabled(true);
                buttonStartStop.setButtonDrawable(R.drawable.start);
                connectionStatus.setText("Connected");
                statusImage.setImageResource(R.drawable.connected);
                break;
            case InitializationService.STATUS_ERROR:
                /* Handle the error */
                String error = resultData.getString(Intent.EXTRA_TEXT);
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                break;
        }
    }

    private void initialize() {
        initializeMAP();
        //initializeDefaultMAP();
        initializeStimuli();
        if (leftMAP.exists) {
            leftACE = new ACE(leftMAP);
        }
        if (rightMAP.exists) {
            rightACE = new ACE(rightMAP);
        }

        initializeGUI();
    }

    private void initializeMAP() {
        leftMAP = new MAP();
        rightMAP = new MAP();

        leftMAP.getLeftMapData();
        if (leftMAP.dataMissing) {
            Toast.makeText(getApplicationContext(), "Left MAP could not be opened due to missing data.", Toast.LENGTH_LONG).show();
        }
        rightMAP.getRightMapData();
        if (rightMAP.dataMissing) {
            Toast.makeText(getApplicationContext(), "Right MAP could not be opened due to missing data.", Toast.LENGTH_LONG).show();
        }

        if (leftMAP.exists || rightMAP.exists) {
            writeMAPToPreferences();
        }
    }

    public void writeMAPToPreferences() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        if (leftMAP.exists) {
            editor.putInt("leftMAPnbands",leftMAP.nbands);
            editor.putBoolean("leftMapExists", true);
            editor.putString("Left.implantType", leftMAP.implantType);
            editor.putInt("Left.samplingFrequency", leftMAP.samplingFrequency);
            editor.putInt("Left.numberOfChannels", leftMAP.numberOfChannels);
            editor.putString("Left.frequencyTable", leftMAP.frequencyTable);
            editor.putString("Left.soundProcessingStrategy", leftMAP.soundProcessingStrategy);
            editor.putInt("Left.nMaxima", leftMAP.nMaxima);
            editor.putString("Left.stimulationMode", leftMAP.stimulationMode);
            editor.putInt("Left.stimulationRate", leftMAP.stimulationRate);
            editor.putInt("Left.pulseWidth", leftMAP.pulseWidth);
            putDouble(editor, "Left.sensitivity", leftMAP.sensitivity);
            putDouble(editor, "Left.gain", leftMAP.gain);
            editor.putInt("Left.volume", leftMAP.volume);
            putDouble(editor, "Left.Qfactor", leftMAP.Qfactor);
            putDouble(editor, "Left.baseLevel", leftMAP.baseLevel);
            putDouble(editor, "Left.saturationLevel", leftMAP.saturationLevel);
            editor.putString("Left.stimulationOrder", leftMAP.stimulationOrder);
            editor.putString("Left.window", leftMAP.window);

            // Getting electrode array
            for (int i = 0; i < leftMAP.nbands; i++ ) {
                editor.putInt("leftTHR" + i,leftMAP.THR[i]);
                editor.putInt("leftMCL" + i, leftMAP.MCL[i]);
                putDouble(editor,"leftgain" + i,leftMAP.gains[i]);
                editor.putInt("leftelectrodes" + i,leftMAP.electrodes[i]);
            }
        } else {
            editor.putBoolean("leftMapExists", false);
        }
        if (rightMAP.exists) {
            editor.putInt("rightMAPnbands",rightMAP.nbands);
            editor.putBoolean("rightMapExists", true);
            editor.putString("Right.implantType", rightMAP.implantType);
            editor.putInt("Right.samplingFrequency", rightMAP.samplingFrequency);
            editor.putInt("Right.numberOfChannels", rightMAP.numberOfChannels);
            editor.putString("Right.frequencyTable", rightMAP.frequencyTable);
            editor.putString("Right.soundProcessingStrategy", rightMAP.soundProcessingStrategy);
            editor.putInt("Right.nMaxima", rightMAP.nMaxima);
            editor.putString("Right.stimulationMode", rightMAP.stimulationMode);
            editor.putInt("Right.stimulationRate", rightMAP.stimulationRate);
            editor.putInt("Right.pulseWidth", rightMAP.pulseWidth);
            putDouble(editor, "Right.sensitivity", rightMAP.sensitivity);
            putDouble(editor, "Right.gain", rightMAP.gain);
            editor.putInt("Right.volume", rightMAP.volume);
            putDouble(editor, "Right.Qfactor", rightMAP.Qfactor);
            putDouble(editor, "Right.baseLevel", rightMAP.baseLevel);
            putDouble(editor, "Right.saturationLevel", rightMAP.saturationLevel);
            editor.putString("Right.stimulationOrder", rightMAP.stimulationOrder);
            editor.putString("Right.window", rightMAP.window);

            // Getting electrode array
            for (int i = 0; i < rightMAP.nbands; i++ ) {
                editor.putInt("rightTHR" + i,rightMAP.THR[i]);
                editor.putInt("rightMCL" + i, rightMAP.MCL[i]);
                putDouble(editor,"rightgain" + i,rightMAP.gains[i]);
                editor.putInt("rightelectrodes" + i,rightMAP.electrodes[i]);
            }
        } else {
            editor.putBoolean("rightMapExists", false);
        }

        if (leftMAP.exists && rightMAP.exists) { // assign audio recording settings
            settingsMono = false;
            settingsLeft = false;
            settingsRight = false;
            settingsStereo = true;
        } else if (leftMAP.exists) {
            settingsMono = true;
            settingsLeft = true;
            settingsRight = false;
            settingsStereo = false;
        } else {
            settingsMono = true;
            settingsLeft = false;
            settingsRight = true;
            settingsStereo = false;
        }

        editor.apply();
    }

    SharedPreferences.Editor putDouble(final SharedPreferences.Editor edit, final String key, final double value) {
        return edit.putLong(key, Double.doubleToRawLongBits(value));
    }

    double getDouble(final SharedPreferences prefs, final String key, final double defaultValue) {
        return Double.longBitsToDouble(prefs.getLong(key, Double.doubleToLongBits(defaultValue)));
    }

    private void initializeGUI() {
        connectionStatus = (TextView) findViewById(R.id.textConnectionStatus);
        connectionStatus.setText("Connecting...");
        buttonStartStop = (ToggleButton) findViewById(R.id.toggleButtonStartStop);
        buttonStartStop.setText(null);
        buttonStartStop.setTextOn(null);
        buttonStartStop.setTextOff(null);
        buttonStartStop.setButtonDrawable(R.drawable.start0);
        buttonStartStop.setEnabled(false);

        statusImage = (ImageView) findViewById(R.id.imageStatus);
        //////////////LEFT/////////////////////////////////////////////////////
        if (leftMAP.exists) {

            leftSensitivity = (TextView) findViewById(R.id.textViewLeftSensitivity);
            leftSensitivity.setText("Left Sensitivity: " + leftMAP.sensitivity);
            seekBarLeftSensitivity = (SeekBar) findViewById(R.id.seekBarLeftSensitivity);
            seekBarLeftSensitivity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                double value;

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    value = (double) progress / 10;
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    leftMAP.sensitivity = value;
                    leftScaleFactor = value / 32768;
                    leftSensitivity.setText("Left Sensitivity: " + value);
                    Toast.makeText(getApplicationContext(), "Left Sensitivity value changed to " + value, Toast.LENGTH_SHORT).show();

                    // Update preferences value
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                    SharedPreferences.Editor editor = preferences.edit();
                    putDouble(editor, "Left.sensitivity", leftMAP.sensitivity);
                    editor.apply();
                }
            });

            leftGain = (TextView) findViewById(R.id.textViewLeftGain);
            leftGain.setText("Left Gain: " + leftMAP.gain + " dB");
            seekBarLeftGain = (SeekBar) findViewById(R.id.seekBarLeftGain);
            seekBarLeftGain.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                double value;

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    value = (double) progress;
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    leftMAP.gain = value;
                    leftACE = new ACE(leftMAP);
                    leftGain.setText("Left Gain: " + value + " dB");
                    Toast.makeText(getApplicationContext(), "Left Gain value changed to " + value + " dB", Toast.LENGTH_SHORT).show();

                    // Update preferences value
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                    SharedPreferences.Editor editor = preferences.edit();
                    putDouble(editor, "Left.gain", leftMAP.gain);
                    editor.apply();
                }
            });
        } else {
            seekBarLeftSensitivity = (SeekBar) findViewById(R.id.seekBarLeftSensitivity);
            seekBarLeftSensitivity.setEnabled(false);

            seekBarLeftGain = (SeekBar) findViewById(R.id.seekBarLeftGain);
            seekBarLeftGain.setEnabled(false);

            // Disable text (reduce alpha to 38%)
            leftSensitivity = (TextView) findViewById(R.id.textViewLeftSensitivity);
            leftSensitivity.setTextColor(Color.argb(disabledAlpha, 0, 0, 0));

            leftGain = (TextView) findViewById(R.id.textViewLeftGain);
            leftGain.setTextColor(Color.argb(disabledAlpha, 0, 0, 0));
        }
        //////////////RIGHT////////////////////////////////////////////////////
        if (rightMAP.exists) {
            rightSensitivity = (TextView) findViewById(R.id.textViewRightSensitivity);
            rightSensitivity.setText("Right Sensitivity: " + rightMAP.sensitivity);
            seekBarRightSensitivity = (SeekBar) findViewById(R.id.seekBarRightSensitivity);
            seekBarRightSensitivity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                double value;

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    value = (double) progress / 10;
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    rightMAP.sensitivity = value;
                    rightScaleFactor = value / 32768;
                    rightSensitivity.setText("Right Sensitivity: " + value);
                    Toast.makeText(getApplicationContext(), "Right Sensitivity value changed to " + value, Toast.LENGTH_SHORT).show();

                    // Update preferences value
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                    SharedPreferences.Editor editor = preferences.edit();
                    putDouble(editor, "Right.sensitivity", rightMAP.sensitivity);
                    editor.apply();
                }
            });

            rightGain = (TextView) findViewById(R.id.textViewRightGain);
            rightGain.setText("Right Gain: " + rightMAP.gain + " dB");
            seekBarRightGain = (SeekBar) findViewById(R.id.seekBarRightGain);
            seekBarRightGain.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                double value;

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    value = (double) progress;
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    rightMAP.gain = value;
                    rightACE = new ACE(rightMAP);
                    rightGain.setText("Right Gain: " + value + " dB");
                    Toast.makeText(getApplicationContext(), "Right Gain value changed to " + value + " dB", Toast.LENGTH_SHORT).show();

                    // Update preferences value
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                    SharedPreferences.Editor editor = preferences.edit();
                    putDouble(editor, "Right.gain", rightMAP.gain);
                    editor.apply();
                }
            });
        } else {
            seekBarRightSensitivity = (SeekBar) findViewById(R.id.seekBarRightSensitivity);
            seekBarRightSensitivity.setEnabled(false);

            seekBarRightGain = (SeekBar) findViewById(R.id.seekBarRightGain);
            seekBarRightGain.setEnabled(false);

            // Disable text (reduce alpha to 38%)
            rightSensitivity = (TextView) findViewById(R.id.textViewRightSensitivity);
            rightSensitivity.setTextColor(Color.argb(disabledAlpha, 0, 0, 0));

            rightGain = (TextView) findViewById(R.id.textViewRightGain);
            rightGain.setTextColor(Color.argb(disabledAlpha, 0, 0, 0));
        }
    }

    /*private void initializeDefaultMAP() {
        leftMAP = new MAP();
        String mapStatus = leftMAP.openDefaultMAP();

        rightMAP = new MAP();
        mapStatus = rightMAP.openRightMAP();

    }*/

    private void initializeStimuli() {
        writeBuffer = new byte[516]; nullwriteBuffer = new byte[516]; sineBuffer = new byte[516];
        stimuli = new Stimuli();
        sine_stim = new short[8];
        sin_token = new short[8]; null_token = new short[8];
        sine_token = new short[50];
        for (int i = 0; i < 8; ++i) {
            sin_token[i] = (short)(i + 1);
        }

        for (int i = 0; i < 50; ++i) {
            sine_token[i] = (short) (200 * Math.sin(2 * Math.PI * i * 0.01)); // this is one cycle of sine wave
        }
    }


    private int initializeConnection() {
        boolean t = false;
        availQ = new LinkedList<Integer>();
        try{
            ftD2xx = D2xxManager.getInstance(this);
            s =new D2xxManager.DriverParameters();
            s.setBufferNumber(16);
            s.setReadTimeout(0);
            t = true;
        }
        catch (D2xxManager.D2xxException e) {
            Log.e("FTDI_HT", "getInstance fail!!");
        }
        //initializeStimuli();
        try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); }
        int resultInitializeConnection = initializeDevice();
        try {
            textOut(Integer.toString(resultInitializeConnection));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resultInitializeConnection;
    }


   private int initializeDevice() {
        connectFunction();
        ftDev.resetDevice();
        ftDev.clrRts();
        setConfig(baudRate, dataBit, stopBit, parity, flowControl);
        if (ftDev==null) {
            status.append("ftDev is null");
            return 0;}
        else  {
            status.append("ftDev is set");
            return 1; }
    }

    private void restart() {
        global_context = this;
        boolean t = false;
        //int DevCount = -1;
        //int currentPortIndex = -1;
        //int portIndex = -1;
        //onDestroy();
        ftDev.resetDevice();
        ftDev.close();
        ftDev = null;

        createDeviceList();

        if(DevCount > 0)
        {
            connectFunction();
            ftDev.resetDevice();
            ftDev.clrRts();
            setConfig(baudRate, dataBit, stopBit, parity, flowControl);
            //midToast("config:", Toast.LENGTH_SHORT);
            try { textOut("config: ");}
            catch (IOException e) { e.printStackTrace(); }
        } else {
            //midToast("DevCount<0", Toast.LENGTH_SHORT);
            try { textOut("DevCount<0");}
            catch (IOException e) { e.printStackTrace(); }
        }

        try{
            ftD2xx = D2xxManager.getInstance(this);
            s =new D2xxManager.DriverParameters();
            s.setBufferNumber(16);
            s.setReadTimeout(0);
            t = true;
        }
        catch (D2xxManager.D2xxException e) {
            Log.e("FTDI_HT", "getInstance fail!!");
        }
        status.append("BOARD IS READY NOW");
    }


    protected void onResume() {
        super.onResume();
        if(null == ftDev || false == ftDev.isOpen())
        {
            createDeviceList();
            if(DevCount > 0)
            {
                connectFunction();
                setConfig(baudRate, dataBit, stopBit, parity, flowControl);
            }
        }
    }

    protected void onPause() {
        super.onPause();
    }


    protected void onStop() {
        super.onStop();
    }


    protected void onDestroy() {
        disconnectFunction();
        android.os.Process.killProcess(android.os.Process.myPid());
        super.onDestroy();
    }


    public void createDeviceList() {

    }

    public void disconnectFunction() {
        DevCount = -1;
        currentPortIndex = -1;
        start = false;
        try    {  Thread.sleep(50);    }
        catch (InterruptedException e) {e.printStackTrace();}
        if(ftDev != null)  {
            if( true == ftDev.isOpen()) {
                ftDev.close();          }
        }
    }

    /*
    Function that chooses the second device
     */

    public void connectFunction() {
        StringBuilder stringport = new StringBuilder("Port No.: ");
        if( portIndex + 1 > DevCount){
            portIndex = 0;
        }

        if( currentPortIndex == portIndex && ftDev != null && true == ftDev.isOpen() ){
            stringport.append(String.valueOf(portIndex));
            try { textOut(stringport.toString());}
            catch (IOException e) { e.printStackTrace(); }
            return;
        }

        if(null == ftDev){
            ft_device_0 = ftD2xx.openByIndex(global_context, 0,s);
            ft_device_1 = ftD2xx.openByIndex(global_context, 1,s);
            ftDev = ft_device_0;
            ftDev.purge((byte)(D2xxManager.FT_PURGE_RX|D2xxManager.FT_PURGE_TX));
        }

        uart_configured = false;

        if(ftDev == null)
        {
            stringport.append(String.valueOf(portIndex));
            try { textOut(stringport.toString());}
            catch (IOException e) { e.printStackTrace(); }
            return;
        }

        if (true == ftDev.isOpen())
        {
            currentPortIndex = portIndex;
            ftDev.purge(D2xxManager.FT_PURGE_RX);
        }
        else
        {
            stringport.append(String.valueOf(portIndex));
            try { textOut(stringport.toString());}
            catch (IOException e) { e.printStackTrace(); }
        }
    }


    void setConfig(int baud, byte dataBits, byte stopBits, byte parity, byte flowControl) {
        // configure port
        // reset to UART mode for 232 devices
        ftDev.setBitMode((byte) 0, D2xxManager.FT_BITMODE_RESET);

        ftDev.setBaudRate(baud);

        switch (dataBits)
        {
            case 7:
                dataBits = D2xxManager.FT_DATA_BITS_7;
                break;
            case 8:
                dataBits = D2xxManager.FT_DATA_BITS_8;
                break;
            default:
                dataBits = D2xxManager.FT_DATA_BITS_8;
                break;
        }

        switch (stopBits)
        {
            case 1:
                stopBits = D2xxManager.FT_STOP_BITS_1;
                break;
            case 2:
                stopBits = D2xxManager.FT_STOP_BITS_2;
                break;
            default:
                stopBits = D2xxManager.FT_STOP_BITS_1;
                break;
        }

        switch (parity)
        {
            case 0:
                parity = D2xxManager.FT_PARITY_NONE;
                break;
            case 1:
                parity = D2xxManager.FT_PARITY_ODD;
                break;
            case 2:
                parity = D2xxManager.FT_PARITY_EVEN;
                break;
            case 3:
                parity = D2xxManager.FT_PARITY_MARK;
                break;
            case 4:
                parity = D2xxManager.FT_PARITY_SPACE;
                break;
            default:
                parity = D2xxManager.FT_PARITY_NONE;
                break;
        }

        ftDev.setDataCharacteristics(dataBits, stopBits, parity);

        short flowCtrlSetting;
        switch (flowControl)
        {
            case 0:
                flowCtrlSetting = D2xxManager.FT_FLOW_NONE;
                break;
            case 1:
                flowCtrlSetting = D2xxManager.FT_FLOW_RTS_CTS;
                break;
            case 2:
                flowCtrlSetting = D2xxManager.FT_FLOW_DTR_DSR;
                break;
            case 3:
                flowCtrlSetting = D2xxManager.FT_FLOW_XON_XOFF;
                break;
            default:
                flowCtrlSetting = D2xxManager.FT_FLOW_NONE;
                break;
        }

        ftDev.setFlowControl(flowCtrlSetting, XON, XOFF);
        uart_configured = true;
    }

    void midToast(String str, int showTime) {
        Toast toast = Toast.makeText(global_context, str, showTime);
        toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL, 0, 0);

        TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
        v.setTextColor(Color.YELLOW);
        toast.show();
    }

    public void textOut(final String s) throws IOException {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                status.append("\n" + s);
                //status.setText("\n"+s);
            } }); }

    // Storage Permissions variables
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    //permission method.
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have read or write permission
        int writePermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readPermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (writePermission != PackageManager.PERMISSION_GRANTED || readPermission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    /*
     *function for setting the output data buffer as per the given guidelines
     */
    private void setOutputBuffer(byte outputBuffer[], short sine_token[] ) {

        short n = 8;
        short electrode_token[] = new short[8]/*, sine_token[8]*/;
        for (short i = 0; i < n; ++i) {
            electrode_token[i] = (short)(i + 1);
            //sine_token[i] = i + 1;
        }
        short ppf = 64;
        short pw = 25;

        short mode = 28;
        short elecs[] = new short[64];
        short amps[] = new short[64];
        for (int i = 0; i < ppf; ++i) {
            int j = i / n;
            amps[i] = sine_token[j];
            elecs[i] = electrode_token[i % 8];
        }
        //char outputBuffer[516] = {0};
        outputBuffer[0] = (byte)136;
        outputBuffer[1] = (byte)254;
        outputBuffer[2] = 5;
        outputBuffer[3] = 1;
        outputBuffer[4] = 4;
        outputBuffer[5] = (byte)252;
        for (int i = 0; i < 64; ++i) {
            outputBuffer[i + 6] = (byte)elecs[i];
            outputBuffer[i + 132] = (byte)amps[i];
            outputBuffer[i + 264] = (byte)elecs[i];
            outputBuffer[i + 390] = (byte)amps[i];
        }
        outputBuffer[258] = (byte)136;
        outputBuffer[259] = (byte)254;
        outputBuffer[260] = (byte)5;
        outputBuffer[261] = (byte)1;
        outputBuffer[262] = (byte)4;
        outputBuffer[263] = (byte)252;

        outputBuffer[380] = (byte)mode; //mode left
        outputBuffer[381] = (byte)mode; //mode right
        outputBuffer[382] = (byte)(pw / 256); //left pulsewidth high[15:8]
        outputBuffer[383] = (byte)(pw % 256); //left pulsewidth low[7:0]
        outputBuffer[384] = (byte)(pw / 256); //right pulsewidth high[15:8]
        outputBuffer[385] = (byte)(pw % 256); //right pulsewidth low[7:0]

        outputBuffer[506] = (byte)(ppf / 256); //left pulsesPerFrame high[15:8]
        outputBuffer[507] = (byte)(ppf % 256); //left pulsesPerFrame low[7:0]
        outputBuffer[508] = (byte)(ppf / 256); //right pulsesPerFrame high[15:8]
        outputBuffer[509] = (byte)(ppf % 256); //right pulsesPerFrame low[7:0]

        short cycles = 600;
        outputBuffer[510] = (byte)(cycles / 256); //left pulsesPerFrame high[15:8]
        outputBuffer[511] = (byte)(cycles % 256); //left pulsesPerFrame low[7:0]
        outputBuffer[512] = (byte)(cycles / 256); //right pulsesPerFrame high[15:8]
        outputBuffer[513] = (byte)(cycles % 256); //right pulsesPerFrame low[7:0]

    }

    /*
 *function for setting the output data buffer as per the given guidelines
 */
    private void setOutputBuffer(byte outputBuffer[], MAP left_map, MAP right_map) {

        short left_pw = (short)left_map.pulseWidth;
        short right_pw = (short)right_map.pulseWidth;

        short left_ppf = (short)left_map.pulsesPerFrame;
        short right_ppf = (short)right_map.pulsesPerFrame;

        short left_nRFcycles = (short)left_map.nRFcycles;
        short right_nRFcycles = (short)right_map.nRFcycles;

        //Header
        outputBuffer[0] = (byte)136;
        outputBuffer[1] = (byte)254;
        outputBuffer[2] = 5;
        outputBuffer[3] = 1;
        outputBuffer[4] = 4;
        outputBuffer[5] = (byte)252;
        outputBuffer[258] = (byte)136;
        outputBuffer[259] = (byte)254;
        outputBuffer[260] = (byte)5;
        outputBuffer[261] = (byte)1;
        outputBuffer[262] = (byte)4;
        outputBuffer[263] = (byte)252;

        outputBuffer[380] = (byte)left_map.stimulationModeCode; //mode left
        outputBuffer[381] = (byte)right_map.stimulationModeCode; //mode right
        outputBuffer[382] = (byte)(left_pw / 256); //left pulsewidth high[15:8]
        outputBuffer[383] = (byte)(left_pw % 256); //left pulsewidth low[7:0]
        outputBuffer[384] = (byte)(right_pw / 256); //right pulsewidth high[15:8]
        outputBuffer[385] = (byte)(right_pw % 256); //right pulsewidth low[7:0]

        outputBuffer[506] = (byte)(left_ppf / 256); //left pulsesPerFrame high[15:8]
        outputBuffer[507] = (byte)(left_ppf % 256); //left pulsesPerFrame low[7:0]
        outputBuffer[508] = (byte)(right_ppf / 256); //right pulsesPerFrame high[15:8]
        outputBuffer[509] = (byte)(right_ppf % 256); //right pulsesPerFrame low[7:0]

        outputBuffer[510] = (byte)(left_nRFcycles / 256); //left number of RF cycles for each IPD high[15:8]
        outputBuffer[511] = (byte)(left_nRFcycles % 256); //left number of RF cycles for each IPD low[7:0]
        outputBuffer[512] = (byte)(right_nRFcycles / 256); //right number of RF cycles for each IPD high[15:8]
        outputBuffer[513] = (byte)(right_nRFcycles % 256); //right number of RF cycles for each IPD low[7:0]
    }



    public void startOrStop(View view) {
        boolean on = ((ToggleButton) view).isChecked();
        if (on) {
            buttonStartStop.setButtonDrawable(R.drawable.stop);
            startProcessing();
        } else {
            buttonStartStop.setButtonDrawable(R.drawable.start);
            stopProcessing();
        }
    }

    private void startProcessing() {
        start = true;
        portIndex = 1;
        if (ftDev==null){
            status.append("DEVICE IS NULL");
            Toast.makeText(this,"Device not Connected. Please reconnect the board,",Toast.LENGTH_LONG).show();
        }
        else {
            status.setText("Running");
            setOutputBuffer(nullwriteBuffer, null_token); // zero buffer
            setOutputBuffer(writeBuffer, leftMAP, rightMAP); // zero buffer
            readThread1 = new readThread();
            readThread1.start();
        }
    }


    private void stopProcessing() {
        readThread1.interrupt();
        status.setText("Stopped");
    }


    /*
 *Main core logic for reading and writing data
 */

    private class readThread  extends Thread
    {
        //Handler mHandler;
        readThread(){ this.setPriority(Thread.MAX_PRIORITY);        }

        @Override
        public void run()
        {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
            final byte [] buffbyte2 =  new byte[512];
            final double [] leftData = new double[128];
            final double [] rightData = new double[128];
            final short [] shortbuffer = new short[256];
            byte[] readBuffer = new byte[512];

            ftDev.read(readBuffer, 512, 0); // to get the board started
            int rc = ftDev.write(nullwriteBuffer, 516, true); // to get the board started
            rc = ftDev.write(nullwriteBuffer, 516, true);// to get the board started

            leftScaleFactor = leftMAP.sensitivity/32768;
            rightScaleFactor = rightMAP.sensitivity/32768;

            int k =0;

            while(start) {
                if (Thread.interrupted()) {
                    start = false;
                    //stopStimulation = true;
                    sendnullframes();
                    break;
                }

                int iavailable_0 = ftDev.getQueueStatus();
                if(iavailable_0>=512) {

                    for (int j = 0; j < 8; ++j) {
                        sine_stim[j] = sine_token[k]; //200;
                        ++k; k %= 50;  }
                    setOutputBuffer(sineBuffer, sine_stim);

                    // STEP 1: READ AUDIO SIGNAL
                    Arrays.fill(buffbyte2, (byte) 0);
                    ftDev.read(readBuffer, 512, 0);
                    System.arraycopy(readBuffer, 0, buffbyte2, 0, 512);
                    ByteBuffer.wrap(buffbyte2).order(ByteOrder.BIG_ENDIAN).asShortBuffer().get(shortbuffer);
                    for (int m = 0; m < leftData.length; m++) {
                        leftData[m] = ((double) shortbuffer[2 * m]) * leftScaleFactor;
                        rightData[m] = ((double) shortbuffer[(2 * m) + 1]) *rightScaleFactor;
                    }

                    // STEP 2: Process Audio Signal
                    leftStimuli = leftACE.processAudio(leftData);
                    rightStimuli = rightACE.processAudio(rightData);

                    // STEP 3: Stream Stimuli
                    updateOutputBuffer(); // comment this to pass sine wave
                    rc = ftDev.write(writeBuffer, 516, true); // comment this to pass sine wave
                    //rc = ftDev.write(sineBuffer, 516, true); // uncomment this to pass sine wave
                }
            }
        }

        private void updateOutputBuffer() {
            for (int i = 0; i < leftMAP.pulsesPerFrame; ++i) {
                writeBuffer[i + 6] = (byte)leftStimuli.Electrodes[i];
                writeBuffer[i + 132] = (byte)leftStimuli.Amplitudes[i];
            }
            for (int i = 0; i < rightMAP.pulsesPerFrame; ++i) {
                writeBuffer[i + 264] = (byte)rightStimuli.Electrodes[i];
                writeBuffer[i + 390] = (byte)rightStimuli.Amplitudes[i];
            }
        }

        private void sendnullframes() {
            byte[] readBuffer = new byte[512];
            setOutputBuffer(writeBuffer, null_token); // zero buffer
            int counter = 10;

            while(counter>0) {
                int iavailable_0 = ftDev.getQueueStatus();
                if (iavailable_0 >= 512) {
                    ftDev.read(readBuffer, 512, 0);
                    int rc = ftDev.write(writeBuffer, 516, true);
                    counter--;
                }
            }
        }
    }



}
