package cilab.utdallas.edu.ccimobile;

import android.Manifest;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.OpenableColumns;
import androidx.annotation.NonNull;

import com.google.android.material.color.MaterialColors;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.util.JsonWriter;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.ftdi.j2xx.D2xxManager;
import com.ftdi.j2xx.FT_Device;
import com.xw.repo.BubbleSeekBar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;

/**
 * The MainActivity class manages the home activity of the application
 */
public class MainActivity extends AppCompatActivity implements InitializationResultReceiver.Receiver {

    boolean settingsMono, settingsLeft, settingsRight, settingsStereo, folderExists;
    boolean noMAP = true; // No MAP selected yet
    boolean errorMAP = false;
    int disAlpha = 38; // opacity at 38% when item disabled

    public static D2xxManager ftD2xx = null;
    FT_Device ft_device_0, ft_device_1, ftDev;
    int DevCount = -1;
    int currentPortIndex = -1;
    int portIndex = -1;
    Queue<Integer> availQ;
    readThread readThread1;

    byte[] writeBuffer, nullWriteBuffer, sineBuffer;

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
    short sine_stim[], sin_token[], null_token[], sine_token[];

    public MAP leftMAP, rightMAP;
    private ACE leftACE, rightACE;

    private double leftScaleFactor, rightScaleFactor;

    Stimuli stimuli, leftStimuli, rightStimuli;
    D2xxManager.DriverParameters s;

    TextView status, connectionStatus, leftSensitivity, rightSensitivity, leftGain, rightGain, textViewMAP;
    ImageView statusImage;
    ToggleButton buttonStartStop, buttonOnOff;
    Button buttonSaveMAP;

    BubbleSeekBar bubbleLeftSens, bubbleLeftGain, bubbleRightSens, bubbleRightGain;

    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 12;
    private static final int READ_REQUEST_CODE = 42;
    private static final int RETURN_FROM_SETTINGS = 3;

    /**
     * Called when the application is first created.
     * @param savedInstanceState saved state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        global_context = this;
        status = findViewById(R.id.textStatus);
        leftSensitivity = findViewById(R.id.textViewLeftSensitivity);
        leftGain = findViewById(R.id.textViewLeftGain);
        rightSensitivity = findViewById(R.id.textViewRightSensitivity);
        rightGain = findViewById(R.id.textViewRightGain);
        textViewMAP = findViewById(R.id.textView215);
        buttonSaveMAP = findViewById(R.id.buttonSaveMAP);
        bubbleLeftSens = findViewById(R.id.bubbleSeekBarLeftSensitivity);
        bubbleLeftGain = findViewById(R.id.bubbleLeftGain);
        bubbleRightSens = findViewById(R.id.bubbleRightSens);
        bubbleRightGain = findViewById(R.id.bubbleRightGain);

        buttonOnOff = findViewById(R.id.toggleButtonOnOff);
        buttonOnOff.setEnabled(false);

        // Disable buttons/sliders
        buttonSaveMAP.setEnabled(false);

        disableSliders("both");

        leftSensitivity.setText(R.string.textLeftSens);
        leftGain.setText(R.string.textLeftGain);
        rightSensitivity.setText(R.string.textRightSens);
        rightGain.setText(R.string.textRightGain);

    }

    /**
     * Called when the SELECT MAP button is pressed. It checks for permission from the user to
     * access external storage on the phone (to retrieve the MAP text files). Then it calls
     * performFileSearch to open the MAP file.
     * @param view view
     */
    public void selectMAP(View view) {
        // Check if permission already exists
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        // If permission does not already exist
        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.e("MainActivity.java", "Permission DOES NOT ALREADY EXIST for " +
                    "WRITE_EXTERNAL_STORAGE.");
            // If user has previously denied permission, first explain why the permission is needed,
            // then make request
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Permission to access device files is required for this app " +
                        "to load your MAP file(s). Please allow the permission.")
                        .setTitle("Permission required");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Log.e("MainActivity.java", "Alert dialogue clicked.");
                        makeRequest();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            } else {
                // If user has not previously denied permission, make request without explanation
                makeRequest();
            }
        } else {
            // If permission already exists, perform file search
            Log.e("MainActivity.java", "Permission was already granted.");
            performFileSearch();
        }
    }

    /**
     * Prompts the user for permission.
     */
    protected void makeRequest() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
    }

    /**
     * Opens the file selector
     */
    public void performFileSearch() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    /**
     * Main functions
     */
    void startMainFunctions() {
        if (noMAP) {
            initialize();
            callInitializationService();
        }
        else {
            initializeMAP();
            if (!leftMAP.dataMissing && !rightMAP.dataMissing) {
                updateGUI("both");
            }


        }
        //verifyFolderExists();
        //new VeryLongAsyncTask(this).execute();
    }

    /**
     * Receives result from requesting permission (for file access).
     * @param requestCode request code
     * @param permissions permission
     * @param grantResults result
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission granted
                    Log.e("MainActivity.java", "Permission GRANTED for " +
                            "WRITE_EXTERNAL_STORAGE.");
                    performFileSearch();
                } else {
                    // permission denied
                    Log.e("MainActivity.java", "Permission DENIED for " +
                            "WRITE_EXTERNAL_STORAGE.");
                }
            }
        }
    }

    /**
     * Returns the MAP filename.
     * @param uri uri
     * @return MAP filename
     */
    public String getFileName(Uri uri) {
        String result = null;
        if (Objects.equals(uri.getScheme(), "content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            assert result != null;
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    /**
     * Saves the current MAP parameters to a JSON text file on the phone
     */
    void saveMAP(String saveFilename) {
        String MAPfilename = saveFilename + ".txt";
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), MAPfilename);
        try {
            // Write and save the file
            FileOutputStream fOut = new FileOutputStream(file);
            writeJsonStream(fOut);
            fOut.close();

            // Download the file to make it appear in the Downloads folder
            File dir = new File("//sdcard//Download//");
            File MAPfile = new File(dir, MAPfilename);
            DownloadManager downloadManager = (DownloadManager) global_context.getSystemService(DOWNLOAD_SERVICE);
            if (downloadManager != null) {
                downloadManager.addCompletedDownload(MAPfile.getName(), MAPfile.getName(), true, "text/plain", MAPfile.getAbsolutePath(), file.length(), true);
            }

            Log.e("MainActivity.java", "File saved successfully.");
        } catch (IOException e) {
            Log.e("MainActivity.java", "Error saving file. " + e.getMessage());
        }
    }

    /**
     * Prompts the user for the MAP filename
     */
    public void promptMAPfilename(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Save MAP");

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.text_input_filename, (ViewGroup) findViewById(android.R.id.content), false);
        final EditText input = viewInflated.findViewById(R.id.input);
        builder.setView(viewInflated);

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                String saveFilename = input.getText().toString();
                saveMAP(saveFilename);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        builder.show();

    }

    /**
     * Saves a MAP in JSON format on the phone
     * @param out o
     * @throws IOException e
     */
    public void writeJsonStream(OutputStream out) throws IOException {
        JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
        writer.setIndent("  ");

        writer.beginObject(); // first {

        writeJSONGeneral(writer);

        if (leftMAP.exists)
            writeJSONMAP(writer, leftMAP, "Left");
        if (rightMAP.exists)
            writeJSONMAP(writer, rightMAP, "Right");

        writer.endObject(); // last }
        writer.close();
    }

    /**
     * Writes the general user data in JSON format
     * @param writer w
     * @throws IOException e
     */
    public void writeJSONGeneral(JsonWriter writer) throws IOException {
        writer.name("General");
        writer.beginArray();
        writer.beginObject();

        writer.name("subjectName").value("Subject 01");
        writer.name("subjectID").value("S01");
        writer.name("mapTitle").value("S01_ACE_900Hz");
        writer.name("numberOfImplants").value(2);
        writer.name("implantedEar").value("bilateral");
        writer.name("ear").value("Both");

        writer.endObject();
        writer.endArray();
    }

    /**
     * Writes the left or right MAP in JSON format
     * @param writer w
     * @throws IOException e
     */
    public void writeJSONMAP(JsonWriter writer, MAP map, String side) throws IOException {
        writer.name(side);

        writer.beginArray();
        writer.beginObject();

        writer.name(side + ".implantType").value(map.implantType);
        writer.name(side + ".samplingFrequency").value(map.samplingFrequency);
        writer.name(side + ".numberOfChannels").value(map.numberOfChannels);
        writer.name(side + ".soundProcessingStrategy").value(map.soundProcessingStrategy);
        writer.name(side + ".nMaxima").value(map.nMaxima);
        writer.name(side + ".stimulationMode").value(map.stimulationMode);
        writer.name(side + ".stimulationRate").value(map.stimulationRate);
        writer.name(side + ".pulseWidth").value(map.pulseWidth);
        writer.name(side + ".sensitivity").value(map.sensitivity);
        writer.name(side + ".gain").value(map.gain);
        writer.name(side + ".volume").value(map.volume);
        writer.name(side + ".Qfactor").value(map.Qfactor);
        writer.name(side + ".baseLevel").value(map.baseLevel);
        writer.name(side + ".saturationLevel").value(map.saturationLevel);
        writer.name(side + ".stimulationOrder").value(map.stimulationOrder);
        writer.name(side + ".frequencyTable").value(map.frequencyTable);
        writer.name(side + ".window").value(map.window);

        writer.name(side + ".El_CF1_CF2_THR_MCL_Gain");
        writeJSONElectrodes(writer, map);

        writer.endObject();
        writer.endArray();
    }

    /**
     * Writes the left or right electrode information in JSON format
     * @param writer w
     * @throws IOException e
     */
    public void writeJSONElectrodes(JsonWriter writer, MAP map) throws IOException {
        writer.beginArray();
        int numElectrodes = 22;

        for (int i = 0; i < numElectrodes; i++) {
            writer.beginObject();
            writer.name("electrodes").value(numElectrodes - i);
            writer.name("lowerCutOffFrequencies").value(map.lowerCutOffFrequencies[i]);
            writer.name("higherCutOffFrequencies").value(map.higherCutOffFrequencies[i]);
            writer.name("THR").value(map.THR[i]);
            writer.name("MCL").value(map.MCL[i]);
            writer.name("gains").value(map.gains[i]);
            writer.endObject();
        }

        writer.endArray();
    }

    /**
     * Inflates the menu.
     * @param menu menu
     * @return true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_activity, menu);
        return true;
    }

    /**
     * Removes menu options for Settings and Environments if no MAP has been selected yet.
     * @param menu m
     * @return true
     */
    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
        if (noMAP || errorMAP) {
            menu.findItem(R.id.menuSettings).setEnabled(false);
            menu.findItem(R.id.menuEnvironments).setEnabled(false);
            //menu.findItem(R.id.menuTesting).setEnabled(false);
        }
        else {
            menu.findItem(R.id.menuSettings).setEnabled(true);
            menu.findItem(R.id.menuEnvironments).setEnabled(true);
            //menu.findItem(R.id.menuTesting).setEnabled(true);
        }
        return true;
    }

    /**
     * Creates a folder in phone storage if one doesn't exist.
     */
    void verifyFolderExists() {
        File folder = new File(Environment.getExternalStorageDirectory() +
                File.separator + "CCiMOBILE MAPs");
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdirs();
        }
        folderExists = success;
    }

    /**
     * Selects new activity from menu options.
     * @param item menu
     * @return true if valid activity selected
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuSettings:
                Intent myIntent = new Intent(MainActivity.this, SettingsActivity.class);
                MainActivity.this.startActivityForResult(myIntent, RETURN_FROM_SETTINGS);
                return true;
            case R.id.menuEnvironments:
                myIntent = new Intent(MainActivity.this, EnvironmentsActivity.class);
                MainActivity.this.startActivityForResult(myIntent, 1);
                return true;
            case R.id.menuTesting:
                myIntent = new Intent(MainActivity.this, SettingsActivityTabbed.class);
                MainActivity.this.startActivityForResult(myIntent, 1);
                return true;
            default:
                // The user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Updates the MAP after returning from the Settings activity.
     * @param data updated MAP data
     */
    void updateMAPFromSettings(Intent data) {
        if (leftMAP.exists) {
            leftMAP.sensitivity = data.getDoubleExtra("leftSensitivity", 0);
            if (leftMAP.sensitivity > 10) {
                leftMAP.sensitivity = 10;
            } else if (leftMAP.sensitivity < 0) {
                leftMAP.sensitivity = 0;
            }

            bubbleLeftSens.setProgress((float) leftMAP.sensitivity);

            leftMAP.gain = data.getDoubleExtra("leftGain", 0);
            if (leftMAP.gain > 50) {
                leftMAP.gain = 50;
            } else if (leftMAP.gain < 0) {
                leftMAP.gain = 0;
            }

            bubbleLeftGain.setProgress((float) leftMAP.gain);

            leftMAP.implantGeneration = data.getStringExtra("leftMAPimplantGeneration");
            leftMAP.stimulationModeCode = data.getIntExtra("leftMAPstimulationModeCode", 0);
            leftMAP.pulsesPerFramePerChannel = data.getIntExtra("leftMAPpulsesPerFramePerChannel", 0);
            leftMAP.pulsesPerFrame = data.getIntExtra("leftMAPpulsesPerFrame", 0);
            leftMAP.interpulseDuration = data.getDoubleExtra("leftMAPinterpulseDuration", 0);
            leftMAP.nRFcycles = data.getIntExtra("leftMAPnRFcycles", 0);

            // need to update pulsewidth and all the other parameters
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            leftMAP.stimulationRate = preferences.getInt("Left.stimulationRate", 0);
            leftMAP.pulseWidth = preferences.getInt("Left.pulseWidth", 0);
            leftMAP.sensitivity = getDouble(preferences, "Left.sensitivity");
            leftMAP.gain = getDouble(preferences, "Left.gain");
            leftMAP.Qfactor = getDouble(preferences, "Left.Qfactor");
            leftMAP.baseLevel = getDouble(preferences, "Left.baseLevel");
            leftMAP.saturationLevel = getDouble(preferences, "Left.saturationLevel");
            leftMAP.nMaxima = preferences.getInt("Left.nMaxima", 0);
            leftMAP.volume = preferences.getInt("Left.volume", 0);
            leftMAP.stimulationOrder = preferences.getString("Left.stimulationOrder", "");
            leftMAP.window = preferences.getString("Left.window", "");

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

            bubbleRightSens.setProgress((float) rightMAP.sensitivity);


            rightMAP.gain = data.getDoubleExtra("rightGain", 0);
            if (rightMAP.gain > 50) {
                rightMAP.gain = 50;
            } else if (rightMAP.gain < 0) {
                rightMAP.gain = 0;
            }

            bubbleRightGain.setProgress((float) rightMAP.gain);

            rightMAP.implantGeneration = data.getStringExtra("rightMAPimplantGeneration");
            rightMAP.stimulationModeCode = data.getIntExtra("rightMAPstimulationModeCode", 0);
            rightMAP.pulsesPerFramePerChannel = data.getIntExtra("rightMAPpulsesPerFramePerChannel", 0);
            rightMAP.pulsesPerFrame = data.getIntExtra("rightMAPpulsesPerFrame", 0);
            rightMAP.interpulseDuration = data.getDoubleExtra("rightMAPinterpulseDuration", 0);
            rightMAP.nRFcycles = data.getIntExtra("rightMAPnRFcycles", 0);

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            rightMAP.stimulationRate = preferences.getInt("Right.stimulationRate", 0);
            rightMAP.pulseWidth = preferences.getInt("Right.pulseWidth", 0);
            rightMAP.sensitivity = getDouble(preferences, "Right.sensitivity");
            rightMAP.gain = getDouble(preferences, "Right.gain");
            rightMAP.Qfactor = getDouble(preferences, "Right.Qfactor");
            rightMAP.baseLevel = getDouble(preferences, "Right.baseLevel");
            rightMAP.saturationLevel = getDouble(preferences, "Right.saturationLevel");
            rightMAP.nMaxima = preferences.getInt("Right.nMaxima", 0);
            rightMAP.volume = preferences.getInt("Right.volume", 0);
            rightMAP.stimulationOrder = preferences.getString("Right.stimulationOrder", "");
            rightMAP.window = preferences.getString("Right.window", "");

            // re-initialize ACE
            rightACE = new ACE(rightMAP);
        }
        updateOutputBufferMAP();
    }

    /**
     * Updates the output buffer.
     */
    void updateOutputBufferMAP() {
        if (writeBuffer != null)
        {
            if (leftMAP.exists && rightMAP.exists) { // both left and right
                setOutputBuffer(writeBuffer, leftMAP, rightMAP);
            } else if (leftMAP.exists) { // only left
                setOutputBuffer(writeBuffer, leftMAP, leftMAP); // zero buffer
            } else if (rightMAP.exists) { // only right
                setOutputBuffer(writeBuffer, rightMAP, rightMAP); // zero buffer
            }
        }
    }

    /**
     * Responds to an activity result.
     * @param requestCode request
     * @param resultCode result
     * @param data data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case RETURN_FROM_SETTINGS:
                    updateMAPFromSettings(data); // returning from Settings activity
                    break;
                case READ_REQUEST_CODE:
                    updateMAPfile(data); // selected a MAP
                    break;
            }
        }
    }

    /**
     * Gets the selected MAP filename and updates the MAP.
     * @param data data
     */
    void updateMAPfile(Intent data) {
        Uri uri;
        if (data != null) {
            uri = data.getData();
            assert uri != null;
            Log.e("MainActivity.java", "Uri: " + uri.toString());

            String fileName = getFileName(uri);
            textViewMAP.setText(fileName);

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("MAPfilename", fileName);
            editor.apply();

            startMainFunctions();
            noMAP = false;
        }
    }

    /**
     * Creates a loading animation.
     */
    static private class VeryLongAsyncTask extends AsyncTask<Void, Void, Void> {
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


    /**
     * Starts the board.
     */
    private void callInitializationService() {
        InitializationResultReceiver mReceiver = new InitializationResultReceiver(new Handler());
        mReceiver.setReceiver(this);
        Intent intent = new Intent(Intent.ACTION_SYNC, null, this, InitializationService.class);
        /* Send optional extras to Download IntentService */
        //intent.putExtra("url", url);
        intent.putExtra("receiver", mReceiver);
        intent.putExtra("requestId", 101);
        //startService(intent);
        this.startService(intent);
    }

    /**
     * Manages result codes from the board.
     * @param resultCode code
     * @param resultData data
     */
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
                //results = resultData.getString("result");
                String msg = "Completion Message";
                //Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                Snackbar.make(findViewById(R.id.rootMain), msg, Snackbar.LENGTH_LONG).setAction("Action", null).show();

                break;

            case InitializationService.STATUS_STEP0_FINISHED:
                /* Hide progress & extract result from bundle */
                setProgressBarIndeterminateVisibility(false);
                results = resultData.getString("result");
                connectionStatus.setText(R.string.Connecting1);
                textOut(results);
                break;

            case InitializationService.STATUS_STEP1_FINISHED:
                /* Hide progress & extract result from bundle */
                setProgressBarIndeterminateVisibility(false);
                status.setText(R.string.textStep1);
                connectionStatus.setText(R.string.Connecting2);
                break;

            case InitializationService.STATUS_STEP2_FINISHED:
                /* Hide progress & extract result from bundle */
                setProgressBarIndeterminateVisibility(false);
                status.setText(R.string.textStep2);
                connectionStatus.setText(R.string.Connecting3);
                break;

            case InitializationService.STATUS_STEP3_FINISHED:
                /* Hide progress & extract result from bundle */
                setProgressBarIndeterminateVisibility(false);
                //String msg3 = "Step 3 completed";
                status.setText(R.string.textReady);
                initializeConnection();

                //buttonStartStop.setEnabled(true);
                //buttonStartStop.setButtonDrawable(R.drawable.start);

                buttonOnOff.setEnabled(true);
                buttonOnOff.setChecked(false);

                connectionStatus.setText(R.string.textConnected);
                statusImage.setImageResource(R.drawable.connected);
                break;

            case InitializationService.STATUS_ERROR:
                /* Handle the error */
                String error = resultData.getString(Intent.EXTRA_TEXT);
                //Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                Snackbar.make(findViewById(R.id.rootMain), "Error: " + error, Snackbar.LENGTH_LONG).setAction("Action", null).show();

                break;
        }
    }

    /**
     * Initializes MAPs, stimuli, and GUI.
     */
    private void initialize() {
        initializeMAP();
        if (!leftMAP.dataMissing && !rightMAP.dataMissing) {
            initializeStimuli();
            initializeGUI();
        }
    }

    /**
     * Initializes the MAPs.
     */
    private void initializeMAP() {
        leftMAP = new MAP();
        rightMAP = new MAP();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String MAP_filename;
        MAP_filename = preferences.getString("MAPfilename","");

        // Check if filename string is empty
        assert MAP_filename != null;
        if (!MAP_filename.isEmpty()) {
            leftMAP.getMAPData(MAP_filename, "left");
            rightMAP.getMAPData(MAP_filename, "right");

            if (leftMAP.dataMissing && rightMAP.dataMissing)
                //Toast.makeText(getApplicationContext(), "Error: MAP could not be opened due to missing data from both left and right ear. Please select a different MAP.", Toast.LENGTH_LONG).show();
                Snackbar.make(findViewById(R.id.rootMain), "Error: MAP could not be opened due to missing data from both left and right ear. Please select a different MAP.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            else if (leftMAP.dataMissing)
                //Toast.makeText(getApplicationContext(), "Error: MAP could not be opened due to missing data from left ear. Please select a different MAP.", Toast.LENGTH_LONG).show();
                Snackbar.make(findViewById(R.id.rootMain), "Error: MAP could not be opened due to missing data from left ear. Please select a different MAP.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            else if (rightMAP.dataMissing)
                //Toast.makeText(getApplicationContext(), "Error: MAP could not be opened due to missing data from right ear. Please select a different MAP.", Toast.LENGTH_LONG).show();
                Snackbar.make(findViewById(R.id.rootMain), "Error: MAP could not be opened due to missing data from right ear. Please select a different MAP.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            if (leftMAP.dataMissing || rightMAP.dataMissing) {
                errorMAP = true;
                disableSliders("both");
            }
            else {
                errorMAP = false;
                if (leftMAP.exists || rightMAP.exists)
                    writeMAPToPreferences();
            }
        }
        else {
            //Toast.makeText(getApplicationContext(), "Please select a valid MAP.", Toast.LENGTH_LONG).show();
            Snackbar.make(findViewById(R.id.rootMain), "Please select a valid MAP.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
        }
    }

    /**
     * Saves MAP parameters to Preferences. Called when a new MAP is selected.
     */
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

            // update ACE
            leftACE = new ACE(leftMAP);

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

            // update ACE
            rightACE = new ACE(rightMAP);
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
        updateOutputBufferMAP();
    }

    /**
     * Preferences editor
     * @param edit edit
     * @param key key
     * @param value value
     */
    void putDouble(final SharedPreferences.Editor edit, final String key, final double value) {
        edit.putLong(key, Double.doubleToRawLongBits(value));
    }

    /**
     * Returns double
     * @param prefs prefs
     * @param key key
     * @return double
     */
    double getDouble(final SharedPreferences prefs, final String key) {
        return Double.longBitsToDouble(prefs.getLong(key, Double.doubleToLongBits((double) 0)));
    }

    /**
     * Enables the bubble sliders
     * @param side left or right
     */
    private void enableSliders(String side) {
        if (side.equals("left") || side.equals("both")) {
            bubbleLeftSens.setEnabled(true);
            bubbleLeftSens.setTrackColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
            bubbleLeftSens.setSecondTrackColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
            bubbleLeftSens.setBubbleColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
            bubbleLeftSens.setThumbColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));

            bubbleLeftGain.setEnabled(true);
            bubbleLeftGain.setTrackColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
            bubbleLeftGain.setSecondTrackColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
            bubbleLeftGain.setBubbleColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
            bubbleLeftGain.setThumbColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
        }
        if (side.equals("right") || side.equals("both")) {
            bubbleRightSens.setEnabled(true);
            bubbleRightSens.setTrackColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
            bubbleRightSens.setSecondTrackColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
            bubbleRightSens.setBubbleColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
            bubbleRightSens.setThumbColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));

            bubbleRightGain.setEnabled(true);
            bubbleRightGain.setTrackColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
            bubbleRightGain.setSecondTrackColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
            bubbleRightGain.setBubbleColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
            bubbleRightGain.setThumbColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
        }
    }

    /**
     * Disables the bubble sliders
     * @param side left or right
     */
    private void disableSliders(String side) {
        if (side.equals("left") || side.equals("both")) {
            bubbleLeftSens.setEnabled(false);
            bubbleLeftSens.setTrackColor(Color.argb(disAlpha,0,0,0));
            bubbleLeftSens.setSecondTrackColor(Color.argb(disAlpha,0,0,0));
            bubbleLeftSens.setBubbleColor(Color.argb(disAlpha,0,0,0));
            bubbleLeftSens.setThumbColor(Color.argb(disAlpha,0,0,0));
            bubbleLeftSens.setProgress(0);

            bubbleLeftGain.setEnabled(false);
            bubbleLeftGain.setTrackColor(Color.argb(disAlpha,0,0,0));
            bubbleLeftGain.setSecondTrackColor(Color.argb(disAlpha,0,0,0));
            bubbleLeftGain.setBubbleColor(Color.argb(disAlpha,0,0,0));
            bubbleLeftGain.setThumbColor(Color.argb(disAlpha,0,0,0));
            bubbleLeftGain.setProgress(0);
        }
        if (side.equals("right") || side.equals("both")) {
            bubbleRightSens.setEnabled(false);
            bubbleRightSens.setTrackColor(Color.argb(disAlpha,0,0,0));
            bubbleRightSens.setSecondTrackColor(Color.argb(disAlpha,0,0,0));
            bubbleRightSens.setBubbleColor(Color.argb(disAlpha,0,0,0));
            bubbleRightSens.setThumbColor(Color.argb(disAlpha,0,0,0));
            bubbleRightSens.setProgress(0);

            bubbleRightGain.setEnabled(false);
            bubbleRightGain.setTrackColor(Color.argb(disAlpha,0,0,0));
            bubbleRightGain.setSecondTrackColor(Color.argb(disAlpha,0,0,0));
            bubbleRightGain.setBubbleColor(Color.argb(disAlpha,0,0,0));
            bubbleRightGain.setThumbColor(Color.argb(disAlpha,0,0,0));
            bubbleRightGain.setProgress(0);
        }
    }

    /**
     * Initializes the GUI
     */
    private void initializeGUI() {
        connectionStatus = findViewById(R.id.textConnectionStatus);
        statusImage = findViewById(R.id.imageStatus);
        connectionStatus.setText(R.string.Connecting3);
        buttonSaveMAP.setEnabled(true);

        //buttonStartStop = findViewById(R.id.toggleButtonStartStop);
        //buttonStartStop.setText(null);
        //buttonStartStop.setTextOn(null);
        //buttonStartStop.setTextOff(null);
        //buttonStartStop.setButtonDrawable(R.drawable.start0);
        //buttonStartStop.setEnabled(false);

        updateGUI("both");

        // left
        bubbleLeftSens.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListenerAdapter() {
            double value;

            @Override
            public void onProgressChanged(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {
                value = (double) progressFloat;
                value = (double) Math.round(value * 10d) / 10d;

            }

            @Override
            public void getProgressOnActionUp(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {
                leftMAP.sensitivity = value;
                leftScaleFactor = value / 32768;
                leftACE = new ACE(leftMAP);
                //Toast.makeText(getApplicationContext(), "Left Sensitivity value changed to " + value, Toast.LENGTH_SHORT).show();
                Snackbar.make(findViewById(R.id.rootMain), "Left Sensitivity value changed to " + value, Snackbar.LENGTH_SHORT).setAction("Action", null).show();

                // Update preferences value
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                SharedPreferences.Editor editor = preferences.edit();
                putDouble(editor, "Left.sensitivity", leftMAP.sensitivity);
                editor.apply();

            }

            @Override
            public void getProgressOnFinally(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {

            }
        });

        bubbleLeftGain.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListenerAdapter() {
            double value;

            @Override
            public void onProgressChanged(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {
                value = (double) progressFloat;
                value = (double) Math.round(value * 10d) / 10d;

            }

            @Override
            public void getProgressOnActionUp(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {
                leftMAP.gain = value;
                leftACE = new ACE(leftMAP);
                //Toast.makeText(getApplicationContext(), "Left Gain value changed to " + value + " dB", Toast.LENGTH_SHORT).show();
                Snackbar.make(findViewById(R.id.rootMain), "Left Gain value changed to " + value + " dB", Snackbar.LENGTH_SHORT).setAction("Action", null).show();

                // Update preferences value
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                SharedPreferences.Editor editor = preferences.edit();
                putDouble(editor, "Left.gain", leftMAP.gain);
                editor.apply();
            }

            @Override
            public void getProgressOnFinally(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {
            }
        });

        bubbleRightSens.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListenerAdapter() {
            double value;

            @Override
            public void onProgressChanged(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {
                value = (double) progressFloat;
                value = (double) Math.round(value * 10d) / 10d;

            }

            @Override
            public void getProgressOnActionUp(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {
                rightMAP.sensitivity = value;
                rightScaleFactor = value / 32768;
                rightACE = new ACE(rightMAP);
                //Toast.makeText(getApplicationContext(), "Right Sensitivity value changed to " + value, Toast.LENGTH_SHORT).show();
                Snackbar.make(findViewById(R.id.rootMain), "Right Sensitivity value changed to " + value, Snackbar.LENGTH_SHORT).setAction("Action", null).show();

                // Update preferences value
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                SharedPreferences.Editor editor = preferences.edit();
                putDouble(editor, "Right.sensitivity", rightMAP.sensitivity);
                editor.apply();
            }

            @Override
            public void getProgressOnFinally(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {

            }
        });

        bubbleRightGain.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListenerAdapter() {
            double value;

            @Override
            public void onProgressChanged(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {
                value = (double) progressFloat;
                value = (double) Math.round(value * 10d) / 10d;

            }

            @Override
            public void getProgressOnActionUp(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {
                rightMAP.gain = value;
                rightACE = new ACE(rightMAP);
                //Toast.makeText(getApplicationContext(), "Right Gain value changed to " + value + " dB", Toast.LENGTH_SHORT).show();
                Snackbar.make(findViewById(R.id.rootMain), "Right Gain value changed to " + value + " dB", Snackbar.LENGTH_SHORT).setAction("Action", null).show();

                // Update preferences value
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                SharedPreferences.Editor editor = preferences.edit();
                putDouble(editor, "Right.gain", rightMAP.gain);
                editor.apply();
            }

            @Override
            public void getProgressOnFinally(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {
            }
        });

    }

    /**
     * Updates the GUI
     * @param side s
     */
    private void updateGUI(String side) {
        if (side.equals("left") || side.equals("both")) {
            if (leftMAP.exists && !leftMAP.dataMissing) {
                enableSliders("left");
                bubbleLeftSens.setProgress((float) leftMAP.sensitivity);
                bubbleLeftGain.setProgress((float) leftMAP.gain);
                leftACE = new ACE(leftMAP);
                leftSensitivity.setText(R.string.textLeftSens);
                leftGain.setText(R.string.textLeftGain);
            } else {
                disableSliders("left");
                leftSensitivity.setText("");
                leftGain.setText("");
            }
        }
        if (side.equals("right") || side.equals("both")) {
            if (rightMAP.exists && !rightMAP.dataMissing) {
                enableSliders("right");
                bubbleRightGain.setProgress((float) rightMAP.gain);
                bubbleRightSens.setProgress((float) rightMAP.sensitivity);
                rightACE = new ACE(rightMAP);
                rightSensitivity.setText(R.string.textRightSens);
                rightGain.setText(R.string.textRightGain);
            } else {
                disableSliders("right");
                rightSensitivity.setText("");
                rightGain.setText("");
            }
        }
    }

    /**
     * Initializes the stimuli
     */
    private void initializeStimuli() {
        writeBuffer = new byte[516];
        nullWriteBuffer = new byte[516];
        sineBuffer = new byte[516];
        stimuli = new Stimuli();
        sine_stim = new short[8];
        sin_token = new short[8];
        null_token = new short[8];
        sine_token = new short[50];
        for (int i = 0; i < 8; ++i) {
            sin_token[i] = (short)(i + 1);
        }

        for (int i = 0; i < 50; ++i) {
            sine_token[i] = (short) (200 * Math.sin(2 * Math.PI * i * 0.01)); // this is one cycle of sine wave
        }
    }

    /**
     * Initializes the connection
     */
    private void initializeConnection() {
        availQ = new LinkedList<>();
        try{
            ftD2xx = D2xxManager.getInstance(this);
            s =new D2xxManager.DriverParameters();
            s.setBufferNumber(16);
            s.setReadTimeout(0);
        }
        catch (D2xxManager.D2xxException e) {
            Log.e("FTDI_HT", "getInstance fail!!");
        }
        //initializeStimuli();
        try {
            Thread.sleep(1000); }
        catch (InterruptedException e) {
            e.printStackTrace(); }
        int resultInitializeConnection = initializeDevice();
        textOut(Integer.toString(resultInitializeConnection));
    }

    /**
     * Initializes the device
     * @return int
     */
    private int initializeDevice() {
        connectFunction();
        ftDev.resetDevice();
        ftDev.clrRts();
        setConfig(baudRate, dataBit, stopBit, parity, flowControl);
        if ( ftDev == null ) {
            //status.append("ftDev is null");
            status.setText(R.string.textftDevnull);
            return 0;
        }
        else  {
            //status.append("ftDev is set");
            status.setText(R.string.textftDevset);
            return 1; }
    }

    /**
     * Restarts the board
     */
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
            textOut("config: ");
        } else {
            //midToast("DevCount<0", Toast.LENGTH_SHORT);
            textOut("DevCount<0");
        }

        try{
            ftD2xx = D2xxManager.getInstance(this);
            s =new D2xxManager.DriverParameters();
            s.setBufferNumber(16);
            s.setReadTimeout(0);
        }
        catch (D2xxManager.D2xxException e) {
            Log.e("FTDI_HT", "getInstance fail!!");
        }
        //status.append("BOARD IS READY NOW");
        status.setText(R.string.textBoardReadyNow);
    }

    /**
     * On board resume
     */
    protected void onResume() {
        super.onResume();
        if(null == ftDev || !ftDev.isOpen())
        {
            createDeviceList();
            if(DevCount > 0)
            {
                connectFunction();
                setConfig(baudRate, dataBit, stopBit, parity, flowControl);
            }
        }
    }

    /**
     * On board pause
     */
    protected void onPause() {
        super.onPause();
    }

    /**
     * On board stop
     */
    protected void onStop() {
        super.onStop();
    }

    /**
     * On board destroy
     */
    protected void onDestroy() {
        disconnectFunction();
        android.os.Process.killProcess(android.os.Process.myPid());
        super.onDestroy();
    }

    /**
     * Create the board device list
     */
    public void createDeviceList() {

    }

    /**
     * Disconnect
     */
    public void disconnectFunction() {
        DevCount = -1;
        currentPortIndex = -1;
        start = false;
        try    {  Thread.sleep(50);    }
        catch (InterruptedException e) {e.printStackTrace();}
        if(ftDev != null)  {
            if(ftDev.isOpen()) {
                ftDev.close();          }
        }
    }

    /**
     * Function that chooses the second device
     */
    public void connectFunction() {
        StringBuilder stringport = new StringBuilder("Port No.: ");
        if (portIndex + 1 > DevCount) {
            portIndex = 0;
        }

        if (currentPortIndex == portIndex && ftDev != null && ftDev.isOpen()) {
            stringport.append(String.valueOf(portIndex));
            textOut(stringport.toString());
            return;
        }

        if (null == ftDev) {
            ft_device_0 = ftD2xx.openByIndex(global_context, 0,s);
            ft_device_1 = ftD2xx.openByIndex(global_context, 1,s);
            ftDev = ft_device_0;
            ftDev.purge((byte)(D2xxManager.FT_PURGE_RX|D2xxManager.FT_PURGE_TX));
        }

        uart_configured = false;

        if(ftDev == null)
        {
            stringport.append(String.valueOf(portIndex));
            textOut(stringport.toString());
            return;
        }

        if (ftDev.isOpen())
        {
            currentPortIndex = portIndex;
            ftDev.purge(D2xxManager.FT_PURGE_RX);
        }
        else
        {
            stringport.append(String.valueOf(portIndex));
            textOut(stringport.toString());
        }
    }

    /**
     * Sets configuration
     * @param baud b
     * @param dataBits d
     * @param stopBits s
     * @param parity p
     * @param flowControl f
     */
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

    /**
     * Makes toast
     * @param str s
     * @param showTime st
     */
    void midToast(String str, int showTime) {
        Toast toast = Toast.makeText(global_context, str, showTime);
        toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL, 0, 0);

        TextView v = toast.getView().findViewById(android.R.id.message);
        v.setTextColor(Color.YELLOW);
        toast.show();
    }

    /**
     * Prints text
     * @param s s
     */
    public void textOut(final String s) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //status.append("\n" + s);
                status.setText(String.format("%s%s", getString(R.string.textStatusTextOut), s));
            }
        });
    }

    /**
     * Function for setting the output data buffer as per the given guidelines
     * @param outputBuffer ob
     * @param sine_token st
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

    /**
     * Function for setting the output data buffer as per the given guidelines
     * @param outputBuffer ob
     * @param left_map lm
     * @param right_map rm
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


    /**
     * When start/stop button is pressed
     * @param view v
     */
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

    public void buttonOnOff(View view) {
        if (((ToggleButton) view).isChecked()) {
            startProcessing();
        } else {
            stopProcessing();
        }
    }

    /**
     * Start processing
     */
    private void startProcessing() {
        start = true;
        portIndex = 1;
        if (ftDev == null){
            //status.append("DEVICE IS NULL");
            status.setText(R.string.textDeviceNull);
            //Toast.makeText(this,"Device not Connected. Please reconnect the board,",Toast.LENGTH_LONG).show();
            Snackbar.make(findViewById(R.id.rootMain), "Device not connected. Please reconnect the board.", Snackbar.LENGTH_LONG).setAction("Action", null).show();

        }
        else {
            status.setText(R.string.textRunning);
            setOutputBuffer(nullWriteBuffer, null_token); // zero buffer
            setOutputBuffer(writeBuffer, leftMAP, rightMAP); // zero buffer
            readThread1 = new readThread();
            readThread1.start();
        }
    }

    /**
     * Stop processing
     */
    private void stopProcessing() {
        readThread1.interrupt();
        status.setText(R.string.textStopped);
    }

    /**
     * Main core logic for reading and writing data
     */
    private class readThread  extends Thread
    {
        //Handler mHandler;
        readThread(){this.setPriority(Thread.MAX_PRIORITY);}

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
            int rc = ftDev.write(nullWriteBuffer, 516, true); // to get the board started
            rc = ftDev.write(nullWriteBuffer, 516, true);// to get the board started

            leftScaleFactor = leftMAP.sensitivity/32768;
            rightScaleFactor = rightMAP.sensitivity/32768;

            int k = 0;

            while(start) {
                if (Thread.interrupted()) {
                    start = false;
                    //stopStimulation = true;
                    sendnullframes();
                    break;
                }

                int iavailable_0 = ftDev.getQueueStatus();
                if(iavailable_0>=512) {

                    /*for (int j = 0; j < 8; ++j) {
                        sine_stim[j] = sine_token[k]; //200;
                        ++k; k %= 50;  }
                    setOutputBuffer(sineBuffer, sine_stim);*/

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

        /**
         * Updates the output buffer
         */
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

        /**
         * Sends null frames
         */
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