package cilab.utdallas.edu.ccimobile;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)

public class SettingsActivity extends AppCompatActivity {

    int max_stimulation_rate = 14400;
    int min_stimulation_rate = 125;

    int max_pulsewidth = 400;
    int min_pulsewidth = 25;

    int max_sensitivity = 10;
    int min_sensitivity = 0;

    int max_gain = 50;
    int min_gain = 0;

    int max_Qfactor = 100;
    int min_Qfactor = 0;

    int max_baselevel = 1;
    int min_baselevel = 0;

    int max_saturationlevel = 1;
    int min_saturationlevel = 0;

    String leftMAPimplantType,
            leftMAPfrequencyTable,
            leftMAPsoundProcessingStrategy,
            leftMAPstimulationMode,
            leftMAPstimulationOrder,
            leftMAPwindow,
            rightMAPimplantType,
            rightMAPfrequencyTable,
            rightMAPsoundProcessingStrategy,
            rightMAPstimulationMode,
            rightMAPstimulationOrder,
            rightMAPwindow,
            leftImplantGeneration,
            rightImplantGeneration;

    int leftMAPsamplingFrequency,
            leftMAPnumberOfChannels,
            leftMAPnMaxima,
            leftMAPstimulationRate,
            leftMAPpulseWidth,
            leftMAPvolume,
            leftMAPnbands,
            rightMAPsamplingFrequency,
            rightMAPnumberOfChannels,
            rightMAPnMaxima,
            rightMAPstimulationRate,
            rightMAPpulseWidth,
            rightMAPvolume,
            rightMAPnbands,
            leftStimulationModeCode,
            rightStimulationModeCode,
            leftPulsesPerFramePerChannel,
            rightPulsesPerFramePerChannel,
            leftPulsesPerFrame,
            rightPulsesPerFrame,
            leftnRFcycles,
            rightnRFcycles;

    double leftMAPsensitivity,
            leftMAPgain,
            leftMAPQfactor,
            leftMAPbaseLevel,
            leftMAPsaturationLevel,
            rightMAPsensitivity,
            rightMAPgain,
            rightMAPQfactor,
            rightMAPbaseLevel,
            rightMAPsaturationLevel,
            leftInterpulseDuration,
            rightInterpulseDuration;

    private final int leftInterPhaseGap = 8;            //Interphase Gap is 8us
    private final int rightInterPhaseGap = 8;            //Interphase Gap is 8us

    private final int leftDurationSYNC = 6;             //Duration of Sycn Toekn in uS
    private final int rightDurationSYNC = 6;             //Duration of Sycn Toekn in uS

    private final int leftAdditionalGap = 1;            //additional gap to make interpulse duration 7 uS ref. Fig. 14 in CIC4 specification
    private final int rightAdditionalGap = 1;            //additional gap to make interpulse duration 7 uS ref. Fig. 14 in CIC4 specification

    int[] leftMAPTHR, rightMAPTHR, leftMAPMCL, rightMAPMCL, leftMAPelectrodes, rightMAPelectrodes;
    double[] leftMAPgains, rightMAPgains;

    boolean leftExists, rightExists;

    int disabledAlpha = 38;

    // Define TextViews
    private TextView textView18, textView19, textView20, textView21, textView22, textView23,
            textView24, textView25, textView38, textView39, textView40, textView41, textView42,
            textView43, textView46, textView47, textView48, textView49, textView50, textView51,
            textView54, textView55, textView56, textView57, textView58, textView59, textView62,
            textView63, textView64, textView65, textView66, textView67, textView70, textView71,
            textView72, textView73, textView74, textView75, textView78, textView79, textView80,
            textView81, textView82, textView83, textView86, textView87, textView88, textView89,
            textView90, textView91, textView94, textView95, textView96, textView97, textView98,
            textView99, textView102, textView103, textView104, textView105, textView106,
            textView107, textView110, textView111, textView112, textView113, textView114,
            textView115, textView118, textView119, textView120, textView121, textView122,
            textView123, textView126, textView127, textView128, textView129, textView130,
            textView131, textView134, textView135, textView136, textView137, textView138,
            textView139, textView142, textView143, textView144, textView145, textView146,
            textView147, textView150, textView151, textView152, textView153, textView154,
            textView155, textView158, textView159, textView160, textView161, textView162,
            textView163, textView166, textView167, textView168, textView169, textView170,
            textView171, textView174, textView175, textView176, textView177, textView178,
            textView179, textView182, textView183, textView184, textView185, textView186,
            textView187, textView190, textView191, textView192, textView193, textView194,
            textView195, textView198, textView199, textView200, textView201, textView202,
            textView203, textView206, textView207, textView208, textView209, textView210,
            textView211;

    // Define EditTexts
    private EditText editText4, editText5, editText6, editText7, editText9, editText10, editText11,
            editText17, editText18, editText19, editText20, editText22, editText23, editText24;

    // Define Spinners
    private Spinner volumeSpinnerLeft, volumeSpinnerRight, soundProcessSpinnerLeft,
            soundProcessSpinnerRight, stimOrderLeft, stimOrderRight, stimModeLeft, stimModeRight,
            windowSpinnerLeft, windowSpinnerRight, nMaximaSpinnerLeft, nMaximaSpinnerRight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setUpWidgets();
        getMAPFromPreferences();
    }

    void setUpWidgets() {
        // Retrieve the widgets
        textView18 = (TextView) findViewById(R.id.textView18);
        textView19 = (TextView) findViewById(R.id.textView19);
        textView20 = (TextView) findViewById(R.id.textView20);
        textView21 = (TextView) findViewById(R.id.textView21);
        textView22 = (TextView) findViewById(R.id.textView22);
        textView23 = (TextView) findViewById(R.id.textView23);
        textView24 = (TextView) findViewById(R.id.textView24);
        textView25 = (TextView) findViewById(R.id.textView25);

        textView38 = (TextView) findViewById(R.id.textView38);
        textView46 = (TextView) findViewById(R.id.textView46);
        textView54 = (TextView) findViewById(R.id.textView54);
        textView62 = (TextView) findViewById(R.id.textView62);
        textView70 = (TextView) findViewById(R.id.textView70);
        textView78 = (TextView) findViewById(R.id.textView78);
        textView86 = (TextView) findViewById(R.id.textView86);
        textView94 = (TextView) findViewById(R.id.textView94);
        textView102 = (TextView) findViewById(R.id.textView102);
        textView110 = (TextView) findViewById(R.id.textView110);
        textView118 = (TextView) findViewById(R.id.textView118);
        textView126 = (TextView) findViewById(R.id.textView126);
        textView134 = (TextView) findViewById(R.id.textView134);
        textView142 = (TextView) findViewById(R.id.textView142);
        textView150 = (TextView) findViewById(R.id.textView150);
        textView158 = (TextView) findViewById(R.id.textView158);
        textView166 = (TextView) findViewById(R.id.textView166);
        textView174 = (TextView) findViewById(R.id.textView174);
        textView182 = (TextView) findViewById(R.id.textView182);
        textView190 = (TextView) findViewById(R.id.textView190);
        textView198 = (TextView) findViewById(R.id.textView198);
        textView206 = (TextView) findViewById(R.id.textView206);

        textView39 = (TextView) findViewById(R.id.textView39);
        textView47 = (TextView) findViewById(R.id.textView47);
        textView55 = (TextView) findViewById(R.id.textView55);
        textView63 = (TextView) findViewById(R.id.textView63);
        textView71 = (TextView) findViewById(R.id.textView71);
        textView79 = (TextView) findViewById(R.id.textView79);
        textView87 = (TextView) findViewById(R.id.textView87);
        textView95 = (TextView) findViewById(R.id.textView95);
        textView103 = (TextView) findViewById(R.id.textView103);
        textView111 = (TextView) findViewById(R.id.textView111);
        textView119 = (TextView) findViewById(R.id.textView119);
        textView127 = (TextView) findViewById(R.id.textView127);
        textView135 = (TextView) findViewById(R.id.textView135);
        textView143 = (TextView) findViewById(R.id.textView143);
        textView151 = (TextView) findViewById(R.id.textView151);
        textView159 = (TextView) findViewById(R.id.textView159);
        textView167 = (TextView) findViewById(R.id.textView167);
        textView175 = (TextView) findViewById(R.id.textView175);
        textView183 = (TextView) findViewById(R.id.textView183);
        textView191 = (TextView) findViewById(R.id.textView191);
        textView199 = (TextView) findViewById(R.id.textView199);
        textView207 = (TextView) findViewById(R.id.textView207);

        textView40 = (TextView) findViewById(R.id.textView40);
        textView48 = (TextView) findViewById(R.id.textView48);
        textView56 = (TextView) findViewById(R.id.textView56);
        textView64 = (TextView) findViewById(R.id.textView64);
        textView72 = (TextView) findViewById(R.id.textView72);
        textView80 = (TextView) findViewById(R.id.textView80);
        textView88 = (TextView) findViewById(R.id.textView88);
        textView96 = (TextView) findViewById(R.id.textView96);
        textView104 = (TextView) findViewById(R.id.textView104);
        textView112 = (TextView) findViewById(R.id.textView112);
        textView120 = (TextView) findViewById(R.id.textView120);
        textView128 = (TextView) findViewById(R.id.textView128);
        textView136 = (TextView) findViewById(R.id.textView136);
        textView144 = (TextView) findViewById(R.id.textView144);
        textView152 = (TextView) findViewById(R.id.textView152);
        textView160 = (TextView) findViewById(R.id.textView160);
        textView168 = (TextView) findViewById(R.id.textView168);
        textView176 = (TextView) findViewById(R.id.textView176);
        textView184 = (TextView) findViewById(R.id.textView184);
        textView192 = (TextView) findViewById(R.id.textView192);
        textView200 = (TextView) findViewById(R.id.textView200);
        textView208 = (TextView) findViewById(R.id.textView208);

        editText4 = (EditText) findViewById(R.id.editText4);
        editText5 = (EditText) findViewById(R.id.editText5);
        editText6 = (EditText) findViewById(R.id.editText6);
        editText7 = (EditText) findViewById(R.id.editText7);
        editText9 = (EditText) findViewById(R.id.editText9);
        editText10 = (EditText) findViewById(R.id.editText10);
        editText11 = (EditText) findViewById(R.id.editText11);
        editText17 = (EditText) findViewById(R.id.editText17);
        editText18 = (EditText) findViewById(R.id.editText18);
        editText19 = (EditText) findViewById(R.id.editText19);
        editText20 = (EditText) findViewById(R.id.editText20);
        editText22 = (EditText) findViewById(R.id.editText22);
        editText23 = (EditText) findViewById(R.id.editText23);
        editText24 = (EditText) findViewById(R.id.editText24);

        textView41 = (TextView) findViewById(R.id.textView41);
        textView49 = (TextView) findViewById(R.id.textView49);
        textView57 = (TextView) findViewById(R.id.textView57);
        textView65 = (TextView) findViewById(R.id.textView65);
        textView73 = (TextView) findViewById(R.id.textView73);
        textView79 = (TextView) findViewById(R.id.textView79);
        textView81 = (TextView) findViewById(R.id.textView81);
        textView89 = (TextView) findViewById(R.id.textView89);
        textView97 = (TextView) findViewById(R.id.textView97);
        textView105 = (TextView) findViewById(R.id.textView105);
        textView113 = (TextView) findViewById(R.id.textView113);
        textView121 = (TextView) findViewById(R.id.textView121);
        textView129 = (TextView) findViewById(R.id.textView129);
        textView137 = (TextView) findViewById(R.id.textView137);
        textView145 = (TextView) findViewById(R.id.textView145);
        textView153 = (TextView) findViewById(R.id.textView153);
        textView161 = (TextView) findViewById(R.id.textView161);
        textView169 = (TextView) findViewById(R.id.textView169);
        textView177 = (TextView) findViewById(R.id.textView177);
        textView185 = (TextView) findViewById(R.id.textView185);
        textView193 = (TextView) findViewById(R.id.textView193);
        textView201 = (TextView) findViewById(R.id.textView201);
        textView209 = (TextView) findViewById(R.id.textView209);

        textView42 = (TextView) findViewById(R.id.textView42);
        textView50 = (TextView) findViewById(R.id.textView50);
        textView58 = (TextView) findViewById(R.id.textView58);
        textView66 = (TextView) findViewById(R.id.textView66);
        textView74 = (TextView) findViewById(R.id.textView74);
        textView79 = (TextView) findViewById(R.id.textView79);
        textView82 = (TextView) findViewById(R.id.textView82);
        textView90 = (TextView) findViewById(R.id.textView90);
        textView98 = (TextView) findViewById(R.id.textView98);
        textView106 = (TextView) findViewById(R.id.textView106);
        textView114 = (TextView) findViewById(R.id.textView114);
        textView122 = (TextView) findViewById(R.id.textView122);
        textView130 = (TextView) findViewById(R.id.textView130);
        textView138 = (TextView) findViewById(R.id.textView138);
        textView146 = (TextView) findViewById(R.id.textView146);
        textView154 = (TextView) findViewById(R.id.textView154);
        textView162 = (TextView) findViewById(R.id.textView162);
        textView170 = (TextView) findViewById(R.id.textView170);
        textView178 = (TextView) findViewById(R.id.textView178);
        textView186 = (TextView) findViewById(R.id.textView186);
        textView194 = (TextView) findViewById(R.id.textView194);
        textView202 = (TextView) findViewById(R.id.textView202);
        textView210 = (TextView) findViewById(R.id.textView210);

        textView43 = (TextView) findViewById(R.id.textView43);
        textView51 = (TextView) findViewById(R.id.textView51);
        textView59 = (TextView) findViewById(R.id.textView59);
        textView67 = (TextView) findViewById(R.id.textView67);
        textView75 = (TextView) findViewById(R.id.textView75);
        textView79 = (TextView) findViewById(R.id.textView79);
        textView83 = (TextView) findViewById(R.id.textView83);
        textView91 = (TextView) findViewById(R.id.textView91);
        textView99 = (TextView) findViewById(R.id.textView99);
        textView107 = (TextView) findViewById(R.id.textView107);
        textView115 = (TextView) findViewById(R.id.textView115);
        textView123 = (TextView) findViewById(R.id.textView123);
        textView131 = (TextView) findViewById(R.id.textView131);
        textView139 = (TextView) findViewById(R.id.textView139);
        textView147 = (TextView) findViewById(R.id.textView147);
        textView155 = (TextView) findViewById(R.id.textView155);
        textView163 = (TextView) findViewById(R.id.textView163);
        textView171 = (TextView) findViewById(R.id.textView171);
        textView179 = (TextView) findViewById(R.id.textView179);
        textView187 = (TextView) findViewById(R.id.textView187);
        textView195 = (TextView) findViewById(R.id.textView195);
        textView203 = (TextView) findViewById(R.id.textView203);
        textView211 = (TextView) findViewById(R.id.textView211);

        volumeSpinnerLeft = (Spinner) findViewById(R.id.volumeSpinnerLeft);
        volumeSpinnerRight = (Spinner) findViewById(R.id.volumeSpinnerRight);
        soundProcessSpinnerLeft = (Spinner) findViewById(R.id.soundProcessSpinnerLeft);
        soundProcessSpinnerRight = (Spinner) findViewById(R.id.soundProcessSpinnerRight);
        stimOrderLeft = (Spinner) findViewById(R.id.stimOrderLeft);
        stimOrderRight = (Spinner) findViewById(R.id.stimOrderRight);
        stimModeLeft = (Spinner) findViewById(R.id.stimModeLeft);
        stimModeRight = (Spinner) findViewById(R.id.stimModeRight);
        windowSpinnerLeft = (Spinner) findViewById(R.id.windowSpinnerLeft);
        windowSpinnerRight = (Spinner) findViewById(R.id.windowSpinnerRight);
        nMaximaSpinnerLeft = (Spinner) findViewById(R.id.nMaximaSpinnerLeft);
        nMaximaSpinnerRight = (Spinner) findViewById(R.id.nMaximaSpinnerRight);

        Button updateButton = (Button) findViewById(R.id.updateButton);

        nMaximaSpinnerLeft.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.v("item", (String) parent.getItemAtPosition(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        nMaximaSpinnerRight.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.v("item", (String) parent.getItemAtPosition(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        assert updateButton != null;
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateButtonText();
            }
        });
    }

    void getMAPFromPreferences() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        leftExists = preferences.getBoolean("leftMapExists", false);
        rightExists = preferences.getBoolean("rightMapExists", false);

        if (leftExists) {
            leftMAPnbands = preferences.getInt("leftMAPnbands", 0);
            leftMAPimplantType = preferences.getString("Left.implantType", "");
            leftMAPsamplingFrequency = preferences.getInt("Left.samplingFrequency", 0);
            leftMAPnumberOfChannels = preferences.getInt("Left.numberOfChannels", 0);
            leftMAPfrequencyTable = preferences.getString("Left.frequencyTable", "");
            leftMAPsoundProcessingStrategy = preferences.getString("Left.soundProcessingStrategy", "");
            leftMAPnMaxima = preferences.getInt("Left.nMaxima", 0);
            leftMAPstimulationMode = preferences.getString("Left.stimulationMode", "");
            leftMAPstimulationRate = preferences.getInt("Left.stimulationRate", 0);
            leftMAPpulseWidth = preferences.getInt("Left.pulseWidth", 0);
            leftMAPsensitivity = getDouble(preferences, "Left.sensitivity", 0);
            leftMAPgain = getDouble(preferences, "Left.gain", 0);
            leftMAPvolume = preferences.getInt("Left.volume", 0);
            leftMAPQfactor = getDouble(preferences, "Left.Qfactor", 0);
            leftMAPbaseLevel = getDouble(preferences, "Left.baseLevel", 0);
            leftMAPsaturationLevel = getDouble(preferences, "Left.saturationLevel", 0);
            leftMAPstimulationOrder = preferences.getString("Left.stimulationOrder", "");
            leftMAPwindow = preferences.getString("Left.window", "");

            leftMAPTHR = new int[leftMAPnbands];
            leftMAPMCL = new int[leftMAPnbands];
            leftMAPgains = new double[leftMAPnbands];
            leftMAPelectrodes = new int[leftMAPnbands];

            for (int i = 0; i < leftMAPnbands; i++) {
                leftMAPTHR[i] = preferences.getInt("leftTHR" + i, 0);
                leftMAPMCL[i] = preferences.getInt("leftMCL" + i, 0);
                leftMAPgains[i] = getDouble(preferences, "leftgain" + i, 0);
                leftMAPelectrodes[i] = preferences.getInt("leftelectrodes" + i, 0);
            }

            setLeftMAPText();
        } else {
            noLeftMAPText();
        }

        if (rightExists) {
            rightMAPnbands = preferences.getInt("rightMAPnbands", 0);
            rightMAPimplantType = preferences.getString("Right.implantType", "");
            rightMAPsamplingFrequency = preferences.getInt("Right.samplingFrequency", 0);
            rightMAPnumberOfChannels = preferences.getInt("Right.numberOfChannels", 0);
            rightMAPfrequencyTable = preferences.getString("Right.frequencyTable", "");
            rightMAPsoundProcessingStrategy = preferences.getString("Right.soundProcessingStrategy", "");
            rightMAPnMaxima = preferences.getInt("Right.nMaxima", 0);
            rightMAPstimulationMode = preferences.getString("Right.stimulationMode", "");
            rightMAPstimulationRate = preferences.getInt("Right.stimulationRate", 0);
            rightMAPpulseWidth = preferences.getInt("Right.pulseWidth", 0);
            rightMAPsensitivity = getDouble(preferences, "Right.sensitivity", 0);
            rightMAPgain = getDouble(preferences, "Right.gain", 0);
            rightMAPvolume = preferences.getInt("Right.volume", 0);
            rightMAPQfactor = getDouble(preferences, "Right.Qfactor", 0);
            rightMAPbaseLevel = getDouble(preferences, "Right.baseLevel", 0);
            rightMAPsaturationLevel = getDouble(preferences, "Right.saturationLevel", 0);
            rightMAPstimulationOrder = preferences.getString("Right.stimulationOrder", "");
            rightMAPwindow = preferences.getString("Right.window", "");

            rightMAPTHR = new int[rightMAPnbands];
            rightMAPMCL = new int[rightMAPnbands];
            rightMAPgains = new double[rightMAPnbands];
            rightMAPelectrodes = new int[rightMAPnbands];

            for (int i = 0; i < rightMAPnbands; i++) {
                rightMAPTHR[i] = preferences.getInt("rightTHR" + i, 0);
                rightMAPMCL[i] = preferences.getInt("rightMCL" + i, 0);
                rightMAPgains[i] = getDouble(preferences, "rightgain" + i, 0);
                rightMAPelectrodes[i] = preferences.getInt("rightelectrodes" + i, 0);
            }

            setRightMAPText();
        } else {
            noRightMAPText();
        }
    }

    void setLeftMAPText() {
        ArrayAdapter<CharSequence> soundProcessAdapterLeft = ArrayAdapter.createFromResource(SettingsActivity.this,
                R.array.strategySpinnerItems, android.R.layout.simple_spinner_item);
        soundProcessAdapterLeft.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ArrayAdapter<CharSequence> stimOrderAdapterLeft = ArrayAdapter.createFromResource(SettingsActivity.this,
                R.array.stimOrderSpinnerItems, android.R.layout.simple_spinner_item);
        stimOrderAdapterLeft.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ArrayAdapter<CharSequence> stimModeAdapterLeft = ArrayAdapter.createFromResource(SettingsActivity.this,
                R.array.stimModeSpinnerItems, android.R.layout.simple_spinner_item);
        stimModeAdapterLeft.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ArrayAdapter<CharSequence> windowAdapterLeft = ArrayAdapter.createFromResource(SettingsActivity.this,
                R.array.windowSpinnerItems, android.R.layout.simple_spinner_item);
        windowAdapterLeft.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        soundProcessSpinnerLeft.setAdapter(soundProcessAdapterLeft);
        stimOrderLeft.setAdapter(stimOrderAdapterLeft);
        stimModeLeft.setAdapter(stimModeAdapterLeft);
        windowSpinnerLeft.setAdapter(windowAdapterLeft);

        ArrayAdapter<CharSequence> volumeAdapterLeft = ArrayAdapter.createFromResource(SettingsActivity.this,
                R.array.volumeSpinnerItems, android.R.layout.simple_spinner_item);
        volumeAdapterLeft.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        volumeSpinnerLeft.setAdapter(volumeAdapterLeft);

        String[] nMaximaItemsLeft = new String[]{"-1"};

        switch (leftMAPnumberOfChannels) {
            case 1:
                nMaximaItemsLeft = new String[]{"1"};
                break;
            case 2:
                nMaximaItemsLeft = new String[]{"1", "2"};
                break;
            case 3:
                nMaximaItemsLeft = new String[]{"1", "2", "3"};
                break;
            case 4:
                nMaximaItemsLeft = new String[]{"1", "2", "3", "4"};
                break;
            case 5:
                nMaximaItemsLeft = new String[]{"1", "2", "3", "4", "5"};
                break;
            case 6:
                nMaximaItemsLeft = new String[]{"1", "2", "3", "4", "5", "6"};
                break;
            case 7:
                nMaximaItemsLeft = new String[]{"1", "2", "3", "4", "5", "6", "7"};
                break;
            case 8:
                nMaximaItemsLeft = new String[]{"1", "2", "3", "4", "5", "6", "7", "8"};
                break;
            case 9:
                nMaximaItemsLeft = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9"};
                break;
            case 10:
                nMaximaItemsLeft = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
                break;
            case 11:
                nMaximaItemsLeft = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
                        "11"};
                break;
            case 12:
                nMaximaItemsLeft = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
                        "11", "12"};
                break;
            case 13:
                nMaximaItemsLeft = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
                        "11", "12", "13"};
                break;
            case 14:
                nMaximaItemsLeft = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
                        "11", "12", "13", "14"};
                break;
            case 15:
                nMaximaItemsLeft = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
                        "11", "12", "13", "14", "15"};
                break;
            case 16:
                nMaximaItemsLeft = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
                        "11", "12", "13", "14", "15", "16"};
                break;
            case 17:
                nMaximaItemsLeft = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
                        "11", "12", "13", "14", "15", "16", "17"};
                break;
            case 18:
                nMaximaItemsLeft = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
                        "11", "12", "13", "14", "15", "16", "17", "18"};
                break;
            case 19:
                nMaximaItemsLeft = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
                        "11", "12", "13", "14", "15", "16", "17", "18", "19"};
                break;
            case 20:
                nMaximaItemsLeft = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
                        "11", "12", "13", "14", "15", "16", "17", "18", "19", "20"};
                break;
            case 21:
                nMaximaItemsLeft = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
                        "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21"};
                break;
            case 22:
                nMaximaItemsLeft = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
                        "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22"};
                break;
        }

        ArrayAdapter<String> nMaximaAdapterLeft = new ArrayAdapter<>(SettingsActivity.this,
                android.R.layout.simple_spinner_dropdown_item, nMaximaItemsLeft);
        nMaximaSpinnerLeft.setAdapter(nMaximaAdapterLeft);

        textView18.setText(leftMAPimplantType);
        textView19.setText(String.valueOf(leftMAPsamplingFrequency));
        textView20.setText(String.valueOf(leftMAPnumberOfChannels));
        textView21.setText(leftMAPfrequencyTable);

        editText4.setText(String.valueOf(leftMAPstimulationRate), TextView.BufferType.EDITABLE);
        editText5.setText(String.valueOf(leftMAPpulseWidth), TextView.BufferType.EDITABLE);
        editText6.setText(String.valueOf(leftMAPsensitivity), TextView.BufferType.EDITABLE);
        editText7.setText(String.valueOf(leftMAPgain), TextView.BufferType.EDITABLE);
        editText9.setText(String.valueOf(leftMAPQfactor), TextView.BufferType.EDITABLE);
        editText10.setText(String.valueOf(leftMAPbaseLevel), TextView.BufferType.EDITABLE);
        editText11.setText(String.valueOf(leftMAPsaturationLevel), TextView.BufferType.EDITABLE);

        nMaximaSpinnerLeft.setSelection(getSpinIndex(nMaximaSpinnerLeft, String.valueOf(leftMAPnMaxima)));
        volumeSpinnerLeft.setSelection(getSpinIndex(volumeSpinnerLeft, String.valueOf(leftMAPvolume)));
        stimOrderLeft.setSelection(getSpinIndex(stimOrderLeft, leftMAPstimulationOrder));
        windowSpinnerLeft.setSelection(getSpinIndex(windowSpinnerLeft, leftMAPwindow));

        if (leftMAPelectrodes.length == 22) { // left electrode array
            textView38.setText(String.valueOf(leftMAPTHR[0]));
            textView46.setText(String.valueOf(leftMAPTHR[1]));
            textView54.setText(String.valueOf(leftMAPTHR[2]));
            textView62.setText(String.valueOf(leftMAPTHR[3]));
            textView70.setText(String.valueOf(leftMAPTHR[4]));
            textView78.setText(String.valueOf(leftMAPTHR[5]));
            textView86.setText(String.valueOf(leftMAPTHR[6]));
            textView94.setText(String.valueOf(leftMAPTHR[7]));
            textView102.setText(String.valueOf(leftMAPTHR[8]));
            textView110.setText(String.valueOf(leftMAPTHR[9]));
            textView118.setText(String.valueOf(leftMAPTHR[10]));
            textView126.setText(String.valueOf(leftMAPTHR[11]));
            textView134.setText(String.valueOf(leftMAPTHR[12]));
            textView142.setText(String.valueOf(leftMAPTHR[13]));
            textView150.setText(String.valueOf(leftMAPTHR[14]));
            textView158.setText(String.valueOf(leftMAPTHR[15]));
            textView166.setText(String.valueOf(leftMAPTHR[16]));
            textView174.setText(String.valueOf(leftMAPTHR[17]));
            textView182.setText(String.valueOf(leftMAPTHR[18]));
            textView190.setText(String.valueOf(leftMAPTHR[19]));
            textView198.setText(String.valueOf(leftMAPTHR[20]));
            textView206.setText(String.valueOf(leftMAPTHR[21]));

            textView39.setText(String.valueOf(leftMAPMCL[0]));
            textView47.setText(String.valueOf(leftMAPMCL[1]));
            textView55.setText(String.valueOf(leftMAPMCL[2]));
            textView63.setText(String.valueOf(leftMAPMCL[3]));
            textView71.setText(String.valueOf(leftMAPMCL[4]));
            textView79.setText(String.valueOf(leftMAPMCL[5]));
            textView87.setText(String.valueOf(leftMAPMCL[6]));
            textView95.setText(String.valueOf(leftMAPMCL[7]));
            textView103.setText(String.valueOf(leftMAPMCL[8]));
            textView111.setText(String.valueOf(leftMAPMCL[9]));
            textView119.setText(String.valueOf(leftMAPMCL[10]));
            textView127.setText(String.valueOf(leftMAPMCL[11]));
            textView135.setText(String.valueOf(leftMAPMCL[12]));
            textView143.setText(String.valueOf(leftMAPMCL[13]));
            textView151.setText(String.valueOf(leftMAPMCL[14]));
            textView159.setText(String.valueOf(leftMAPMCL[15]));
            textView167.setText(String.valueOf(leftMAPMCL[16]));
            textView175.setText(String.valueOf(leftMAPMCL[17]));
            textView183.setText(String.valueOf(leftMAPMCL[18]));
            textView191.setText(String.valueOf(leftMAPMCL[19]));
            textView199.setText(String.valueOf(leftMAPMCL[20]));
            textView207.setText(String.valueOf(leftMAPMCL[21]));

            textView40.setText(String.valueOf(leftMAPgains[0]));
            textView48.setText(String.valueOf(leftMAPgains[1]));
            textView56.setText(String.valueOf(leftMAPgains[2]));
            textView64.setText(String.valueOf(leftMAPgains[3]));
            textView72.setText(String.valueOf(leftMAPgains[4]));
            textView80.setText(String.valueOf(leftMAPgains[5]));
            textView88.setText(String.valueOf(leftMAPgains[6]));
            textView96.setText(String.valueOf(leftMAPgains[7]));
            textView104.setText(String.valueOf(leftMAPgains[8]));
            textView112.setText(String.valueOf(leftMAPgains[9]));
            textView120.setText(String.valueOf(leftMAPgains[10]));
            textView128.setText(String.valueOf(leftMAPgains[11]));
            textView136.setText(String.valueOf(leftMAPgains[12]));
            textView144.setText(String.valueOf(leftMAPgains[13]));
            textView152.setText(String.valueOf(leftMAPgains[14]));
            textView160.setText(String.valueOf(leftMAPgains[15]));
            textView168.setText(String.valueOf(leftMAPgains[16]));
            textView176.setText(String.valueOf(leftMAPgains[17]));
            textView184.setText(String.valueOf(leftMAPgains[18]));
            textView192.setText(String.valueOf(leftMAPgains[19]));
            textView200.setText(String.valueOf(leftMAPgains[20]));
            textView208.setText(String.valueOf(leftMAPgains[21]));
        } else {

            int electrodeNum;
            for (int i = 0; i < leftMAPelectrodes.length; i++) {
                electrodeNum = leftMAPelectrodes[i];
                switch (electrodeNum) {
                    case 1:
                        textView38.setText(String.valueOf(leftMAPTHR[i]));
                        textView39.setText(String.valueOf(leftMAPMCL[i]));
                        textView40.setText(String.valueOf(leftMAPgains[i]));
                        break;
                    case 2:
                        textView46.setText(String.valueOf(leftMAPTHR[i]));
                        textView47.setText(String.valueOf(leftMAPMCL[i]));
                        textView48.setText(String.valueOf(leftMAPgains[i]));
                        break;
                    case 3:
                        textView54.setText(String.valueOf(leftMAPTHR[i]));
                        textView55.setText(String.valueOf(leftMAPMCL[i]));
                        textView56.setText(String.valueOf(leftMAPgains[i]));
                        break;
                    case 4:
                        textView62.setText(String.valueOf(leftMAPTHR[i]));
                        textView63.setText(String.valueOf(leftMAPMCL[i]));
                        textView64.setText(String.valueOf(leftMAPgains[i]));
                        break;
                    case 5:
                        textView70.setText(String.valueOf(leftMAPTHR[i]));
                        textView71.setText(String.valueOf(leftMAPMCL[i]));
                        textView72.setText(String.valueOf(leftMAPgains[i]));
                        break;
                    case 6:
                        textView78.setText(String.valueOf(leftMAPTHR[i]));
                        textView79.setText(String.valueOf(leftMAPMCL[i]));
                        textView80.setText(String.valueOf(leftMAPgains[i]));
                        break;
                    case 7:
                        textView86.setText(String.valueOf(leftMAPTHR[i]));
                        textView87.setText(String.valueOf(leftMAPMCL[i]));
                        textView88.setText(String.valueOf(leftMAPgains[i]));
                        break;
                    case 8:
                        textView94.setText(String.valueOf(leftMAPTHR[i]));
                        textView95.setText(String.valueOf(leftMAPMCL[i]));
                        textView96.setText(String.valueOf(leftMAPgains[i]));
                        break;
                    case 9:
                        textView102.setText(String.valueOf(leftMAPTHR[i]));
                        textView103.setText(String.valueOf(leftMAPMCL[i]));
                        textView104.setText(String.valueOf(leftMAPgains[i]));
                        break;
                    case 10:
                        textView110.setText(String.valueOf(leftMAPTHR[i]));
                        textView111.setText(String.valueOf(leftMAPMCL[i]));
                        textView112.setText(String.valueOf(leftMAPgains[i]));
                    case 11:
                        textView118.setText(String.valueOf(leftMAPTHR[i]));
                        textView119.setText(String.valueOf(leftMAPMCL[i]));
                        textView120.setText(String.valueOf(leftMAPgains[i]));
                        break;
                    case 12:
                        textView126.setText(String.valueOf(leftMAPTHR[i]));
                        textView127.setText(String.valueOf(leftMAPMCL[i]));
                        textView128.setText(String.valueOf(leftMAPgains[i]));
                        break;
                    case 13:
                        textView134.setText(String.valueOf(leftMAPTHR[i]));
                        textView135.setText(String.valueOf(leftMAPMCL[i]));
                        textView136.setText(String.valueOf(leftMAPgains[i]));
                        break;
                    case 14:
                        textView142.setText(String.valueOf(leftMAPTHR[i]));
                        textView143.setText(String.valueOf(leftMAPMCL[i]));
                        textView144.setText(String.valueOf(leftMAPgains[i]));
                        break;
                    case 15:
                        textView150.setText(String.valueOf(leftMAPTHR[i]));
                        textView151.setText(String.valueOf(leftMAPMCL[i]));
                        textView152.setText(String.valueOf(leftMAPgains[i]));
                        break;
                    case 16:
                        textView158.setText(String.valueOf(leftMAPTHR[i]));
                        textView159.setText(String.valueOf(leftMAPMCL[i]));
                        textView160.setText(String.valueOf(leftMAPgains[i]));
                        break;
                    case 17:
                        textView166.setText(String.valueOf(leftMAPTHR[i]));
                        textView167.setText(String.valueOf(leftMAPMCL[i]));
                        textView168.setText(String.valueOf(leftMAPgains[i]));
                        break;
                    case 18:
                        textView174.setText(String.valueOf(leftMAPTHR[i]));
                        textView175.setText(String.valueOf(leftMAPMCL[i]));
                        textView176.setText(String.valueOf(leftMAPgains[i]));
                        break;
                    case 19:
                        textView182.setText(String.valueOf(leftMAPTHR[i]));
                        textView183.setText(String.valueOf(leftMAPMCL[i]));
                        textView184.setText(String.valueOf(leftMAPgains[i]));
                        break;
                    case 20:
                        textView190.setText(String.valueOf(leftMAPTHR[i]));
                        textView191.setText(String.valueOf(leftMAPMCL[i]));
                        textView192.setText(String.valueOf(leftMAPgains[i]));
                        break;
                    case 21:
                        textView198.setText(String.valueOf(leftMAPTHR[i]));
                        textView199.setText(String.valueOf(leftMAPMCL[i]));
                        textView200.setText(String.valueOf(leftMAPgains[i]));
                        break;
                    case 22:
                        textView206.setText(String.valueOf(leftMAPTHR[i]));
                        textView207.setText(String.valueOf(leftMAPMCL[i]));
                        textView208.setText(String.valueOf(leftMAPgains[i]));
                        break;
                }
            }
        }

    }

    void noLeftMAPText() {
        textView18.setEnabled(false);
        textView19.setEnabled(false);
        textView20.setEnabled(false);
        textView21.setEnabled(false);

        editText4.setEnabled(false);
        editText5.setEnabled(false);
        editText6.setEnabled(false);
        editText7.setEnabled(false);
        editText9.setEnabled(false);
        editText10.setEnabled(false);
        editText11.setEnabled(false);

        soundProcessSpinnerLeft.setEnabled(false);
        nMaximaSpinnerLeft.setEnabled(false);
        stimModeLeft.setEnabled(false);
        volumeSpinnerLeft.setEnabled(false);
        stimOrderLeft.setEnabled(false);
        windowSpinnerLeft.setEnabled(false);

        // Disable text (reduce alpha to 38%)
        TextView textView01 = (TextView) findViewById(R.id.textView01);
        if (textView01 != null) {
            textView01.setTextColor(Color.argb(disabledAlpha, 0, 0, 0));
        }

        TextView textView26 = (TextView) findViewById(R.id.textView26);
        if (textView26 != null) {
            textView26.setTextColor(Color.argb(disabledAlpha, 0, 0, 0));
        }

        TextView textView30 = (TextView) findViewById(R.id.textView30);
        if (textView30 != null) {
            textView30.setTextColor(Color.argb(disabledAlpha, 0, 0, 0));
        }

        TextView textView31 = (TextView) findViewById(R.id.textView31);
        if (textView31 != null) {
            textView31.setTextColor(Color.argb(disabledAlpha, 0, 0, 0));
        }

        TextView textView32 = (TextView) findViewById(R.id.textView32);
        if (textView32 != null) {
            textView32.setTextColor(Color.argb(disabledAlpha, 0, 0, 0));
        }
    }

    void setRightMAPText() {
        ArrayAdapter<CharSequence> soundProcessAdapterRight = ArrayAdapter.createFromResource(SettingsActivity.this,
                R.array.strategySpinnerItems, android.R.layout.simple_spinner_item);
        soundProcessAdapterRight.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ArrayAdapter<CharSequence> stimOrderAdapterRight = ArrayAdapter.createFromResource(SettingsActivity.this,
                R.array.stimOrderSpinnerItems, android.R.layout.simple_spinner_item);
        stimOrderAdapterRight.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ArrayAdapter<CharSequence> stimModeAdapterRight = ArrayAdapter.createFromResource(SettingsActivity.this,
                R.array.stimModeSpinnerItems, android.R.layout.simple_spinner_item);
        stimModeAdapterRight.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ArrayAdapter<CharSequence> windowAdapterRight = ArrayAdapter.createFromResource(SettingsActivity.this,
                R.array.windowSpinnerItems, android.R.layout.simple_spinner_item);
        windowAdapterRight.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        soundProcessSpinnerRight.setAdapter(soundProcessAdapterRight);
        stimOrderRight.setAdapter(stimOrderAdapterRight);
        stimModeRight.setAdapter(stimModeAdapterRight);
        windowSpinnerRight.setAdapter(windowAdapterRight);

        ArrayAdapter<CharSequence> volumeAdapterRight = ArrayAdapter.createFromResource(SettingsActivity.this,
                R.array.volumeSpinnerItems, android.R.layout.simple_spinner_item);
        volumeAdapterRight.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        volumeSpinnerRight.setAdapter(volumeAdapterRight);

        String[] nMaximaItemsRight = new String[]{"-1"};

        switch (rightMAPnumberOfChannels) {
            case 1:
                nMaximaItemsRight = new String[]{"1"};
                break;
            case 2:
                nMaximaItemsRight = new String[]{"1", "2"};
                break;
            case 3:
                nMaximaItemsRight = new String[]{"1", "2", "3"};
                break;
            case 4:
                nMaximaItemsRight = new String[]{"1", "2", "3", "4"};
                break;
            case 5:
                nMaximaItemsRight = new String[]{"1", "2", "3", "4", "5"};
                break;
            case 6:
                nMaximaItemsRight = new String[]{"1", "2", "3", "4", "5", "6"};
                break;
            case 7:
                nMaximaItemsRight = new String[]{"1", "2", "3", "4", "5", "6", "7"};
                break;
            case 8:
                nMaximaItemsRight = new String[]{"1", "2", "3", "4", "5", "6", "7", "8"};
                break;
            case 9:
                nMaximaItemsRight = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9"};
                break;
            case 10:
                nMaximaItemsRight = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
                break;
            case 11:
                nMaximaItemsRight = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
                        "11"};
                break;
            case 12:
                nMaximaItemsRight = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
                        "11", "12"};
                break;
            case 13:
                nMaximaItemsRight = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
                        "11", "12", "13"};
                break;
            case 14:
                nMaximaItemsRight = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
                        "11", "12", "13", "14"};
                break;
            case 15:
                nMaximaItemsRight = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
                        "11", "12", "13", "14", "15"};
                break;
            case 16:
                nMaximaItemsRight = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
                        "11", "12", "13", "14", "15", "16"};
                break;
            case 17:
                nMaximaItemsRight = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
                        "11", "12", "13", "14", "15", "16", "17"};
                break;
            case 18:
                nMaximaItemsRight = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
                        "11", "12", "13", "14", "15", "16", "17", "18"};
                break;
            case 19:
                nMaximaItemsRight = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
                        "11", "12", "13", "14", "15", "16", "17", "18", "19"};
                break;
            case 20:
                nMaximaItemsRight = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
                        "11", "12", "13", "14", "15", "16", "17", "18", "19", "20"};
                break;
            case 21:
                nMaximaItemsRight = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
                        "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21"};
                break;
            case 22:
                nMaximaItemsRight = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
                        "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22"};
                break;
        }

        ArrayAdapter<String> nMaximaAdapterRight = new ArrayAdapter<>(SettingsActivity.this,
                android.R.layout.simple_spinner_dropdown_item, nMaximaItemsRight);
        nMaximaSpinnerRight.setAdapter(nMaximaAdapterRight);

        textView22.setText(rightMAPimplantType);
        textView23.setText(String.valueOf(rightMAPsamplingFrequency));
        textView24.setText(String.valueOf(rightMAPnumberOfChannels));
        textView25.setText(rightMAPfrequencyTable);

        editText17.setText(String.valueOf(rightMAPstimulationRate), TextView.BufferType.EDITABLE);
        editText18.setText(String.valueOf(rightMAPpulseWidth), TextView.BufferType.EDITABLE);
        editText19.setText(String.valueOf(rightMAPsensitivity), TextView.BufferType.EDITABLE);
        editText20.setText(String.valueOf(rightMAPgain), TextView.BufferType.EDITABLE);
        editText22.setText(String.valueOf(rightMAPQfactor), TextView.BufferType.EDITABLE);
        editText23.setText(String.valueOf(rightMAPbaseLevel), TextView.BufferType.EDITABLE);
        editText24.setText(String.valueOf(rightMAPsaturationLevel), TextView.BufferType.EDITABLE);

        nMaximaSpinnerRight.setSelection(getSpinIndex(nMaximaSpinnerRight, String.valueOf(rightMAPnMaxima)));
        volumeSpinnerRight.setSelection(getSpinIndex(volumeSpinnerRight, String.valueOf(rightMAPvolume)));
        stimOrderRight.setSelection(getSpinIndex(stimOrderRight, rightMAPstimulationOrder));
        windowSpinnerRight.setSelection(getSpinIndex(windowSpinnerRight, rightMAPwindow));

        if (rightMAPelectrodes.length == 22) { // right electrode array
            textView41.setText(String.valueOf(rightMAPTHR[0]));
            textView49.setText(String.valueOf(rightMAPTHR[1]));
            textView57.setText(String.valueOf(rightMAPTHR[2]));
            textView65.setText(String.valueOf(rightMAPTHR[3]));
            textView73.setText(String.valueOf(rightMAPTHR[4]));
            textView81.setText(String.valueOf(rightMAPTHR[5]));
            textView89.setText(String.valueOf(rightMAPTHR[6]));
            textView97.setText(String.valueOf(rightMAPTHR[7]));
            textView105.setText(String.valueOf(rightMAPTHR[8]));
            textView113.setText(String.valueOf(rightMAPTHR[9]));
            textView121.setText(String.valueOf(rightMAPTHR[10]));
            textView129.setText(String.valueOf(rightMAPTHR[11]));
            textView137.setText(String.valueOf(rightMAPTHR[12]));
            textView145.setText(String.valueOf(rightMAPTHR[13]));
            textView153.setText(String.valueOf(rightMAPTHR[14]));
            textView161.setText(String.valueOf(rightMAPTHR[15]));
            textView169.setText(String.valueOf(rightMAPTHR[16]));
            textView177.setText(String.valueOf(rightMAPTHR[17]));
            textView185.setText(String.valueOf(rightMAPTHR[18]));
            textView193.setText(String.valueOf(rightMAPTHR[19]));
            textView201.setText(String.valueOf(rightMAPTHR[20]));
            textView209.setText(String.valueOf(rightMAPTHR[21]));

            textView42.setText(String.valueOf(rightMAPMCL[0]));
            textView50.setText(String.valueOf(rightMAPMCL[1]));
            textView58.setText(String.valueOf(rightMAPMCL[2]));
            textView66.setText(String.valueOf(rightMAPMCL[3]));
            textView74.setText(String.valueOf(rightMAPMCL[4]));
            textView82.setText(String.valueOf(rightMAPMCL[5]));
            textView90.setText(String.valueOf(rightMAPMCL[6]));
            textView98.setText(String.valueOf(rightMAPMCL[7]));
            textView106.setText(String.valueOf(rightMAPMCL[8]));
            textView114.setText(String.valueOf(rightMAPMCL[9]));
            textView122.setText(String.valueOf(rightMAPMCL[10]));
            textView130.setText(String.valueOf(rightMAPMCL[11]));
            textView138.setText(String.valueOf(rightMAPMCL[12]));
            textView146.setText(String.valueOf(rightMAPMCL[13]));
            textView154.setText(String.valueOf(rightMAPMCL[14]));
            textView162.setText(String.valueOf(rightMAPMCL[15]));
            textView170.setText(String.valueOf(rightMAPMCL[16]));
            textView178.setText(String.valueOf(rightMAPMCL[17]));
            textView186.setText(String.valueOf(rightMAPMCL[18]));
            textView194.setText(String.valueOf(rightMAPMCL[19]));
            textView202.setText(String.valueOf(rightMAPMCL[20]));
            textView210.setText(String.valueOf(rightMAPMCL[21]));

            textView43.setText(String.valueOf(rightMAPgains[0]));
            textView51.setText(String.valueOf(rightMAPgains[1]));
            textView59.setText(String.valueOf(rightMAPgains[2]));
            textView67.setText(String.valueOf(rightMAPgains[3]));
            textView75.setText(String.valueOf(rightMAPgains[4]));
            textView83.setText(String.valueOf(rightMAPgains[5]));
            textView91.setText(String.valueOf(rightMAPgains[6]));
            textView99.setText(String.valueOf(rightMAPgains[7]));
            textView107.setText(String.valueOf(rightMAPgains[8]));
            textView115.setText(String.valueOf(rightMAPgains[9]));
            textView123.setText(String.valueOf(rightMAPgains[10]));
            textView131.setText(String.valueOf(rightMAPgains[11]));
            textView139.setText(String.valueOf(rightMAPgains[12]));
            textView147.setText(String.valueOf(rightMAPgains[13]));
            textView155.setText(String.valueOf(rightMAPgains[14]));
            textView163.setText(String.valueOf(rightMAPgains[15]));
            textView171.setText(String.valueOf(rightMAPgains[16]));
            textView179.setText(String.valueOf(rightMAPgains[17]));
            textView187.setText(String.valueOf(rightMAPgains[18]));
            textView195.setText(String.valueOf(rightMAPgains[19]));
            textView203.setText(String.valueOf(rightMAPgains[20]));
            textView211.setText(String.valueOf(rightMAPgains[21]));
        } else {

            int electrodeNum;
            for (int i = 0; i < rightMAPelectrodes.length; i++) {
                electrodeNum = rightMAPelectrodes[i];
                switch (electrodeNum) {
                    case 1:
                        textView41.setText(String.valueOf(rightMAPTHR[i]));
                        textView42.setText(String.valueOf(rightMAPMCL[i]));
                        textView43.setText(String.valueOf(rightMAPgains[i]));
                        break;
                    case 2:
                        textView49.setText(String.valueOf(rightMAPTHR[i]));
                        textView50.setText(String.valueOf(rightMAPMCL[i]));
                        textView51.setText(String.valueOf(rightMAPgains[i]));
                        break;
                    case 3:
                        textView57.setText(String.valueOf(rightMAPTHR[i]));
                        textView58.setText(String.valueOf(rightMAPMCL[i]));
                        textView59.setText(String.valueOf(rightMAPgains[i]));
                        break;
                    case 4:
                        textView65.setText(String.valueOf(rightMAPTHR[i]));
                        textView66.setText(String.valueOf(rightMAPMCL[i]));
                        textView67.setText(String.valueOf(rightMAPgains[i]));
                        break;
                    case 5:
                        textView73.setText(String.valueOf(rightMAPTHR[i]));
                        textView74.setText(String.valueOf(rightMAPMCL[i]));
                        textView75.setText(String.valueOf(rightMAPgains[i]));
                        break;
                    case 6:
                        textView81.setText(String.valueOf(rightMAPTHR[i]));
                        textView82.setText(String.valueOf(rightMAPMCL[i]));
                        textView83.setText(String.valueOf(rightMAPgains[i]));
                        break;
                    case 7:
                        textView89.setText(String.valueOf(rightMAPTHR[i]));
                        textView90.setText(String.valueOf(rightMAPMCL[i]));
                        textView91.setText(String.valueOf(rightMAPgains[i]));
                        break;
                    case 8:
                        textView97.setText(String.valueOf(rightMAPTHR[i]));
                        textView98.setText(String.valueOf(rightMAPMCL[i]));
                        textView99.setText(String.valueOf(rightMAPgains[i]));
                        break;
                    case 9:
                        textView105.setText(String.valueOf(rightMAPTHR[i]));
                        textView106.setText(String.valueOf(rightMAPMCL[i]));
                        textView107.setText(String.valueOf(rightMAPgains[i]));
                        break;
                    case 10:
                        textView113.setText(String.valueOf(rightMAPTHR[i]));
                        textView114.setText(String.valueOf(rightMAPMCL[i]));
                        textView115.setText(String.valueOf(rightMAPgains[i]));
                    case 11:
                        textView121.setText(String.valueOf(rightMAPTHR[i]));
                        textView122.setText(String.valueOf(rightMAPMCL[i]));
                        textView123.setText(String.valueOf(rightMAPgains[i]));
                        break;
                    case 12:
                        textView129.setText(String.valueOf(rightMAPTHR[i]));
                        textView130.setText(String.valueOf(rightMAPMCL[i]));
                        textView131.setText(String.valueOf(rightMAPgains[i]));
                        break;
                    case 13:
                        textView137.setText(String.valueOf(rightMAPTHR[i]));
                        textView138.setText(String.valueOf(rightMAPMCL[i]));
                        textView139.setText(String.valueOf(rightMAPgains[i]));
                        break;
                    case 14:
                        textView145.setText(String.valueOf(rightMAPTHR[i]));
                        textView146.setText(String.valueOf(rightMAPMCL[i]));
                        textView147.setText(String.valueOf(rightMAPgains[i]));
                        break;
                    case 15:
                        textView153.setText(String.valueOf(rightMAPTHR[i]));
                        textView154.setText(String.valueOf(rightMAPMCL[i]));
                        textView155.setText(String.valueOf(rightMAPgains[i]));
                        break;
                    case 16:
                        textView161.setText(String.valueOf(rightMAPTHR[i]));
                        textView162.setText(String.valueOf(rightMAPMCL[i]));
                        textView163.setText(String.valueOf(rightMAPgains[i]));
                        break;
                    case 17:
                        textView169.setText(String.valueOf(rightMAPTHR[i]));
                        textView170.setText(String.valueOf(rightMAPMCL[i]));
                        textView171.setText(String.valueOf(rightMAPgains[i]));
                        break;
                    case 18:
                        textView177.setText(String.valueOf(rightMAPTHR[i]));
                        textView178.setText(String.valueOf(rightMAPMCL[i]));
                        textView179.setText(String.valueOf(rightMAPgains[i]));
                        break;
                    case 19:
                        textView185.setText(String.valueOf(rightMAPTHR[i]));
                        textView186.setText(String.valueOf(rightMAPMCL[i]));
                        textView187.setText(String.valueOf(rightMAPgains[i]));
                        break;
                    case 20:
                        textView193.setText(String.valueOf(rightMAPTHR[i]));
                        textView194.setText(String.valueOf(rightMAPMCL[i]));
                        textView195.setText(String.valueOf(rightMAPgains[i]));
                        break;
                    case 21:
                        textView201.setText(String.valueOf(rightMAPTHR[i]));
                        textView202.setText(String.valueOf(rightMAPMCL[i]));
                        textView203.setText(String.valueOf(rightMAPgains[i]));
                        break;
                    case 22:
                        textView209.setText(String.valueOf(rightMAPTHR[i]));
                        textView210.setText(String.valueOf(rightMAPMCL[i]));
                        textView211.setText(String.valueOf(rightMAPgains[i]));
                        break;
                }
            }

        }
    }

    void noRightMAPText() {
        textView22.setEnabled(false);
        textView23.setEnabled(false);
        textView24.setEnabled(false);
        textView25.setEnabled(false);

        editText17.setEnabled(false);
        editText18.setEnabled(false);
        editText19.setEnabled(false);
        editText20.setEnabled(false);
        editText22.setEnabled(false);
        editText23.setEnabled(false);
        editText24.setEnabled(false);

        soundProcessSpinnerRight.setEnabled(false);
        nMaximaSpinnerRight.setEnabled(false);
        stimModeRight.setEnabled(false);
        volumeSpinnerRight.setEnabled(false);
        stimOrderRight.setEnabled(false);
        windowSpinnerRight.setEnabled(false);

        // Disable text (reduce alpha to 38%)
        TextView textView02 = (TextView) findViewById(R.id.textView02);
        if (textView02 != null) {
            textView02.setTextColor(Color.argb(disabledAlpha, 0, 0, 0));
        }

        TextView textView27 = (TextView) findViewById(R.id.textView27);
        if (textView27 != null) {
            textView27.setTextColor(Color.argb(disabledAlpha, 0, 0, 0));
        }

        TextView textView33 = (TextView) findViewById(R.id.textView33);
        if (textView33 != null) {
            textView33.setTextColor(Color.argb(disabledAlpha, 0, 0, 0));
        }

        TextView textView34 = (TextView) findViewById(R.id.textView34);
        if (textView34 != null) {
            textView34.setTextColor(Color.argb(disabledAlpha, 0, 0, 0));
        }

        TextView textView35 = (TextView) findViewById(R.id.textView35);
        if (textView35 != null) {
            textView35.setTextColor(Color.argb(disabledAlpha, 0, 0, 0));
        }
    }

    private int getSpinIndex(Spinner spinner, String str) {
        int index = 0;

        for (int i = 0; i < spinner.getCount(); i++) {
            String compare = spinner.getItemAtPosition(i).toString();
            String compareZeros = compare + ".0"; // Account for decimal (e.g. 10 or 10.0)
            if (compare.equalsIgnoreCase(str) || compareZeros.equalsIgnoreCase(str)) {
                index = i;
                break;
            }
        }
        return index;
    }

    SharedPreferences.Editor putDouble(final SharedPreferences.Editor edit, final String key, final double value) {
        return edit.putLong(key, Double.doubleToRawLongBits(value));
    }

    double getDouble(final SharedPreferences prefs, final String key, final double defaultValue) {
        return Double.longBitsToDouble(prefs.getLong(key, Double.doubleToLongBits(defaultValue)));
    }

    boolean isIntInRange(int number, int upperbound, int lowerbound) {
        if ((number >= lowerbound) && (number <= upperbound)) {
            return true;
        } else {
            Toast.makeText(getApplicationContext(), "Value is out bounds.", Toast.LENGTH_LONG).show(); // Set your toast message
            return false;
        }
    }

    boolean isDoubleInRange(double number, int upperbound, int lowerbound) {
        if ((number >= lowerbound) && (number <= upperbound)) {
            return true;
        } else {
            Toast.makeText(getApplicationContext(), "Value is out bounds.", Toast.LENGTH_LONG).show(); // Set your toast message
            return false;
        }
    }

    boolean isInteger(String s) {
        boolean result = isInteger(s, 10);
        if (!result) {
            Toast.makeText(getApplicationContext(), "Please enter an integer.", Toast.LENGTH_LONG).show(); // Set your toast message
        }
        return result;
    }

    boolean isInteger(String s, int radix) {
        if (s.isEmpty()) return false;
        for (int i = 0; i < s.length(); i++) {
            if (i == 0 && s.charAt(i) == '-') {
                if (s.length() == 1) return false;
                else continue;
            }
            if (Character.digit(s.charAt(i), radix) < 0) return false;
        }
        return true;
    }

    void updateButtonText() {
        boolean userProblems = false;
        if (leftExists) {
            // Reassign parameter values
            leftMAPsoundProcessingStrategy = soundProcessSpinnerLeft.getSelectedItem().toString();
            leftMAPnMaxima = Integer.parseInt(nMaximaSpinnerLeft.getSelectedItem().toString());
            leftMAPstimulationMode = stimModeLeft.getSelectedItem().toString();
            leftMAPvolume = Integer.parseInt(volumeSpinnerLeft.getSelectedItem().toString());
            leftMAPstimulationOrder = stimOrderLeft.getSelectedItem().toString();
            leftMAPwindow = windowSpinnerLeft.getSelectedItem().toString();

            // Check if user input is valid integer
            if (isInteger(editText4.getText().toString()) && isIntInRange(Integer.valueOf(editText4.getText().toString()), max_stimulation_rate, min_stimulation_rate)) {
                leftMAPstimulationRate = Integer.valueOf(editText4.getText().toString());
            } else {
                userProblems = true;
            }
            if (isInteger(editText5.getText().toString()) && isIntInRange(Integer.valueOf(editText5.getText().toString()), max_pulsewidth, min_pulsewidth)) {
                leftMAPpulseWidth = Integer.valueOf(editText5.getText().toString());
            } else {
                userProblems = true;
            }

            if (isDoubleInRange(Double.valueOf(editText6.getText().toString()), max_sensitivity, min_sensitivity)) {
                leftMAPsensitivity = Double.valueOf(editText6.getText().toString());
            } else {
                userProblems = true;
            }

            if (isDoubleInRange(Double.valueOf(editText7.getText().toString()), max_gain, min_gain)) {
                leftMAPgain = Double.valueOf(editText7.getText().toString());
            } else {
                userProblems = true;
            }

            if (isDoubleInRange(Double.valueOf(editText9.getText().toString()), max_Qfactor, min_Qfactor)) {
                leftMAPQfactor = Double.valueOf(editText9.getText().toString());
            } else {
                userProblems = true;
            }

            if (isDoubleInRange(Double.valueOf(editText10.getText().toString()), max_baselevel, min_baselevel)) {
                leftMAPbaseLevel = Double.valueOf(editText10.getText().toString());
            } else {
                userProblems = true;
            }

            if (isDoubleInRange(Double.valueOf(editText11.getText().toString()), max_saturationlevel, min_saturationlevel)) {
                leftMAPsaturationLevel = Double.valueOf(editText11.getText().toString());
            } else {
                userProblems = true;
            }

            // Check for valid parameters
            leftMAPcheckStimulationParameters();

            // Save values
            updateLeftMAPPreferences();

            // Display new parameter values
            editText4.setText(String.valueOf(leftMAPstimulationRate), TextView.BufferType.EDITABLE);
            editText5.setText(String.valueOf(leftMAPpulseWidth), TextView.BufferType.EDITABLE);
            editText6.setText(String.valueOf(leftMAPsensitivity), TextView.BufferType.EDITABLE);
            editText7.setText(String.valueOf(leftMAPgain), TextView.BufferType.EDITABLE);
            editText9.setText(String.valueOf(leftMAPQfactor), TextView.BufferType.EDITABLE);
            editText10.setText(String.valueOf(leftMAPbaseLevel), TextView.BufferType.EDITABLE);
            editText11.setText(String.valueOf(leftMAPsaturationLevel), TextView.BufferType.EDITABLE);

            nMaximaSpinnerLeft.setSelection(getSpinIndex(nMaximaSpinnerLeft, String.valueOf(leftMAPnMaxima)));
            volumeSpinnerLeft.setSelection(getSpinIndex(volumeSpinnerLeft, String.valueOf(leftMAPvolume)));
            stimOrderLeft.setSelection(getSpinIndex(stimOrderLeft, leftMAPstimulationOrder));
            windowSpinnerLeft.setSelection(getSpinIndex(windowSpinnerLeft, leftMAPwindow));
        }

        if (rightExists) {
            // Reassign parameter values
            rightMAPsoundProcessingStrategy = soundProcessSpinnerRight.getSelectedItem().toString();
            rightMAPnMaxima = Integer.parseInt(nMaximaSpinnerRight.getSelectedItem().toString());
            rightMAPstimulationMode = stimModeRight.getSelectedItem().toString();
            rightMAPvolume = Integer.parseInt(volumeSpinnerRight.getSelectedItem().toString());
            rightMAPstimulationOrder = stimOrderRight.getSelectedItem().toString();
            rightMAPwindow = windowSpinnerRight.getSelectedItem().toString();

            // Check if user input is valid integer
            if (isInteger(editText17.getText().toString()) && isIntInRange(Integer.valueOf(editText17.getText().toString()), max_stimulation_rate, min_stimulation_rate)) {
                rightMAPstimulationRate = Integer.valueOf(editText17.getText().toString());
            } else {
                userProblems = true;
            }
            if (isInteger(editText18.getText().toString()) && isIntInRange(Integer.valueOf(editText18.getText().toString()), max_pulsewidth, min_pulsewidth)) {
                rightMAPpulseWidth = Integer.valueOf(editText18.getText().toString());
            } else {
                userProblems = true;
            }

            if (isDoubleInRange(Double.valueOf(editText19.getText().toString()), max_sensitivity, min_sensitivity)) {
                rightMAPsensitivity = Double.valueOf(editText19.getText().toString());
            } else {
                userProblems = true;
            }

            if (isDoubleInRange(Double.valueOf(editText20.getText().toString()), max_gain, min_gain)) {
                rightMAPgain = Double.valueOf(editText20.getText().toString());
            } else {
                userProblems = true;
            }

            if (isDoubleInRange(Double.valueOf(editText22.getText().toString()), max_Qfactor, min_Qfactor)) {
                rightMAPQfactor = Double.valueOf(editText22.getText().toString());
            } else {
                userProblems = true;
            }

            if (isDoubleInRange(Double.valueOf(editText23.getText().toString()), max_baselevel, min_baselevel)) {
                rightMAPbaseLevel = Double.valueOf(editText23.getText().toString());
            } else {
                userProblems = true;
            }

            if (isDoubleInRange(Double.valueOf(editText24.getText().toString()), max_saturationlevel, min_saturationlevel)) {
                rightMAPsaturationLevel = Double.valueOf(editText24.getText().toString());
            } else {
                userProblems = true;
            }

            // Check for valid parameters
            rightMAPcheckStimulationParameters();

            // Save values
            updateRightMAPPreferences();

            // Display new parameter values
            editText17.setText(String.valueOf(rightMAPstimulationRate), TextView.BufferType.EDITABLE);
            editText18.setText(String.valueOf(rightMAPpulseWidth), TextView.BufferType.EDITABLE);
            editText19.setText(String.valueOf(rightMAPsensitivity), TextView.BufferType.EDITABLE);
            editText20.setText(String.valueOf(rightMAPgain), TextView.BufferType.EDITABLE);
            editText22.setText(String.valueOf(rightMAPQfactor), TextView.BufferType.EDITABLE);
            editText23.setText(String.valueOf(rightMAPbaseLevel), TextView.BufferType.EDITABLE);
            editText24.setText(String.valueOf(rightMAPsaturationLevel), TextView.BufferType.EDITABLE);

            nMaximaSpinnerRight.setSelection(getSpinIndex(nMaximaSpinnerRight, String.valueOf(rightMAPnMaxima)));
            volumeSpinnerRight.setSelection(getSpinIndex(volumeSpinnerRight, String.valueOf(rightMAPvolume)));
            stimOrderRight.setSelection(getSpinIndex(stimOrderRight, rightMAPstimulationOrder));
            windowSpinnerRight.setSelection(getSpinIndex(windowSpinnerRight, rightMAPwindow));
        }

        // Send info back to first activity
        Intent intent = new Intent();
        if (leftExists) {
            intent.putExtra("leftSensitivity", leftMAPsensitivity);
            intent.putExtra("leftGain", leftMAPgain);

            intent.putExtra("leftMAPimplantGeneration", leftImplantGeneration);
            intent.putExtra("leftMAPstimulationModeCode", leftStimulationModeCode);
            intent.putExtra("leftMAPpulsesPerFramePerChannel", leftPulsesPerFramePerChannel);
            intent.putExtra("leftMAPpulsesPerFrame", leftPulsesPerFrame);
            intent.putExtra("leftMAPinterpulseDuration", leftInterpulseDuration);
            intent.putExtra("leftMAPnRFcycles", leftnRFcycles);
        }

        if (rightExists) {
            intent.putExtra("rightSensitivity", rightMAPsensitivity);
            intent.putExtra("rightGain", rightMAPgain);

            intent.putExtra("rightMAPimplantGeneration", rightImplantGeneration);
            intent.putExtra("rightMAPstimulationModeCode", rightStimulationModeCode);
            intent.putExtra("rightMAPpulsesPerFramePerChannel", rightPulsesPerFramePerChannel);
            intent.putExtra("rightMAPpulsesPerFrame", rightPulsesPerFrame);
            intent.putExtra("rightMAPinterpulseDuration", rightInterpulseDuration);
            intent.putExtra("rightMAPnRFcycles", rightnRFcycles);
        }

        setResult(RESULT_OK, intent);
        if (!userProblems) {
            Toast.makeText(getApplicationContext(), "MAP updated successfully.", Toast.LENGTH_SHORT).show(); // Set your toast message
            finish(); // close activity after hitting Update button if there are no errors
        }
    }

    void updateLeftMAPPreferences() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putInt("Left.stimulationRate", leftMAPstimulationRate);
        editor.putInt("Left.pulseWidth", leftMAPpulseWidth);
        putDouble(editor, "Left.sensitivity", leftMAPsensitivity);
        putDouble(editor, "Left.gain", leftMAPgain);
        putDouble(editor, "Left.Qfactor", leftMAPQfactor);
        putDouble(editor, "Left.baseLevel", leftMAPbaseLevel);
        putDouble(editor, "Left.saturationLevel", leftMAPsaturationLevel);
        editor.putInt("Left.nMaxima", leftMAPnMaxima);
        editor.putInt("Left.volume", leftMAPvolume);
        editor.putString("Left.stimulationOrder", leftMAPstimulationOrder);
        editor.putString("Left.window", leftMAPwindow);

        editor.apply();
    }

    void updateRightMAPPreferences() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putInt("Right.stimulationRate", rightMAPstimulationRate);
        editor.putInt("Right.pulseWidth", rightMAPpulseWidth);
        putDouble(editor, "Right.sensitivity", rightMAPsensitivity);
        putDouble(editor, "Right.gain", rightMAPgain);
        putDouble(editor, "Right.Qfactor", rightMAPQfactor);
        putDouble(editor, "Right.baseLevel", rightMAPbaseLevel);
        putDouble(editor, "Right.saturationLevel", rightMAPsaturationLevel);
        editor.putInt("Right.nMaxima", rightMAPnMaxima);
        editor.putInt("Right.volume", rightMAPvolume);
        editor.putString("Right.stimulationOrder", rightMAPstimulationOrder);
        editor.putString("Right.window", rightMAPwindow);

        editor.apply();
    }

    void leftMAPcheckStimulationParameters() {
        // check if valid user entries
        // have to rerun ACE?

        checkImplantTypeLeft();
        generateStimulationModeCodeLeft();
        checkPulseWidthLeft();
        checkStimulationRateLeft();
        checkTimingParametersLeft();
        computePulseTimingLeft();
        checkLevelsLeft();
        checkVolumeLeft();
    }

    void rightMAPcheckStimulationParameters() {
        // check if valid user entries
        // have to rerun ACE?

        checkImplantTypeRight();
        generateStimulationModeCodeRight();
        checkPulseWidthRight();
        checkStimulationRateRight();
        checkTimingParametersRight();
        computePulseTimingRight();
        checkLevelsRight();
        checkVolumeRight();
    }

    void checkImplantTypeLeft() {
        switch (leftMAPimplantType) {
            case "CI24RE":
                leftImplantGeneration = "CIC4";
                break;
            case "CI24R":
                leftImplantGeneration = "CIC4";
                break;
            case "CI24M":
                leftImplantGeneration = "CIC3";
                break;
            default:
                leftImplantGeneration = "CIC4";
                break;
        }
    }

    void checkImplantTypeRight() {
        switch (rightMAPimplantType) {
            case "CI24RE":
                rightImplantGeneration = "CIC4";
                break;
            case "CI24R":
                rightImplantGeneration = "CIC4";
                break;
            case "CI24M":
                rightImplantGeneration = "CIC3";
                break;
            default:
                rightImplantGeneration = "CIC4";
                break;
        }
    }

    void generateStimulationModeCodeLeft() {
        if (leftImplantGeneration.equals("CIC4")) {
            switch (leftMAPstimulationMode) {
                case "MP1+2":
                    leftStimulationModeCode = 28;
                    break;
                //add other codes for other stimulation modes here
                default:
                    leftStimulationModeCode = 28;
                    break;
            }
        }
        if (leftImplantGeneration.equals("CIC3")) {
            switch (leftMAPstimulationMode) {
                case "MP1+2":
                    leftStimulationModeCode = 30;
                    break;
                //add other codes for other stimulation modes here
                default:
                    leftStimulationModeCode = 30;
                    break;
            }
        }
    }

    void generateStimulationModeCodeRight() {
        if (rightImplantGeneration.equals("CIC4")) {
            switch (rightMAPstimulationMode) {
                case "MP1+2":
                    rightStimulationModeCode = 28;
                    break;
                //add other codes for other stimulation modes here
                default:
                    rightStimulationModeCode = 28;
                    break;
            }
        }
        if (rightImplantGeneration.equals("CIC3")) {
            switch (rightMAPstimulationMode) {
                case "MP1+2":
                    rightStimulationModeCode = 30;
                    break;
                //add other codes for other stimulation modes here
                default:
                    rightStimulationModeCode = 30;
                    break;
            }
        }
    }

    void checkPulseWidthLeft() {
        if (leftMAPpulseWidth > 400) {
            leftMAPpulseWidth = 400;
        } // Limit Pulse Width to 400us
    }

    void checkPulseWidthRight() {
        if (rightMAPpulseWidth > 400) {
            rightMAPpulseWidth = 400;
        } // Limit Pulse Width to 400us
    }

    void checkStimulationRateLeft() {
        int totalStimulationRate;
        double maxPulseWidth;

        totalStimulationRate = (leftMAPstimulationRate * leftMAPnMaxima);
        if (leftMAPstimulationRate <= 14400) //maximum stimulation rate supported by Freedom implants is 14400 Hz
        {
            if (totalStimulationRate <= 14400) {
                maxPulseWidth = Math.floor(0.5 * ((1000000 / totalStimulationRate) - (leftInterPhaseGap + 11))); //for MP stimulation modes in CIC3/CIC4
                if (leftMAPpulseWidth > maxPulseWidth) {
                    leftMAPpulseWidth = (int) maxPulseWidth; //this means it is the STD protocol, PW is reduced to maxPW
                }

            }
        }

        if (totalStimulationRate > 14400) {
            //High Rate Protocol is currently not supported
            while (totalStimulationRate > 14400) {
                leftMAPstimulationRate--;
                totalStimulationRate = (leftMAPstimulationRate * leftMAPnMaxima);
            }
            //print error, exit
        }

    }

    void checkStimulationRateRight() {
        int totalStimulationRate;
        double maxPulseWidth;

        totalStimulationRate = (rightMAPstimulationRate * rightMAPnMaxima);
        if (rightMAPstimulationRate <= 14400) //maximum stimulation rate supported by Freedom implants is 14400 Hz
        {
            if (totalStimulationRate <= 14400) {
                maxPulseWidth = Math.floor(0.5 * ((1000000 / totalStimulationRate) - (rightInterPhaseGap + 11))); //for MP stimulation modes in CIC3/CIC4
                if (rightMAPpulseWidth > maxPulseWidth) {
                    rightMAPpulseWidth = (int) maxPulseWidth; //this means it is the STD protocol, PW is reduced to maxPW
                }

            }
        }

        if (totalStimulationRate > 14400) {
            //High Rate Protocol is currently not supported
            while (totalStimulationRate > 14400) {
                rightMAPstimulationRate--;
                totalStimulationRate = (rightMAPstimulationRate * rightMAPnMaxima);
            }
            //print error, exit
        }

    }

    void checkTimingParametersLeft() {
        leftPulsesPerFramePerChannel = (int) Math.round((8 * (double) leftMAPstimulationRate) / 1000); //8ms //%floor(floor((8.0*rate_set)/1000));
        leftPulsesPerFrame = leftMAPnMaxima * leftPulsesPerFramePerChannel;
        double leftMAPtotalStimulationRate = ((double)leftMAPstimulationRate * (double)leftMAPnMaxima);

        //Pulse-Width Centric
        // for pulse-width centric, max possible pulse-width is:
        //double maxPulseWidth = Math.floor(0.5 * ((8000 / (double) leftMAPnMaxima) - (leftInterPhaseGap + leftDurationSYNC + leftAdditionalGap)));
        double maxPulseWidth = Math.floor(0.5 * ((1000000 / leftMAPtotalStimulationRate) - (leftInterPhaseGap + 11))); //for MP stimulation modes in CIC3/CIC4
        //double maxPulseWidth = Math.floor(0.5 * ((1000000 / leftMAPtotalStimulationRate) - (leftInterPhaseGap + 11+200))); //for BP and CG stimulation modes in CIC3/CIC4

        if (leftMAPpulseWidth > maxPulseWidth) {
            leftMAPpulseWidth = (int) maxPulseWidth;
        } // Limit Pulse Width to 400us

        double pd1 = 8000 / leftPulsesPerFrame;
        double pd2 = (leftMAPpulseWidth * 2 + leftInterPhaseGap + leftDurationSYNC + leftAdditionalGap);
        if (pd1 < pd2) {
            while (pd1 < pd2) {
                leftMAPstimulationRate--;
                leftPulsesPerFramePerChannel = (int) Math.round((8 * (double) leftMAPstimulationRate) / 1000); //8ms //%floor(floor((8.0*rate_set)/1000));
                leftPulsesPerFrame = leftMAPnMaxima * leftPulsesPerFramePerChannel;
                pd1 = 8000 / leftPulsesPerFrame;
            }
        }
    }

    void checkTimingParametersRight() {
        rightPulsesPerFramePerChannel = (int) Math.round((8 * (double) rightMAPstimulationRate) / 1000); //8ms //%floor(floor((8.0*rate_set)/1000));
        rightPulsesPerFrame = rightMAPnMaxima * rightPulsesPerFramePerChannel;
        double rightMAPtotalStimulationRate = ((double)rightMAPstimulationRate * (double)rightMAPnMaxima);

        //Pulse-Width Centric
        // for pulse-width centric, max possible pulse-width is:
        //double maxPulseWidth = Math.floor(0.5 * ((8000 / (double) rightMAPnMaxima) - (rightInterPhaseGap + rightDurationSYNC + rightAdditionalGap)));
        double maxPulseWidth = Math.floor(0.5 * ((1000000 / rightMAPtotalStimulationRate) - (rightInterPhaseGap + 11))); //for MP stimulation modes in CIC3/CIC4
        //double maxPulseWidth = Math.floor(0.5 * ((1000000 / rightMAPtotalStimulationRate) - (rightInterPhaseGap + 11+200))); //for BP and CG stimulation modes in CIC3/CIC4

        if (rightMAPpulseWidth > maxPulseWidth) {
            rightMAPpulseWidth = (int) maxPulseWidth;
        } // Limit Pulse Width to 400us

        double pd1 = 8000 / rightPulsesPerFrame;
        double pd2 = (rightMAPpulseWidth * 2 + rightInterPhaseGap + rightDurationSYNC + rightAdditionalGap);
        if (pd1 < pd2) {
            while (pd1 < pd2) {
                rightMAPstimulationRate--;
                rightPulsesPerFramePerChannel = (int) Math.round((8 * (double) rightMAPstimulationRate) / 1000); //8ms //%floor(floor((8.0*rate_set)/1000));
                rightPulsesPerFrame = rightMAPnMaxima * rightPulsesPerFramePerChannel;
                pd1 = 8000 / rightPulsesPerFrame;
            }
        }
    }

    void computePulseTimingLeft() {
        leftPulsesPerFramePerChannel = (int) Math.round((8 * (double) leftMAPstimulationRate) / 1000); //8ms //%floor(floor((8.0*rate_set)/1000));
        leftPulsesPerFrame = leftMAPnMaxima * leftPulsesPerFramePerChannel;
        leftMAPstimulationRate = 125 * leftPulsesPerFramePerChannel; //125 frames of 8ms in 1s
        //blockShiftL = (BLOCK_SIZE / leftPulsesPerFramePerChannel); //ceil(fs / p.Left.analysis_rate);
        int leftFrameDuration = 8;
        leftInterpulseDuration = (double) leftFrameDuration * 1000 / ((double) leftPulsesPerFrame) - 2 * (double) leftMAPpulseWidth - (double) leftInterPhaseGap - (double) leftDurationSYNC - (double) leftAdditionalGap;
        leftnRFcycles = (int) Math.round((leftInterpulseDuration / 0.1));
    }

    void computePulseTimingRight() {
        rightPulsesPerFramePerChannel = (int) Math.round((8 * (double) rightMAPstimulationRate) / 1000); //8ms //%floor(floor((8.0*rate_set)/1000));
        rightPulsesPerFrame = rightMAPnMaxima * rightPulsesPerFramePerChannel;
        rightMAPstimulationRate = 125 * rightPulsesPerFramePerChannel; //125 frames of 8ms in 1s
        //blockShiftL = (BLOCK_SIZE / rightPulsesPerFramePerChannel); //ceil(fs / p.Right.analysis_rate);
        int rightFrameDuration = 8;
        rightInterpulseDuration = (double) rightFrameDuration * 1000 / ((double) rightPulsesPerFrame) - 2 * (double) rightMAPpulseWidth - (double) rightInterPhaseGap - (double) rightDurationSYNC - (double) rightAdditionalGap;
        rightnRFcycles = (int) Math.round((rightInterpulseDuration / 0.1));
    }

    void checkLevelsLeft() {
        // do something
    }

    void checkLevelsRight() {
        // do something
    }

    void checkVolumeLeft() {
        if (leftMAPvolume > 10) {
            leftMAPvolume = 10;
        }
    }

    void checkVolumeRight() {
        if (rightMAPvolume > 10) {
            rightMAPvolume = 10;
        }
    }
}
