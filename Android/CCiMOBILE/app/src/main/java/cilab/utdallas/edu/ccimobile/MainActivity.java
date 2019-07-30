package cilab.utdallas.edu.ccimobile;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.OpenableColumns;
import android.util.JsonWriter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.ftdi.j2xx.D2xxManager;
import com.ftdi.j2xx.FT_Device;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.scichart.charting.model.dataSeries.XyDataSeries;
import com.scichart.charting.visuals.SciChartSurface;
import com.scichart.charting.visuals.axes.IAxis;
import com.scichart.charting.visuals.renderableSeries.FastColumnRenderableSeries;
import com.scichart.core.framework.UpdateSuspender;
import com.scichart.core.model.DoubleValues;
import com.scichart.drawing.utility.ColorUtil;
import com.scichart.extensions.builders.SciChartBuilder;
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
import java.util.Collections;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import static cilab.utdallas.edu.ccimobile.SharedHelper.getPermission;
import static cilab.utdallas.edu.ccimobile.SharedHelper.putDouble;

/**
 * The MainActivity class manages the home activity of the application
 */
public class MainActivity extends AppCompatActivity implements InitializationResultReceiver.Receiver {

    boolean settingsMono, settingsLeft, settingsRight, settingsStereo, folderExists;
    boolean noMAP = true; // No MAP selected yet
    boolean errorMAP = false;
    int disAlpha = 38; // opacity at 38% when item disabled

    Uri myFolder;
    public int[] electrodeStates;

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
    short[] sine_stim, sin_token, null_token, sine_token;
    
    private ACE leftACE, rightACE;

    public PatientMAP patientMAPleft, patientMAPright;

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

        getMainActivityViews();
        getMainActivityChart();
    }

    /**
     * Sets up views for MainActivity
     */
    public void getMainActivityViews() {
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
        buttonSaveMAP.setEnabled(false);

        disableSliders("both");

        leftSensitivity.setText(R.string.textLeftSens);
        leftGain.setText(R.string.textLeftGain);
        rightSensitivity.setText(R.string.textRightSens);
        rightGain.setText(R.string.textRightGain);
    }

    /**
     * Sets up SciChart for MainActivity
     */
    public void getMainActivityChart() {
        // chart
        SciChartSurface surface = findViewById(R.id.chartSurface);

        surface.setTheme(R.style.SciChart_Bright_Spark);

        // Licensing SciChartSurface
        try {
            SciChartSurface.setRuntimeLicenseKeyFromResource(this, "app\\src\\main\\res\\raw\\license.xml");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Initialize the SciChartBuilder
        SciChartBuilder.init(this);

        // Obtain the SciChartBuilder instance
        final SciChartBuilder sciChartBuilder = SciChartBuilder.instance();

        // Create a numeric X axis
        final IAxis xAxis = sciChartBuilder.newNumericAxis()
                .withAxisTitle("Channel")
                .withVisibleRange(1, 22)
                .build();

        // Create a numeric Y axis
        final IAxis yAxis = sciChartBuilder.newNumericAxis() // 250 max
                .withAxisTitle("Clinical Level")
                .withVisibleRange(0, 250)
                .build();

        // Add the Y axis to the YAxes collection of the surface
        Collections.addAll(surface.getYAxes(), yAxis);

        // Add the X axis to the XAxes collection of the surface
        Collections.addAll(surface.getXAxes(), xAxis);

        final XyDataSeries lineData = sciChartBuilder.newXyDataSeries(Integer.class, Double.class).build();
        final int dataCount = 22;

        // initialize with max values
        for (int i = 0; i < dataCount; i++)
        {
            lineData.append(i+1, (double) 250);
        }

        // Set up an update
        final DoubleValues lineDoubleData = new DoubleValues(dataCount);
        lineDoubleData.setSize(dataCount);

        TimerTask updateDataTask = new TimerTask() {
            @Override
            public void run() {
                UpdateSuspender.using(surface, () -> {
                    // Clear data
                    for (int i = 0; i < dataCount; i++) {
                        lineDoubleData.set(i, 0);
                    }

                    // Put in active electrodes & current values (convert to channels) for first nMaxima
                    for (int i = 0; i < patientMAPleft.getnMaxima(); i++) {
                        lineDoubleData.set(dataCount-leftStimuli.Electrodes[i], leftStimuli.Amplitudes[i]);
                    }

                    // Update DataSeries using bunch update
                    lineData.updateRangeYAt(0, lineDoubleData);
                    //surface.zoomExtents();
                });
            }
        };

        Timer timer = new Timer();
        long delay = 0;
        long interval = 50; // updates every X ms
        timer.schedule(updateDataTask, delay, interval);

        // Create and configure the Column Chart Series
        final FastColumnRenderableSeries columnSeries = sciChartBuilder.newColumnSeries()
                .withStrokeStyle(0xA99A8A)
                .withDataPointWidth(1)
                .withLinearGradientColors(ColorUtil.LightSteelBlue, ColorUtil.SteelBlue)
                .withDataSeries(lineData)
                .build();

        // Add the chart series to the RenderableSeriesCollection of the surface
        Collections.addAll(surface.getRenderableSeries(), columnSeries);

        // Should be called at the end of chart set up
        surface.zoomExtents();
    }

    /**
     * Called when the SELECT MAP button is pressed. It checks for permission from the user to
     * access external storage on the phone (to retrieve the MAP text files). Then it calls
     * performFileSearch to open the MAP file.
     * @param view view
     */
    public void selectMAP(View view) {
        if (getPermission(this, this, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE)) {
            performFileSearch();
        }
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
            if (!patientMAPleft.isDataMissing() && !patientMAPright.isDataMissing()) {
                updateGUI("both");
            }
        }
        //verifyFolderExists();
    }

    /**
     * Receives result from requesting permission (for file access).
     * @param requestCode request code
     * @param permissions permission
     * @param grantResults result
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission granted
                Log.e("MainActivity.java", "Permission GRANTED for " +
                        "WRITE_EXTERNAL_STORAGE.");
                //verifyFolderExists();
                performFileSearch();
            } else {
                // permission denied
                Log.e("MainActivity.java", "Permission DENIED for " +
                        "WRITE_EXTERNAL_STORAGE.");
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
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
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
        MaterialAlertDialogBuilder saveAlert = new MaterialAlertDialogBuilder(this);
        saveAlert.setTitle("Save MAP");
        View viewInflated = LayoutInflater.from(this).inflate(R.layout.text_input_filename, findViewById(android.R.id.content), false);
        final EditText input = viewInflated.findViewById(R.id.input);
        saveAlert.setView(viewInflated);

        saveAlert.setPositiveButton("Save", (dialog, which) -> {
            dialog.dismiss();
            String saveFilename = input.getText().toString();
            saveMAP(saveFilename);
        });
        saveAlert.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        saveAlert.show();
    }

    /**
     * Saves a MAP in JSON format on the phone
     * @param out o
     * @throws IOException e
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void writeJsonStream(OutputStream out) throws IOException {
        JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
        writer.setIndent("  ");

        writer.beginObject(); // first {

        writeJSONGeneral(writer);

        if (patientMAPleft.isExists())
            writeJSONMAP(writer, patientMAPleft, "Left");
        if (patientMAPright.isExists())
            writeJSONMAP(writer, patientMAPright, "Right");

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
    public void writeJSONMAP(JsonWriter writer, PatientMAP map, String side) throws IOException {
        writer.name(side);

        writer.beginArray();
        writer.beginObject();

        writer.name(side + ".implantType").value(map.getImplantType());
        writer.name(side + ".samplingFrequency").value(map.getSamplingFrequency());
        writer.name(side + ".numberOfChannels").value(map.getNumberOfChannels());
        writer.name(side + ".soundProcessingStrategy").value(map.getSoundProcessingStrategy());
        writer.name(side + ".nMaxima").value(map.getnMaxima());
        writer.name(side + ".stimulationMode").value(map.getStimulationMode());
        writer.name(side + ".stimulationRate").value(map.getStimulationRate());
        writer.name(side + ".pulseWidth").value(map.getPulseWidth());
        writer.name(side + ".sensitivity").value(map.getSensitivity());
        writer.name(side + ".gain").value(map.getGain());
        writer.name(side + ".volume").value(map.getVolume());
        writer.name(side + ".Qfactor").value(map.getQfactor());
        writer.name(side + ".baseLevel").value(map.getBaseLevel());
        writer.name(side + ".saturationLevel").value(map.getSaturationLevel());
        writer.name(side + ".stimulationOrder").value(map.getStimulationOrder());
        writer.name(side + ".frequencyTable").value(map.getFrequencyTable());
        writer.name(side + ".window").value(map.getWindow());

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
    public void writeJSONElectrodes(JsonWriter writer, PatientMAP map) throws IOException {
        writer.beginArray();
        int numElectrodes = 22;

        int[] THR = map.getTHR();
        int[] MCL = map.getMCL();
        double[] gains = map.getGains();
        double[] lowerCutOffFrequencies = map.getLowerCutOffFrequencies();
        double[] higherCutOffFrequencies = map.getHigherCutOffFrequencies();

        for (int i = 0; i < numElectrodes; i++) {
            writer.beginObject();
            writer.name("electrodes").value(numElectrodes - i);
            writer.name("lowerCutOffFrequencies").value(lowerCutOffFrequencies[i]);
            writer.name("higherCutOffFrequencies").value(higherCutOffFrequencies[i]);
            writer.name("THR").value(THR[i]);
            writer.name("MCL").value(MCL[i]);
            writer.name("gains").value(gains[i]);
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
        // Check if external storage is available
        if (isExternalStorageWritable()) {
            File folder = new File(Environment.getExternalStorageDirectory() +
                    File.separator + "CCi-MOBILE MAPs");
            myFolder = Uri.fromFile(folder);

            boolean success = true;
            if (!folder.exists()) {
                success = folder.mkdirs();
                Snackbar.make(findViewById(R.id.rootMain), "Created folder in phone storage: 'CCi-MOBILE MAPs.'", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
            folderExists = success;
        } else {
            Snackbar.make(findViewById(R.id.rootMain), "Error: External phone storage unavailable. Cannot read or write to MAPs.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
        }
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
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
                myIntent.putExtra("PatientMAPleft", patientMAPleft);
                myIntent.putExtra("PatientMAPright", patientMAPright);
                MainActivity.this.startActivityForResult(myIntent, RETURN_FROM_SETTINGS);
                return true;
            case R.id.menuEnvironments:
                myIntent = new Intent(MainActivity.this, EnvironmentsActivity.class);
                MainActivity.this.startActivityForResult(myIntent, 1);
                return true;
            case R.id.moreInfo:
                myIntent = new Intent(MainActivity.this, HowToUseActivity.class);
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
        if (patientMAPleft.isExists()) {
            PatientMAP pmapleft = data.getParcelableExtra("PatientMAPleft");
            patientMAPleft.updateChangedParameters(pmapleft);

            bubbleLeftSens.setProgress((float) patientMAPleft.getSensitivity());
            bubbleLeftGain.setProgress((float) patientMAPleft.getGain());

            // re-initialize ACE
            leftACE = new ACE(patientMAPleft);
        }
        if (patientMAPright.isExists()) {
            PatientMAP pmapright = data.getParcelableExtra("PatientMAPright");
            patientMAPright.updateChangedParameters(pmapright);

            bubbleRightSens.setProgress((float) patientMAPright.getSensitivity());
            bubbleRightGain.setProgress((float) patientMAPright.getGain());

            // re-initialize ACE
            rightACE = new ACE(patientMAPright);
        }
        updateOutputBufferMAP();
    }

    /**
     * Updates the output buffer.
     */
    void updateOutputBufferMAP() {
        if (writeBuffer != null)
        {
            if (patientMAPleft.isExists() && patientMAPright.isExists()) { // both left and right
                setOutputBuffer(writeBuffer, patientMAPleft, patientMAPright);
            } else if (patientMAPleft.isExists()) { // only left
                setOutputBuffer(writeBuffer, patientMAPleft, patientMAPleft); // zero buffer
            } else if (patientMAPright.isExists()) { // only right
                setOutputBuffer(writeBuffer, patientMAPright, patientMAPright); // zero buffer
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

                buttonOnOff.setEnabled(true);
                buttonOnOff.setChecked(false);

                connectionStatus.setText(R.string.textConnected);
                statusImage.setImageResource(R.drawable.ic_done_black_24dp);
                break;

            case InitializationService.STATUS_ERROR:
                /* Handle the error */
                String error = resultData.getString(Intent.EXTRA_TEXT);
                Snackbar.make(findViewById(R.id.rootMain), "Error: " + error, Snackbar.LENGTH_LONG).setAction("Action", null).show();

                break;
        }
    }

    /**
     * Initializes MAPs, stimuli, and GUI.
     */
    private void initialize() {
        initializeMAP();
        if (!patientMAPleft.isDataMissing() && !patientMAPright.isDataMissing()) {
            initializeStimuli();
            initializeGUI();
        }
    }

    /**
     * Initializes the MAPs.
     */
    private void initializeMAP() {
        // here
        patientMAPleft = new PatientMAP();
        patientMAPright = new PatientMAP();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String MAP_filename;
        MAP_filename = preferences.getString("MAPfilename","");

        // Check if filename string is empty
        assert MAP_filename != null;
        if (!MAP_filename.isEmpty()) {
            patientMAPleft.getMAPData(MAP_filename, "left");
            patientMAPright.getMAPData(MAP_filename, "right");

            if (patientMAPleft.isDataMissing() || patientMAPright.isDataMissing()) {
                errorMAP = true;
                disableSliders("both");
                Snackbar.make(findViewById(R.id.rootMain), "Error: Left and/or right MAP data missing. Please select a valid MAP.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            } else {
                errorMAP = false;
                if (patientMAPleft.isExists() || patientMAPright.isExists())
                    writeMAPToPreferences();
            }
        }
        else {
            Snackbar.make(findViewById(R.id.rootMain), "Error: Empty MAP. Please select a valid MAP.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
        }
    }

    /**
     * Saves MAP parameters to Preferences. Called when a new MAP is selected.
     * Saving to Preferences for ParametersFragment.
     */
    public void writeMAPToPreferences() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();

        if (patientMAPleft.isExists()) {
            editor.putInt("leftMAPnbands",patientMAPleft.getNbands());
            editor.putBoolean("leftMapExists", true);
            editor.putString("Left.implantType", patientMAPleft.getImplantType());
            editor.putInt("Left.samplingFrequency", patientMAPleft.getSamplingFrequency());
            editor.putInt("Left.numberOfChannels", patientMAPleft.getNumberOfChannels());
            editor.putString("Left.frequencyTable", patientMAPleft.getFrequencyTable());
            editor.putString("Left.soundProcessingStrategy", patientMAPleft.getSoundProcessingStrategy());
            editor.putInt("Left.nMaxima", patientMAPleft.getnMaxima());
            editor.putString("Left.stimulationMode", patientMAPleft.getStimulationMode());
            editor.putInt("Left.stimulationRate", patientMAPleft.getStimulationRate());
            editor.putInt("Left.pulseWidth", patientMAPleft.getPulseWidth());
            putDouble(editor, "Left.sensitivity", patientMAPleft.getSensitivity());
            putDouble(editor, "Left.gain", patientMAPleft.getGain());
            editor.putInt("Left.volume", patientMAPleft.getVolume());
            putDouble(editor, "Left.Qfactor", patientMAPleft.getQfactor());
            putDouble(editor, "Left.baseLevel", patientMAPleft.getBaseLevel());
            putDouble(editor, "Left.saturationLevel", patientMAPleft.getSaturationLevel());
            editor.putString("Left.stimulationOrder", patientMAPleft.getStimulationOrder());
            editor.putString("Left.window", patientMAPleft.getWindow());

            // Getting electrode array
            for (int i = 0; i < patientMAPleft.getNbands(); i++ ) {
                editor.putInt("leftTHR" + i, patientMAPleft.getTHR(i));
                editor.putInt("leftMCL" + i, patientMAPleft.getMCL(i));
                putDouble(editor,"leftgain" + i, patientMAPleft.getGains(i));
                editor.putInt("leftelectrodes" + i, patientMAPleft.getElectrodes(i));
            }

            leftACE = new ACE(patientMAPleft);

        } else {
            editor.putBoolean("leftMapExists", false);
        }
        
        if (patientMAPright.isExists()) {
            editor.putInt("rightMAPnbands",patientMAPright.getNbands());
            editor.putBoolean("rightMapExists", true);
            editor.putString("Right.implantType", patientMAPright.getImplantType());
            editor.putInt("Right.samplingFrequency", patientMAPright.getSamplingFrequency());
            editor.putInt("Right.numberOfChannels", patientMAPright.getNumberOfChannels());
            editor.putString("Right.frequencyTable", patientMAPright.getFrequencyTable());
            editor.putString("Right.soundProcessingStrategy", patientMAPright.getSoundProcessingStrategy());
            editor.putInt("Right.nMaxima", patientMAPright.getnMaxima());
            editor.putString("Right.stimulationMode", patientMAPright.getStimulationMode());
            editor.putInt("Right.stimulationRate", patientMAPright.getStimulationRate());
            editor.putInt("Right.pulseWidth", patientMAPright.getPulseWidth());
            putDouble(editor, "Right.sensitivity", patientMAPright.getSensitivity());
            putDouble(editor, "Right.gain", patientMAPright.getGain());
            editor.putInt("Right.volume", patientMAPright.getVolume());
            putDouble(editor, "Right.Qfactor", patientMAPright.getQfactor());
            putDouble(editor, "Right.baseLevel", patientMAPright.getBaseLevel());
            putDouble(editor, "Right.saturationLevel", patientMAPright.getSaturationLevel());
            editor.putString("Right.stimulationOrder", patientMAPright.getStimulationOrder());
            editor.putString("Right.window", patientMAPright.getWindow());

            // Getting electrode array
            for (int i = 0; i < patientMAPright.getNbands(); i++ ) {
                editor.putInt("rightTHR" + i, patientMAPright.getTHR(i));
                editor.putInt("rightMCL" + i, patientMAPright.getMCL(i));
                putDouble(editor,"rightgain" + i, patientMAPright.getGains(i));
                editor.putInt("rightelectrodes" + i, patientMAPright.getElectrodes(i));
            }

            rightACE = new ACE(patientMAPright);

        } else {
            editor.putBoolean("rightMapExists", false);
        }

        if (patientMAPleft.isExists() && patientMAPright.isExists()) { // assign audio recording settings
            settingsMono = false;
            settingsLeft = false;
            settingsRight = false;
            settingsStereo = true;
        } else if (patientMAPleft.isExists()) {
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
                patientMAPleft.setSensitivity(value);
                leftScaleFactor = value / 32768;
                leftACE = new ACE(patientMAPleft);
                Snackbar.make(findViewById(R.id.rootMain), "Left Sensitivity value changed to " + value, Snackbar.LENGTH_SHORT).setAction("Action", null).show();

                // Update preferences value
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                SharedPreferences.Editor editor = preferences.edit();
                putDouble(editor, "Left.sensitivity", patientMAPleft.getSensitivity());
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
                patientMAPleft.setGain(value);
                leftACE = new ACE(patientMAPleft);
                Snackbar.make(findViewById(R.id.rootMain), "Left Gain value changed to " + value + " dB", Snackbar.LENGTH_SHORT).setAction("Action", null).show();

                // Update preferences value
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                SharedPreferences.Editor editor = preferences.edit();
                putDouble(editor, "Left.gain", patientMAPleft.getGain());
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
                patientMAPright.setSensitivity(value);
                rightScaleFactor = value / 32768;
                rightACE = new ACE(patientMAPright);
                Snackbar.make(findViewById(R.id.rootMain), "Right Sensitivity value changed to " + value, Snackbar.LENGTH_SHORT).setAction("Action", null).show();

                // Update preferences value
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                SharedPreferences.Editor editor = preferences.edit();
                putDouble(editor, "Right.sensitivity", patientMAPright.getSensitivity());
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
                patientMAPright.setGain(value);
                rightACE = new ACE(patientMAPright);
                Snackbar.make(findViewById(R.id.rootMain), "Right Gain value changed to " + value + " dB", Snackbar.LENGTH_SHORT).setAction("Action", null).show();

                // Update preferences value
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                SharedPreferences.Editor editor = preferences.edit();
                putDouble(editor, "Right.gain", patientMAPright.getGain());
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
            if (patientMAPleft.isExists() && !patientMAPleft.isDataMissing()) {
                enableSliders("left");
                bubbleLeftSens.setProgress((float) patientMAPleft.getSensitivity());
                bubbleLeftGain.setProgress((float) patientMAPleft.getGain());
                leftACE = new ACE(patientMAPleft);
                leftSensitivity.setText(R.string.textLeftSens);
                leftGain.setText(R.string.textLeftGain);
            } else {
                disableSliders("left");
                leftSensitivity.setText("");
                leftGain.setText("");
            }
        }
        if (side.equals("right") || side.equals("both")) {
            if (patientMAPright.isExists() && !patientMAPright.isDataMissing()) {
                enableSliders("right");
                bubbleRightGain.setProgress((float) patientMAPright.getGain());
                bubbleRightSens.setProgress((float) patientMAPright.getSensitivity());
                rightACE = new ACE(patientMAPright);
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
            //status.setText(R.string.textftDevset);
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
            textOut("config: ");
        } else {
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
            stringport.append(portIndex);
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
            stringport.append(portIndex);
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
            stringport.append(portIndex);
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
     * Prints text
     * @param s s
     */
    public void textOut(final String s) {
        runOnUiThread(() -> {
            //status.append("\n" + s);
            //status.setText(String.format("%s%s", getString(R.string.textStatusTextOut), s));
        });
    }

    /**
     * Function for setting the output data buffer as per the given guidelines
     * @param outputBuffer ob
     * @param sine_token st
     */
    private void setOutputBuffer(byte[] outputBuffer, short[] sine_token) {

        short n = 8;
        short[] electrode_token = new short[8]/*, sine_token[8]*/;
        for (short i = 0; i < n; ++i) {
            electrode_token[i] = (short)(i + 1);
            //sine_token[i] = i + 1;
        }
        short ppf = 64;
        short pw = 25;

        short mode = 28;
        short[] elecs = new short[64];
        short[] amps = new short[64];
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
    private void setOutputBuffer(byte[] outputBuffer, PatientMAP left_map, PatientMAP right_map) {

        short left_pw = (short)left_map.getPulseWidth();
        short right_pw = (short)right_map.getPulseWidth();

        short left_ppf = (short)left_map.getPulsesPerFrame();
        short right_ppf = (short)right_map.getPulsesPerFrame();

        short left_nRFcycles = (short)left_map.getnRFcycles();
        short right_nRFcycles = (short)right_map.getnRFcycles();

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

        outputBuffer[380] = (byte)left_map.getStimulationModeCode(); //mode left
        outputBuffer[381] = (byte)right_map.getStimulationModeCode(); //mode right
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
            status.setText(R.string.textDeviceNull);
            Snackbar.make(findViewById(R.id.rootMain), "Device not connected. Please reconnect the board.", Snackbar.LENGTH_LONG).setAction("Action", null).show();

        }
        else {
            status.setText(R.string.textRunning);
            setOutputBuffer(nullWriteBuffer, null_token); // zero buffer
            setOutputBuffer(writeBuffer, patientMAPleft, patientMAPright); // zero buffer
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

            leftScaleFactor = patientMAPleft.getSensitivity()/32768;
            rightScaleFactor = patientMAPright.getSensitivity()/32768;

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
            for (int i = 0; i < patientMAPleft.getPulsesPerFrame(); ++i) {
                writeBuffer[i + 6] = (byte)leftStimuli.Electrodes[i];
                writeBuffer[i + 132] = (byte)leftStimuli.Amplitudes[i];
            }
            for (int i = 0; i < patientMAPright.getPulsesPerFrame(); ++i) {
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