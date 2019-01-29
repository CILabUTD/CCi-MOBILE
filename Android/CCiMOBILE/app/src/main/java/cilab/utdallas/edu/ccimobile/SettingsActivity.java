package cilab.utdallas.edu.ccimobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class SettingsActivity extends AppCompatActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;


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

     //Define TextViews
    private TextView textViewITL, textViewSFL, textViewNCL, textViewFTL, textViewITR, textViewSFR,
            textViewNCR, textViewFTR, textView38, textView39, textView40, textView41, textView42,
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
    private EditText editTextSRL, editTextPWL, editTextSL, editTextGL, editTextQFL, editTextBLL, editTextSLL,
            editTextSRR, editTextPWR, editTextSR, editTextGR, editTextQFR, editTextBLR, editTextSLR;

    // Define Spinners
    private Spinner spinnerVL, spinnerVR, spinnerSPSL,
            spinnerSPSR, spinnerSOL, spinnerSOR, spinnerSML, spinnerSMR,
            spinnerWL, spinnerWR, spinnerNML, spinnerNMR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_tabbed);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateButtonText();
                Snackbar.make(view, "MAP updated.", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
            }
        });

        setUpWidgets();
        //getMAPFromPreferences();
    }

    public static class ParametersFragment extends Fragment {
        public ParametersFragment() {

        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View settingsView = inflater.inflate(R.layout.settings, container, false);
            TextView textViewITL = settingsView.findViewById(R.id.textViewITL);
            TextView textViewSFL = settingsView.findViewById(R.id.textViewSFL);
            TextView textViewNCL = settingsView.findViewById(R.id.textViewNCL);
            TextView textViewFTL = settingsView.findViewById(R.id.textViewFTL);
            TextView textViewITR = settingsView.findViewById(R.id.textViewITR);
            TextView textViewSFR = settingsView.findViewById(R.id.textViewSFR);
            TextView textViewNCR = settingsView.findViewById(R.id.textViewNCR);
            TextView textViewFTR = settingsView.findViewById(R.id.textViewFTR);

            EditText editTextSRL = settingsView.findViewById(R.id.editTextSRL);
            EditText editTextPWL = settingsView.findViewById(R.id.editTextPWL);
            EditText editTextSL = settingsView.findViewById(R.id.editTextSL);
            EditText editTextGL = settingsView.findViewById(R.id.editTextGL);
            EditText editTextQFL = settingsView.findViewById(R.id.editTextQFL);
            EditText editTextBLL = settingsView.findViewById(R.id.editTextBLL);
            EditText editTextSLL = settingsView.findViewById(R.id.editTextSLL);
            EditText editTextSRR = settingsView.findViewById(R.id.editTextSRR);
            EditText editTextPWR = settingsView.findViewById(R.id.editTextPWR);
            EditText editTextSR = settingsView.findViewById(R.id.editTextSR);
            EditText editTextGR = settingsView.findViewById(R.id.editTextGR);
            EditText editTextQFR = settingsView.findViewById(R.id.editTextQFR);
            EditText editTextBLR = settingsView.findViewById(R.id.editTextBLR);
            EditText editTextSLR = settingsView.findViewById(R.id.editTextSLR);

            Spinner spinnerVL = settingsView.findViewById(R.id.spinnerVL);
            Spinner spinnerVR = settingsView.findViewById(R.id.spinnerVR);
            Spinner spinnerSPSL = settingsView.findViewById(R.id.spinnerSPSL);
            Spinner spinnerSPSR = settingsView.findViewById(R.id.spinnerSPSR);
            Spinner spinnerSOL = settingsView.findViewById(R.id.spinnerSOL);
            Spinner spinnerSOR = settingsView.findViewById(R.id.spinnerSOR);
            Spinner spinnerSML = settingsView.findViewById(R.id.spinnerSML);
            Spinner spinnerSMR = settingsView.findViewById(R.id.spinnerSMR);
            Spinner spinnerWL = settingsView.findViewById(R.id.spinnerWL);
            Spinner spinnerWR = settingsView.findViewById(R.id.spinnerWR);
            Spinner spinnerNML = settingsView.findViewById(R.id.spinnerNML);
            Spinner spinnerNMR = settingsView.findViewById(R.id.spinnerNMR);

            spinnerNML.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Log.v("item", (String) parent.getItemAtPosition(position));
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            spinnerNMR.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Log.v("item", (String) parent.getItemAtPosition(position));
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });



//          return inflater.inflate(R.layout.settings, container, false);
            return settingsView;
        }
    }

    public static class ElectrodesFragment extends Fragment {
        public ElectrodesFragment() {

        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.activity_electrodes, container, false);
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new ParametersFragment();
                default:
                    return new ElectrodesFragment();
            }

            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).

            // return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        getMAPFromPreferences();
        //update preferences each time the Settings activity is opened
    }

    void setUpWidgets() {
        // Retrieve the widgets
//        textViewITL = findViewById(R.id.textViewITL);
//        textViewSFL = findViewById(R.id.textViewSFL);
//        textViewNCL = findViewById(R.id.textViewNCL);
//        textViewFTL = findViewById(R.id.textViewFTL);
//        textViewITR = findViewById(R.id.textViewITR);
//        textViewSFR = findViewById(R.id.textViewSFR);
//        textViewNCR = findViewById(R.id.textViewNCR);
//        textViewFTR = findViewById(R.id.textViewFTR);

//        textView38 = findViewById(R.id.textView38);
//        textView46 = findViewById(R.id.textView46);
//        textView54 = findViewById(R.id.textView54);
//        textView62 = findViewById(R.id.textView62);
//        textView70 = findViewById(R.id.textView70);
//        textView78 = findViewById(R.id.textView78);
//        textView86 = findViewById(R.id.textView86);
//        textView94 = findViewById(R.id.textView94);
//        textView102 = findViewById(R.id.textView102);
//        textView110 = findViewById(R.id.textView110);
//        textView118 = findViewById(R.id.textView118);
//        textView126 = findViewById(R.id.textView126);
//        textView134 = findViewById(R.id.textView134);
//        textView142 = findViewById(R.id.textView142);
//        textView150 = findViewById(R.id.textView150);
//        textView158 = findViewById(R.id.textView158);
//        textView166 = findViewById(R.id.textView166);
//        textView174 = findViewById(R.id.textView174);
//        textView182 = findViewById(R.id.textView182);
//        textView190 = findViewById(R.id.textView190);
//        textView198 = findViewById(R.id.textView198);
//        textView206 = findViewById(R.id.textView206);

//        textView39 = findViewById(R.id.textView39);
//        textView47 = findViewById(R.id.textView47);
//        textView55 = findViewById(R.id.textView55);
//        textView63 = findViewById(R.id.textView63);
//        textView71 = findViewById(R.id.textView71);
//        textView79 = findViewById(R.id.textView79);
//        textView87 = findViewById(R.id.textView87);
//        textView95 = findViewById(R.id.textView95);
//        textView103 = findViewById(R.id.textView103);
//        textView111 = findViewById(R.id.textView111);
//        textView119 = findViewById(R.id.textView119);
//        textView127 = findViewById(R.id.textView127);
//        textView135 = findViewById(R.id.textView135);
//        textView143 = findViewById(R.id.textView143);
//        textView151 = findViewById(R.id.textView151);
//        textView159 = findViewById(R.id.textView159);
//        textView167 = findViewById(R.id.textView167);
//        textView175 = findViewById(R.id.textView175);
//        textView183 = findViewById(R.id.textView183);
//        textView191 = findViewById(R.id.textView191);
//        textView199 = findViewById(R.id.textView199);
//        textView207 = findViewById(R.id.textView207);
//
//        textView40 = findViewById(R.id.textView40);
//        textView48 = findViewById(R.id.textView48);
//        textView56 = findViewById(R.id.textView56);
//        textView64 = findViewById(R.id.textView64);
//        textView72 = findViewById(R.id.textView72);
//        textView80 = findViewById(R.id.textView80);
//        textView88 = findViewById(R.id.textView88);
//        textView96 = findViewById(R.id.textView96);
//        textView104 = findViewById(R.id.textView104);
//        textView112 = findViewById(R.id.textView112);
//        textView120 = findViewById(R.id.textView120);
//        textView128 = findViewById(R.id.textView128);
//        textView136 = findViewById(R.id.textView136);
//        textView144 = findViewById(R.id.textView144);
//        textView152 = findViewById(R.id.textView152);
//        textView160 = findViewById(R.id.textView160);
//        textView168 = findViewById(R.id.textView168);
//        textView176 = findViewById(R.id.textView176);
//        textView184 = findViewById(R.id.textView184);
//        textView192 = findViewById(R.id.textView192);
//        textView200 = findViewById(R.id.textView200);
//        textView208 = findViewById(R.id.textView208);

//        editTextSRL = findViewById(R.id.editTextSRL);
//        editTextPWL = findViewById(R.id.editTextPWL);
//        editTextSL = findViewById(R.id.editTextSL);
//        editTextGL = findViewById(R.id.editTextGL);
//        editTextQFL = findViewById(R.id.editTextQFL);
//        editTextBLL = findViewById(R.id.editTextBLL);
//        editTextSLL = findViewById(R.id.editTextSLL);
//        editTextSRR = findViewById(R.id.editTextSRR);
//        editTextPWR = findViewById(R.id.editTextPWR);
//        editTextSR = findViewById(R.id.editTextSR);
//        editTextGR = findViewById(R.id.editTextGR);
//        editTextQFR = findViewById(R.id.editTextQFR);
//        editTextBLR = findViewById(R.id.editTextBLR);
//        editTextSLR = findViewById(R.id.editTextSLR);
//
//        textView41 = findViewById(R.id.textView41);
//        textView49 = findViewById(R.id.textView49);
//        textView57 = findViewById(R.id.textView57);
//        textView65 = findViewById(R.id.textView65);
//        textView73 = findViewById(R.id.textView73);
//        textView79 = findViewById(R.id.textView79);
//        textView81 = findViewById(R.id.textView81);
//        textView89 = findViewById(R.id.textView89);
//        textView97 = findViewById(R.id.textView97);
//        textView105 = findViewById(R.id.textView105);
//        textView113 = findViewById(R.id.textView113);
//        textView121 = findViewById(R.id.textView121);
//        textView129 = findViewById(R.id.textView129);
//        textView137 = findViewById(R.id.textView137);
//        textView145 = findViewById(R.id.textView145);
//        textView153 = findViewById(R.id.textView153);
//        textView161 = findViewById(R.id.textView161);
//        textView169 = findViewById(R.id.textView169);
//        textView177 = findViewById(R.id.textView177);
//        textView185 = findViewById(R.id.textView185);
//        textView193 = findViewById(R.id.textView193);
//        textView201 = findViewById(R.id.textView201);
//        textView209 = findViewById(R.id.textView209);
//
//        textView42 = findViewById(R.id.textView42);
//        textView50 = findViewById(R.id.textView50);
//        textView58 = findViewById(R.id.textView58);
//        textView66 = findViewById(R.id.textView66);
//        textView74 = findViewById(R.id.textView74);
//        textView79 = findViewById(R.id.textView79);
//        textView82 = findViewById(R.id.textView82);
//        textView90 = findViewById(R.id.textView90);
//        textView98 = findViewById(R.id.textView98);
//        textView106 = findViewById(R.id.textView106);
//        textView114 = findViewById(R.id.textView114);
//        textView122 = findViewById(R.id.textView122);
//        textView130 = findViewById(R.id.textView130);
//        textView138 = findViewById(R.id.textView138);
//        textView146 = findViewById(R.id.textView146);
//        textView154 = findViewById(R.id.textView154);
//        textView162 = findViewById(R.id.textView162);
//        textView170 = findViewById(R.id.textView170);
//        textView178 = findViewById(R.id.textView178);
//        textView186 = findViewById(R.id.textView186);
//        textView194 = findViewById(R.id.textView194);
//        textView202 = findViewById(R.id.textView202);
//        textView210 = findViewById(R.id.textView210);
//
//        textView43 = findViewById(R.id.textView43);
//        textView51 = findViewById(R.id.textView51);
//        textView59 = findViewById(R.id.textView59);
//        textView67 = findViewById(R.id.textView67);
//        textView75 = findViewById(R.id.textView75);
//        textView79 = findViewById(R.id.textView79);
//        textView83 = findViewById(R.id.textView83);
//        textView91 = findViewById(R.id.textView91);
//        textView99 = findViewById(R.id.textView99);
//        textView107 = findViewById(R.id.textView107);
//        textView115 = findViewById(R.id.textView115);
//        textView123 = findViewById(R.id.textView123);
//        textView131 = findViewById(R.id.textView131);
//        textView139 = findViewById(R.id.textView139);
//        textView147 = findViewById(R.id.textView147);
//        textView155 = findViewById(R.id.textView155);
//        textView163 = findViewById(R.id.textView163);
//        textView171 = findViewById(R.id.textView171);
//        textView179 = findViewById(R.id.textView179);
//        textView187 = findViewById(R.id.textView187);
//        textView195 = findViewById(R.id.textView195);
//        textView203 = findViewById(R.id.textView203);
//        textView211 = findViewById(R.id.textView211);

//        spinnerVL = findViewById(R.id.spinnerVL);
//        spinnerVR = findViewById(R.id.spinnerVR);
//        spinnerSPSL = findViewById(R.id.spinnerSPSL);
//        spinnerSPSR = findViewById(R.id.spinnerSPSR);
//        spinnerSOL = findViewById(R.id.spinnerSOL);
//        spinnerSOR = findViewById(R.id.spinnerSOR);
//        spinnerSML = findViewById(R.id.spinnerSML);
//        spinnerSMR = findViewById(R.id.spinnerSMR);
//        spinnerWL = findViewById(R.id.spinnerWL);
//        spinnerWR = findViewById(R.id.spinnerWR);
//        spinnerNML = findViewById(R.id.spinnerNML);
//        spinnerNMR = findViewById(R.id.spinnerNMR);

        //Button updateButton = findViewById(R.id.updateButton);

//        spinnerNML.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                Log.v("item", (String) parent.getItemAtPosition(position));
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });
//
//        spinnerNMR.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                Log.v("item", (String) parent.getItemAtPosition(position));
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });

//        assert updateButton != null;
//        updateButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                updateButtonText();
//            }
//        });
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
            leftMAPsensitivity = getDouble(preferences, "Left.sensitivity");
            leftMAPgain = getDouble(preferences, "Left.gain");
            leftMAPvolume = preferences.getInt("Left.volume", 0);
            leftMAPQfactor = getDouble(preferences, "Left.Qfactor");
            leftMAPbaseLevel = getDouble(preferences, "Left.baseLevel");
            leftMAPsaturationLevel = getDouble(preferences, "Left.saturationLevel");
            leftMAPstimulationOrder = preferences.getString("Left.stimulationOrder", "");
            leftMAPwindow = preferences.getString("Left.window", "");

            leftMAPTHR = new int[leftMAPnbands];
            leftMAPMCL = new int[leftMAPnbands];
            leftMAPgains = new double[leftMAPnbands];
            leftMAPelectrodes = new int[leftMAPnbands];

            for (int i = 0; i < leftMAPnbands; i++) {
                leftMAPTHR[i] = preferences.getInt("leftTHR" + i, 0);
                leftMAPMCL[i] = preferences.getInt("leftMCL" + i, 0);
                leftMAPgains[i] = getDouble(preferences, "leftgain" + i);
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
            rightMAPsensitivity = getDouble(preferences, "Right.sensitivity");
            rightMAPgain = getDouble(preferences, "Right.gain");
            rightMAPvolume = preferences.getInt("Right.volume", 0);
            rightMAPQfactor = getDouble(preferences, "Right.Qfactor");
            rightMAPbaseLevel = getDouble(preferences, "Right.baseLevel");
            rightMAPsaturationLevel = getDouble(preferences, "Right.saturationLevel");
            rightMAPstimulationOrder = preferences.getString("Right.stimulationOrder", "");
            rightMAPwindow = preferences.getString("Right.window", "");

            rightMAPTHR = new int[rightMAPnbands];
            rightMAPMCL = new int[rightMAPnbands];
            rightMAPgains = new double[rightMAPnbands];
            rightMAPelectrodes = new int[rightMAPnbands];

            for (int i = 0; i < rightMAPnbands; i++) {
                rightMAPTHR[i] = preferences.getInt("rightTHR" + i, 0);
                rightMAPMCL[i] = preferences.getInt("rightMCL" + i, 0);
                rightMAPgains[i] = getDouble(preferences, "rightgain" + i);
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

        spinnerSPSL.setAdapter(soundProcessAdapterLeft);
        spinnerSOL.setAdapter(stimOrderAdapterLeft);
        spinnerSML.setAdapter(stimModeAdapterLeft);
        spinnerWL.setAdapter(windowAdapterLeft);

        ArrayAdapter<CharSequence> volumeAdapterLeft = ArrayAdapter.createFromResource(SettingsActivity.this,
                R.array.volumeSpinnerItems, android.R.layout.simple_spinner_item);
        volumeAdapterLeft.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerVL.setAdapter(volumeAdapterLeft);

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
        spinnerNML.setAdapter(nMaximaAdapterLeft);



        textViewITL.setText(leftMAPimplantType);
        textViewSFL.setText(String.valueOf(leftMAPsamplingFrequency));
        textViewNCL.setText(String.valueOf(leftMAPnumberOfChannels));
        textViewFTL.setText(leftMAPfrequencyTable);

        editTextSRL.setText(String.valueOf(leftMAPstimulationRate), TextView.BufferType.EDITABLE);
        editTextPWL.setText(String.valueOf(leftMAPpulseWidth), TextView.BufferType.EDITABLE);
        editTextSL.setText(String.valueOf(leftMAPsensitivity), TextView.BufferType.EDITABLE);
        editTextGL.setText(String.valueOf(leftMAPgain), TextView.BufferType.EDITABLE);
        editTextQFL.setText(String.valueOf(leftMAPQfactor), TextView.BufferType.EDITABLE);
        editTextBLL.setText(String.valueOf(leftMAPbaseLevel), TextView.BufferType.EDITABLE);
        editTextSLL.setText(String.valueOf(leftMAPsaturationLevel), TextView.BufferType.EDITABLE);

        spinnerNML.setSelection(getSpinIndex(spinnerNML, String.valueOf(leftMAPnMaxima)));
        spinnerVL.setSelection(getSpinIndex(spinnerVL, String.valueOf(leftMAPvolume)));
        spinnerSOL.setSelection(getSpinIndex(spinnerSOL, leftMAPstimulationOrder));
        spinnerWL.setSelection(getSpinIndex(spinnerWL, leftMAPwindow));

        if (leftMAPelectrodes.length == 22) { // left electrode array
//            textView38.setText(String.valueOf(leftMAPTHR[0]));
//            textView46.setText(String.valueOf(leftMAPTHR[1]));
//            textView54.setText(String.valueOf(leftMAPTHR[2]));
//            textView62.setText(String.valueOf(leftMAPTHR[3]));
//            textView70.setText(String.valueOf(leftMAPTHR[4]));
//            textView78.setText(String.valueOf(leftMAPTHR[5]));
//            textView86.setText(String.valueOf(leftMAPTHR[6]));
//            textView94.setText(String.valueOf(leftMAPTHR[7]));
//            textView102.setText(String.valueOf(leftMAPTHR[8]));
//            textView110.setText(String.valueOf(leftMAPTHR[9]));
//            textView118.setText(String.valueOf(leftMAPTHR[10]));
//            textView126.setText(String.valueOf(leftMAPTHR[11]));
//            textView134.setText(String.valueOf(leftMAPTHR[12]));
//            textView142.setText(String.valueOf(leftMAPTHR[13]));
//            textView150.setText(String.valueOf(leftMAPTHR[14]));
//            textView158.setText(String.valueOf(leftMAPTHR[15]));
//            textView166.setText(String.valueOf(leftMAPTHR[16]));
//            textView174.setText(String.valueOf(leftMAPTHR[17]));
//            textView182.setText(String.valueOf(leftMAPTHR[18]));
//            textView190.setText(String.valueOf(leftMAPTHR[19]));
//            textView198.setText(String.valueOf(leftMAPTHR[20]));
//            textView206.setText(String.valueOf(leftMAPTHR[21]));
//
//            textView39.setText(String.valueOf(leftMAPMCL[0]));
//            textView47.setText(String.valueOf(leftMAPMCL[1]));
//            textView55.setText(String.valueOf(leftMAPMCL[2]));
//            textView63.setText(String.valueOf(leftMAPMCL[3]));
//            textView71.setText(String.valueOf(leftMAPMCL[4]));
//            textView79.setText(String.valueOf(leftMAPMCL[5]));
//            textView87.setText(String.valueOf(leftMAPMCL[6]));
//            textView95.setText(String.valueOf(leftMAPMCL[7]));
//            textView103.setText(String.valueOf(leftMAPMCL[8]));
//            textView111.setText(String.valueOf(leftMAPMCL[9]));
//            textView119.setText(String.valueOf(leftMAPMCL[10]));
//            textView127.setText(String.valueOf(leftMAPMCL[11]));
//            textView135.setText(String.valueOf(leftMAPMCL[12]));
//            textView143.setText(String.valueOf(leftMAPMCL[13]));
//            textView151.setText(String.valueOf(leftMAPMCL[14]));
//            textView159.setText(String.valueOf(leftMAPMCL[15]));
//            textView167.setText(String.valueOf(leftMAPMCL[16]));
//            textView175.setText(String.valueOf(leftMAPMCL[17]));
//            textView183.setText(String.valueOf(leftMAPMCL[18]));
//            textView191.setText(String.valueOf(leftMAPMCL[19]));
//            textView199.setText(String.valueOf(leftMAPMCL[20]));
//            textView207.setText(String.valueOf(leftMAPMCL[21]));
//
//            textView40.setText(String.valueOf(leftMAPgains[0]));
//            textView48.setText(String.valueOf(leftMAPgains[1]));
//            textView56.setText(String.valueOf(leftMAPgains[2]));
//            textView64.setText(String.valueOf(leftMAPgains[3]));
//            textView72.setText(String.valueOf(leftMAPgains[4]));
//            textView80.setText(String.valueOf(leftMAPgains[5]));
//            textView88.setText(String.valueOf(leftMAPgains[6]));
//            textView96.setText(String.valueOf(leftMAPgains[7]));
//            textView104.setText(String.valueOf(leftMAPgains[8]));
//            textView112.setText(String.valueOf(leftMAPgains[9]));
//            textView120.setText(String.valueOf(leftMAPgains[10]));
//            textView128.setText(String.valueOf(leftMAPgains[11]));
//            textView136.setText(String.valueOf(leftMAPgains[12]));
//            textView144.setText(String.valueOf(leftMAPgains[13]));
//            textView152.setText(String.valueOf(leftMAPgains[14]));
//            textView160.setText(String.valueOf(leftMAPgains[15]));
//            textView168.setText(String.valueOf(leftMAPgains[16]));
//            textView176.setText(String.valueOf(leftMAPgains[17]));
//            textView184.setText(String.valueOf(leftMAPgains[18]));
//            textView192.setText(String.valueOf(leftMAPgains[19]));
//            textView200.setText(String.valueOf(leftMAPgains[20]));
//            textView208.setText(String.valueOf(leftMAPgains[21]));
        } else {

            int electrodeNum;
//            for (int i = 0; i < leftMAPelectrodes.length; i++) {
//                electrodeNum = leftMAPelectrodes[i];
//                switch (electrodeNum) {
//                    case 1:
//                        textView38.setText(String.valueOf(leftMAPTHR[i]));
//                        textView39.setText(String.valueOf(leftMAPMCL[i]));
//                        textView40.setText(String.valueOf(leftMAPgains[i]));
//                        break;
//                    case 2:
//                        textView46.setText(String.valueOf(leftMAPTHR[i]));
//                        textView47.setText(String.valueOf(leftMAPMCL[i]));
//                        textView48.setText(String.valueOf(leftMAPgains[i]));
//                        break;
//                    case 3:
//                        textView54.setText(String.valueOf(leftMAPTHR[i]));
//                        textView55.setText(String.valueOf(leftMAPMCL[i]));
//                        textView56.setText(String.valueOf(leftMAPgains[i]));
//                        break;
//                    case 4:
//                        textView62.setText(String.valueOf(leftMAPTHR[i]));
//                        textView63.setText(String.valueOf(leftMAPMCL[i]));
//                        textView64.setText(String.valueOf(leftMAPgains[i]));
//                        break;
//                    case 5:
//                        textView70.setText(String.valueOf(leftMAPTHR[i]));
//                        textView71.setText(String.valueOf(leftMAPMCL[i]));
//                        textView72.setText(String.valueOf(leftMAPgains[i]));
//                        break;
//                    case 6:
//                        textView78.setText(String.valueOf(leftMAPTHR[i]));
//                        textView79.setText(String.valueOf(leftMAPMCL[i]));
//                        textView80.setText(String.valueOf(leftMAPgains[i]));
//                        break;
//                    case 7:
//                        textView86.setText(String.valueOf(leftMAPTHR[i]));
//                        textView87.setText(String.valueOf(leftMAPMCL[i]));
//                        textView88.setText(String.valueOf(leftMAPgains[i]));
//                        break;
//                    case 8:
//                        textView94.setText(String.valueOf(leftMAPTHR[i]));
//                        textView95.setText(String.valueOf(leftMAPMCL[i]));
//                        textView96.setText(String.valueOf(leftMAPgains[i]));
//                        break;
//                    case 9:
//                        textView102.setText(String.valueOf(leftMAPTHR[i]));
//                        textView103.setText(String.valueOf(leftMAPMCL[i]));
//                        textView104.setText(String.valueOf(leftMAPgains[i]));
//                        break;
//                    case 10:
//                        textView110.setText(String.valueOf(leftMAPTHR[i]));
//                        textView111.setText(String.valueOf(leftMAPMCL[i]));
//                        textView112.setText(String.valueOf(leftMAPgains[i]));
//                    case 11:
//                        textView118.setText(String.valueOf(leftMAPTHR[i]));
//                        textView119.setText(String.valueOf(leftMAPMCL[i]));
//                        textView120.setText(String.valueOf(leftMAPgains[i]));
//                        break;
//                    case 12:
//                        textView126.setText(String.valueOf(leftMAPTHR[i]));
//                        textView127.setText(String.valueOf(leftMAPMCL[i]));
//                        textView128.setText(String.valueOf(leftMAPgains[i]));
//                        break;
//                    case 13:
//                        textView134.setText(String.valueOf(leftMAPTHR[i]));
//                        textView135.setText(String.valueOf(leftMAPMCL[i]));
//                        textView136.setText(String.valueOf(leftMAPgains[i]));
//                        break;
//                    case 14:
//                        textView142.setText(String.valueOf(leftMAPTHR[i]));
//                        textView143.setText(String.valueOf(leftMAPMCL[i]));
//                        textView144.setText(String.valueOf(leftMAPgains[i]));
//                        break;
//                    case 15:
//                        textView150.setText(String.valueOf(leftMAPTHR[i]));
//                        textView151.setText(String.valueOf(leftMAPMCL[i]));
//                        textView152.setText(String.valueOf(leftMAPgains[i]));
//                        break;
//                    case 16:
//                        textView158.setText(String.valueOf(leftMAPTHR[i]));
//                        textView159.setText(String.valueOf(leftMAPMCL[i]));
//                        textView160.setText(String.valueOf(leftMAPgains[i]));
//                        break;
//                    case 17:
//                        textView166.setText(String.valueOf(leftMAPTHR[i]));
//                        textView167.setText(String.valueOf(leftMAPMCL[i]));
//                        textView168.setText(String.valueOf(leftMAPgains[i]));
//                        break;
//                    case 18:
//                        textView174.setText(String.valueOf(leftMAPTHR[i]));
//                        textView175.setText(String.valueOf(leftMAPMCL[i]));
//                        textView176.setText(String.valueOf(leftMAPgains[i]));
//                        break;
//                    case 19:
//                        textView182.setText(String.valueOf(leftMAPTHR[i]));
//                        textView183.setText(String.valueOf(leftMAPMCL[i]));
//                        textView184.setText(String.valueOf(leftMAPgains[i]));
//                        break;
//                    case 20:
//                        textView190.setText(String.valueOf(leftMAPTHR[i]));
//                        textView191.setText(String.valueOf(leftMAPMCL[i]));
//                        textView192.setText(String.valueOf(leftMAPgains[i]));
//                        break;
//                    case 21:
//                        textView198.setText(String.valueOf(leftMAPTHR[i]));
//                        textView199.setText(String.valueOf(leftMAPMCL[i]));
//                        textView200.setText(String.valueOf(leftMAPgains[i]));
//                        break;
//                    case 22:
//                        textView206.setText(String.valueOf(leftMAPTHR[i]));
//                        textView207.setText(String.valueOf(leftMAPMCL[i]));
//                        textView208.setText(String.valueOf(leftMAPgains[i]));
//                        break;
//                }
//            }
        }

    }

    void noLeftMAPText() {
        textViewITL.setEnabled(false);
        textViewSFL.setEnabled(false);
        textViewNCL.setEnabled(false);
        textViewFTL.setEnabled(false);

        editTextSRL.setEnabled(false);
        editTextPWL.setEnabled(false);
        editTextSL.setEnabled(false);
        editTextGL.setEnabled(false);
        editTextQFL.setEnabled(false);
        editTextBLL.setEnabled(false);
        editTextSLL.setEnabled(false);

        spinnerSPSL.setEnabled(false);
        spinnerNML.setEnabled(false);
        spinnerSML.setEnabled(false);
        spinnerVL.setEnabled(false);
        spinnerSOL.setEnabled(false);
        spinnerWL.setEnabled(false);

        // Disable text (reduce alpha to 38%)
        TextView textViewLeft = findViewById(R.id.textViewLeft);
        if (textViewLeft != null) {
            textViewLeft.setTextColor(Color.argb(disabledAlpha, 0, 0, 0));
        }

        TextView textView26 = findViewById(R.id.textView26);
        if (textView26 != null) {
            textView26.setTextColor(Color.argb(disabledAlpha, 0, 0, 0));
        }

        TextView textView30 = findViewById(R.id.textView30);
        if (textView30 != null) {
            textView30.setTextColor(Color.argb(disabledAlpha, 0, 0, 0));
        }

        TextView textView31 = findViewById(R.id.textView31);
        if (textView31 != null) {
            textView31.setTextColor(Color.argb(disabledAlpha, 0, 0, 0));
        }

        TextView textView32 = findViewById(R.id.textView32);
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

        spinnerSPSR.setAdapter(soundProcessAdapterRight);
        spinnerSOR.setAdapter(stimOrderAdapterRight);
        spinnerSMR.setAdapter(stimModeAdapterRight);
        spinnerWR.setAdapter(windowAdapterRight);

        ArrayAdapter<CharSequence> volumeAdapterRight = ArrayAdapter.createFromResource(SettingsActivity.this,
                R.array.volumeSpinnerItems, android.R.layout.simple_spinner_item);
        volumeAdapterRight.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerVR.setAdapter(volumeAdapterRight);

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
        spinnerNMR.setAdapter(nMaximaAdapterRight);

        textViewITR.setText(rightMAPimplantType);
        textViewSFR.setText(String.valueOf(rightMAPsamplingFrequency));
        textViewNCR.setText(String.valueOf(rightMAPnumberOfChannels));
        textViewFTR.setText(rightMAPfrequencyTable);

        editTextSRR.setText(String.valueOf(rightMAPstimulationRate), TextView.BufferType.EDITABLE);
        editTextPWR.setText(String.valueOf(rightMAPpulseWidth), TextView.BufferType.EDITABLE);
        editTextSR.setText(String.valueOf(rightMAPsensitivity), TextView.BufferType.EDITABLE);
        editTextGR.setText(String.valueOf(rightMAPgain), TextView.BufferType.EDITABLE);
        editTextQFR.setText(String.valueOf(rightMAPQfactor), TextView.BufferType.EDITABLE);
        editTextBLR.setText(String.valueOf(rightMAPbaseLevel), TextView.BufferType.EDITABLE);
        editTextSLR.setText(String.valueOf(rightMAPsaturationLevel), TextView.BufferType.EDITABLE);

        spinnerNMR.setSelection(getSpinIndex(spinnerNMR, String.valueOf(rightMAPnMaxima)));
        spinnerVR.setSelection(getSpinIndex(spinnerVR, String.valueOf(rightMAPvolume)));
        spinnerSOR.setSelection(getSpinIndex(spinnerSOR, rightMAPstimulationOrder));
        spinnerWR.setSelection(getSpinIndex(spinnerWR, rightMAPwindow));

        if (rightMAPelectrodes.length == 22) { // right electrode array
//            textView41.setText(String.valueOf(rightMAPTHR[0]));
//            textView49.setText(String.valueOf(rightMAPTHR[1]));
//            textView57.setText(String.valueOf(rightMAPTHR[2]));
//            textView65.setText(String.valueOf(rightMAPTHR[3]));
//            textView73.setText(String.valueOf(rightMAPTHR[4]));
//            textView81.setText(String.valueOf(rightMAPTHR[5]));
//            textView89.setText(String.valueOf(rightMAPTHR[6]));
//            textView97.setText(String.valueOf(rightMAPTHR[7]));
//            textView105.setText(String.valueOf(rightMAPTHR[8]));
//            textView113.setText(String.valueOf(rightMAPTHR[9]));
//            textView121.setText(String.valueOf(rightMAPTHR[10]));
//            textView129.setText(String.valueOf(rightMAPTHR[11]));
//            textView137.setText(String.valueOf(rightMAPTHR[12]));
//            textView145.setText(String.valueOf(rightMAPTHR[13]));
//            textView153.setText(String.valueOf(rightMAPTHR[14]));
//            textView161.setText(String.valueOf(rightMAPTHR[15]));
//            textView169.setText(String.valueOf(rightMAPTHR[16]));
//            textView177.setText(String.valueOf(rightMAPTHR[17]));
//            textView185.setText(String.valueOf(rightMAPTHR[18]));
//            textView193.setText(String.valueOf(rightMAPTHR[19]));
//            textView201.setText(String.valueOf(rightMAPTHR[20]));
//            textView209.setText(String.valueOf(rightMAPTHR[21]));
//
//            textView42.setText(String.valueOf(rightMAPMCL[0]));
//            textView50.setText(String.valueOf(rightMAPMCL[1]));
//            textView58.setText(String.valueOf(rightMAPMCL[2]));
//            textView66.setText(String.valueOf(rightMAPMCL[3]));
//            textView74.setText(String.valueOf(rightMAPMCL[4]));
//            textView82.setText(String.valueOf(rightMAPMCL[5]));
//            textView90.setText(String.valueOf(rightMAPMCL[6]));
//            textView98.setText(String.valueOf(rightMAPMCL[7]));
//            textView106.setText(String.valueOf(rightMAPMCL[8]));
//            textView114.setText(String.valueOf(rightMAPMCL[9]));
//            textView122.setText(String.valueOf(rightMAPMCL[10]));
//            textView130.setText(String.valueOf(rightMAPMCL[11]));
//            textView138.setText(String.valueOf(rightMAPMCL[12]));
//            textView146.setText(String.valueOf(rightMAPMCL[13]));
//            textView154.setText(String.valueOf(rightMAPMCL[14]));
//            textView162.setText(String.valueOf(rightMAPMCL[15]));
//            textView170.setText(String.valueOf(rightMAPMCL[16]));
//            textView178.setText(String.valueOf(rightMAPMCL[17]));
//            textView186.setText(String.valueOf(rightMAPMCL[18]));
//            textView194.setText(String.valueOf(rightMAPMCL[19]));
//            textView202.setText(String.valueOf(rightMAPMCL[20]));
//            textView210.setText(String.valueOf(rightMAPMCL[21]));
//
//            textView43.setText(String.valueOf(rightMAPgains[0]));
//            textView51.setText(String.valueOf(rightMAPgains[1]));
//            textView59.setText(String.valueOf(rightMAPgains[2]));
//            textView67.setText(String.valueOf(rightMAPgains[3]));
//            textView75.setText(String.valueOf(rightMAPgains[4]));
//            textView83.setText(String.valueOf(rightMAPgains[5]));
//            textView91.setText(String.valueOf(rightMAPgains[6]));
//            textView99.setText(String.valueOf(rightMAPgains[7]));
//            textView107.setText(String.valueOf(rightMAPgains[8]));
//            textView115.setText(String.valueOf(rightMAPgains[9]));
//            textView123.setText(String.valueOf(rightMAPgains[10]));
//            textView131.setText(String.valueOf(rightMAPgains[11]));
//            textView139.setText(String.valueOf(rightMAPgains[12]));
//            textView147.setText(String.valueOf(rightMAPgains[13]));
//            textView155.setText(String.valueOf(rightMAPgains[14]));
//            textView163.setText(String.valueOf(rightMAPgains[15]));
//            textView171.setText(String.valueOf(rightMAPgains[16]));
//            textView179.setText(String.valueOf(rightMAPgains[17]));
//            textView187.setText(String.valueOf(rightMAPgains[18]));
//            textView195.setText(String.valueOf(rightMAPgains[19]));
//            textView203.setText(String.valueOf(rightMAPgains[20]));
//            textView211.setText(String.valueOf(rightMAPgains[21]));
        } else {

            int electrodeNum;
//            for (int i = 0; i < rightMAPelectrodes.length; i++) {
//                electrodeNum = rightMAPelectrodes[i];
//                switch (electrodeNum) {
//                    case 1:
//                        textView41.setText(String.valueOf(rightMAPTHR[i]));
//                        textView42.setText(String.valueOf(rightMAPMCL[i]));
//                        textView43.setText(String.valueOf(rightMAPgains[i]));
//                        break;
//                    case 2:
//                        textView49.setText(String.valueOf(rightMAPTHR[i]));
//                        textView50.setText(String.valueOf(rightMAPMCL[i]));
//                        textView51.setText(String.valueOf(rightMAPgains[i]));
//                        break;
//                    case 3:
//                        textView57.setText(String.valueOf(rightMAPTHR[i]));
//                        textView58.setText(String.valueOf(rightMAPMCL[i]));
//                        textView59.setText(String.valueOf(rightMAPgains[i]));
//                        break;
//                    case 4:
//                        textView65.setText(String.valueOf(rightMAPTHR[i]));
//                        textView66.setText(String.valueOf(rightMAPMCL[i]));
//                        textView67.setText(String.valueOf(rightMAPgains[i]));
//                        break;
//                    case 5:
//                        textView73.setText(String.valueOf(rightMAPTHR[i]));
//                        textView74.setText(String.valueOf(rightMAPMCL[i]));
//                        textView75.setText(String.valueOf(rightMAPgains[i]));
//                        break;
//                    case 6:
//                        textView81.setText(String.valueOf(rightMAPTHR[i]));
//                        textView82.setText(String.valueOf(rightMAPMCL[i]));
//                        textView83.setText(String.valueOf(rightMAPgains[i]));
//                        break;
//                    case 7:
//                        textView89.setText(String.valueOf(rightMAPTHR[i]));
//                        textView90.setText(String.valueOf(rightMAPMCL[i]));
//                        textView91.setText(String.valueOf(rightMAPgains[i]));
//                        break;
//                    case 8:
//                        textView97.setText(String.valueOf(rightMAPTHR[i]));
//                        textView98.setText(String.valueOf(rightMAPMCL[i]));
//                        textView99.setText(String.valueOf(rightMAPgains[i]));
//                        break;
//                    case 9:
//                        textView105.setText(String.valueOf(rightMAPTHR[i]));
//                        textView106.setText(String.valueOf(rightMAPMCL[i]));
//                        textView107.setText(String.valueOf(rightMAPgains[i]));
//                        break;
//                    case 10:
//                        textView113.setText(String.valueOf(rightMAPTHR[i]));
//                        textView114.setText(String.valueOf(rightMAPMCL[i]));
//                        textView115.setText(String.valueOf(rightMAPgains[i]));
//                    case 11:
//                        textView121.setText(String.valueOf(rightMAPTHR[i]));
//                        textView122.setText(String.valueOf(rightMAPMCL[i]));
//                        textView123.setText(String.valueOf(rightMAPgains[i]));
//                        break;
//                    case 12:
//                        textView129.setText(String.valueOf(rightMAPTHR[i]));
//                        textView130.setText(String.valueOf(rightMAPMCL[i]));
//                        textView131.setText(String.valueOf(rightMAPgains[i]));
//                        break;
//                    case 13:
//                        textView137.setText(String.valueOf(rightMAPTHR[i]));
//                        textView138.setText(String.valueOf(rightMAPMCL[i]));
//                        textView139.setText(String.valueOf(rightMAPgains[i]));
//                        break;
//                    case 14:
//                        textView145.setText(String.valueOf(rightMAPTHR[i]));
//                        textView146.setText(String.valueOf(rightMAPMCL[i]));
//                        textView147.setText(String.valueOf(rightMAPgains[i]));
//                        break;
//                    case 15:
//                        textView153.setText(String.valueOf(rightMAPTHR[i]));
//                        textView154.setText(String.valueOf(rightMAPMCL[i]));
//                        textView155.setText(String.valueOf(rightMAPgains[i]));
//                        break;
//                    case 16:
//                        textView161.setText(String.valueOf(rightMAPTHR[i]));
//                        textView162.setText(String.valueOf(rightMAPMCL[i]));
//                        textView163.setText(String.valueOf(rightMAPgains[i]));
//                        break;
//                    case 17:
//                        textView169.setText(String.valueOf(rightMAPTHR[i]));
//                        textView170.setText(String.valueOf(rightMAPMCL[i]));
//                        textView171.setText(String.valueOf(rightMAPgains[i]));
//                        break;
//                    case 18:
//                        textView177.setText(String.valueOf(rightMAPTHR[i]));
//                        textView178.setText(String.valueOf(rightMAPMCL[i]));
//                        textView179.setText(String.valueOf(rightMAPgains[i]));
//                        break;
//                    case 19:
//                        textView185.setText(String.valueOf(rightMAPTHR[i]));
//                        textView186.setText(String.valueOf(rightMAPMCL[i]));
//                        textView187.setText(String.valueOf(rightMAPgains[i]));
//                        break;
//                    case 20:
//                        textView193.setText(String.valueOf(rightMAPTHR[i]));
//                        textView194.setText(String.valueOf(rightMAPMCL[i]));
//                        textView195.setText(String.valueOf(rightMAPgains[i]));
//                        break;
//                    case 21:
//                        textView201.setText(String.valueOf(rightMAPTHR[i]));
//                        textView202.setText(String.valueOf(rightMAPMCL[i]));
//                        textView203.setText(String.valueOf(rightMAPgains[i]));
//                        break;
//                    case 22:
//                        textView209.setText(String.valueOf(rightMAPTHR[i]));
//                        textView210.setText(String.valueOf(rightMAPMCL[i]));
//                        textView211.setText(String.valueOf(rightMAPgains[i]));
//                        break;
//                }
//            }

        }
    }

    void noRightMAPText() {
        textViewITR.setEnabled(false);
        textViewSFR.setEnabled(false);
        textViewNCR.setEnabled(false);
        textViewFTR.setEnabled(false);

        editTextSRR.setEnabled(false);
        editTextPWR.setEnabled(false);
        editTextSR.setEnabled(false);
        editTextGR.setEnabled(false);
        editTextQFR.setEnabled(false);
        editTextBLR.setEnabled(false);
        editTextSLR.setEnabled(false);

        spinnerSPSR.setEnabled(false);
        spinnerNMR.setEnabled(false);
        spinnerSMR.setEnabled(false);
        spinnerVR.setEnabled(false);
        spinnerSOR.setEnabled(false);
        spinnerWR.setEnabled(false);

        // Disable text (reduce alpha to 38%)
        TextView textViewRight = findViewById(R.id.textViewRight);
        if (textViewRight != null) {
            textViewRight.setTextColor(Color.argb(disabledAlpha, 0, 0, 0));
        }

        TextView textView27 = findViewById(R.id.textView27);
        if (textView27 != null) {
            textView27.setTextColor(Color.argb(disabledAlpha, 0, 0, 0));
        }

        TextView textView33 = findViewById(R.id.textView33);
        if (textView33 != null) {
            textView33.setTextColor(Color.argb(disabledAlpha, 0, 0, 0));
        }

        TextView textView34 = findViewById(R.id.textView34);
        if (textView34 != null) {
            textView34.setTextColor(Color.argb(disabledAlpha, 0, 0, 0));
        }

        TextView textView35 = findViewById(R.id.textView35);
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

    void putDouble(final SharedPreferences.Editor edit, final String key, final double value) {
        edit.putLong(key, Double.doubleToRawLongBits(value));
    }

    double getDouble(final SharedPreferences prefs, final String key) {
        return Double.longBitsToDouble(prefs.getLong(key, Double.doubleToLongBits((double) 0)));
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
        if (s.isEmpty())
            return false;
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
            leftMAPsoundProcessingStrategy = spinnerSPSL.getSelectedItem().toString();
            leftMAPnMaxima = Integer.parseInt(spinnerNML.getSelectedItem().toString());
            leftMAPstimulationMode = spinnerSML.getSelectedItem().toString();
            leftMAPvolume = Integer.parseInt(spinnerVL.getSelectedItem().toString());
            leftMAPstimulationOrder = spinnerSOL.getSelectedItem().toString();
            leftMAPwindow = spinnerWL.getSelectedItem().toString();

            // Check if user input is valid integer
            if (isInteger(editTextSRL.getText().toString()) && isIntInRange(Integer.valueOf(editTextSRL.getText().toString()), max_stimulation_rate, min_stimulation_rate)) {
                leftMAPstimulationRate = Integer.valueOf(editTextSRL.getText().toString());
            } else {
                userProblems = true;
            }
            if (isInteger(editTextPWL.getText().toString()) && isIntInRange(Integer.valueOf(editTextPWL.getText().toString()), max_pulsewidth, min_pulsewidth)) {
                leftMAPpulseWidth = Integer.valueOf(editTextPWL.getText().toString());
            } else {
                userProblems = true;
            }

            if (isDoubleInRange(Double.valueOf(editTextSL.getText().toString()), max_sensitivity, min_sensitivity)) {
                leftMAPsensitivity = Double.valueOf(editTextSL.getText().toString());
            } else {
                userProblems = true;
            }

            if (isDoubleInRange(Double.valueOf(editTextGL.getText().toString()), max_gain, min_gain)) {
                leftMAPgain = Double.valueOf(editTextGL.getText().toString());
            } else {
                userProblems = true;
            }

            if (isDoubleInRange(Double.valueOf(editTextQFL.getText().toString()), max_Qfactor, min_Qfactor)) {
                leftMAPQfactor = Double.valueOf(editTextQFL.getText().toString());
            } else {
                userProblems = true;
            }

            if (isDoubleInRange(Double.valueOf(editTextBLL.getText().toString()), max_baselevel, min_baselevel)) {
                leftMAPbaseLevel = Double.valueOf(editTextBLL.getText().toString());
            } else {
                userProblems = true;
            }

            if (isDoubleInRange(Double.valueOf(editTextSLL.getText().toString()), max_saturationlevel, min_saturationlevel)) {
                leftMAPsaturationLevel = Double.valueOf(editTextSLL.getText().toString());
            } else {
                userProblems = true;
            }

            // Check for valid parameters
            leftMAPcheckStimulationParameters();

            // Save values
            updateLeftMAPPreferences();

            // Display new parameter values
            editTextSRL.setText(String.valueOf(leftMAPstimulationRate), TextView.BufferType.EDITABLE);
            editTextPWL.setText(String.valueOf(leftMAPpulseWidth), TextView.BufferType.EDITABLE);
            editTextSL.setText(String.valueOf(leftMAPsensitivity), TextView.BufferType.EDITABLE);
            editTextGL.setText(String.valueOf(leftMAPgain), TextView.BufferType.EDITABLE);
            editTextQFL.setText(String.valueOf(leftMAPQfactor), TextView.BufferType.EDITABLE);
            editTextBLL.setText(String.valueOf(leftMAPbaseLevel), TextView.BufferType.EDITABLE);
            editTextSLL.setText(String.valueOf(leftMAPsaturationLevel), TextView.BufferType.EDITABLE);

            spinnerNML.setSelection(getSpinIndex(spinnerNML, String.valueOf(leftMAPnMaxima)));
            spinnerVL.setSelection(getSpinIndex(spinnerVL, String.valueOf(leftMAPvolume)));
            spinnerSOL.setSelection(getSpinIndex(spinnerSOL, leftMAPstimulationOrder));
            spinnerWL.setSelection(getSpinIndex(spinnerWL, leftMAPwindow));
        }

        if (rightExists) {
            // Reassign parameter values
            rightMAPsoundProcessingStrategy = spinnerSPSR.getSelectedItem().toString();
            rightMAPnMaxima = Integer.parseInt(spinnerNMR.getSelectedItem().toString());
            rightMAPstimulationMode = spinnerSMR.getSelectedItem().toString();
            rightMAPvolume = Integer.parseInt(spinnerVR.getSelectedItem().toString());
            rightMAPstimulationOrder = spinnerSOR.getSelectedItem().toString();
            rightMAPwindow = spinnerWR.getSelectedItem().toString();

            // Check if user input is valid integer
            if (isInteger(editTextSRR.getText().toString()) && isIntInRange(Integer.valueOf(editTextSRR.getText().toString()), max_stimulation_rate, min_stimulation_rate)) {
                rightMAPstimulationRate = Integer.valueOf(editTextSRR.getText().toString());
            } else {
                userProblems = true;
            }
            if (isInteger(editTextPWR.getText().toString()) && isIntInRange(Integer.valueOf(editTextPWR.getText().toString()), max_pulsewidth, min_pulsewidth)) {
                rightMAPpulseWidth = Integer.valueOf(editTextPWR.getText().toString());
            } else {
                userProblems = true;
            }

            if (isDoubleInRange(Double.valueOf(editTextSR.getText().toString()), max_sensitivity, min_sensitivity)) {
                rightMAPsensitivity = Double.valueOf(editTextSR.getText().toString());
            } else {
                userProblems = true;
            }

            if (isDoubleInRange(Double.valueOf(editTextGR.getText().toString()), max_gain, min_gain)) {
                rightMAPgain = Double.valueOf(editTextGR.getText().toString());
            } else {
                userProblems = true;
            }

            if (isDoubleInRange(Double.valueOf(editTextQFR.getText().toString()), max_Qfactor, min_Qfactor)) {
                rightMAPQfactor = Double.valueOf(editTextQFR.getText().toString());
            } else {
                userProblems = true;
            }

            if (isDoubleInRange(Double.valueOf(editTextBLR.getText().toString()), max_baselevel, min_baselevel)) {
                rightMAPbaseLevel = Double.valueOf(editTextBLR.getText().toString());
            } else {
                userProblems = true;
            }

            if (isDoubleInRange(Double.valueOf(editTextSLR.getText().toString()), max_saturationlevel, min_saturationlevel)) {
                rightMAPsaturationLevel = Double.valueOf(editTextSLR.getText().toString());
            } else {
                userProblems = true;
            }

            // Check for valid parameters
            rightMAPcheckStimulationParameters();

            // Save values
            updateRightMAPPreferences();

            // Display new parameter values
            editTextSRR.setText(String.valueOf(rightMAPstimulationRate), TextView.BufferType.EDITABLE);
            editTextPWR.setText(String.valueOf(rightMAPpulseWidth), TextView.BufferType.EDITABLE);
            editTextSR.setText(String.valueOf(rightMAPsensitivity), TextView.BufferType.EDITABLE);
            editTextGR.setText(String.valueOf(rightMAPgain), TextView.BufferType.EDITABLE);
            editTextQFR.setText(String.valueOf(rightMAPQfactor), TextView.BufferType.EDITABLE);
            editTextBLR.setText(String.valueOf(rightMAPbaseLevel), TextView.BufferType.EDITABLE);
            editTextSLR.setText(String.valueOf(rightMAPsaturationLevel), TextView.BufferType.EDITABLE);

            spinnerNMR.setSelection(getSpinIndex(spinnerNMR, String.valueOf(rightMAPnMaxima)));
            spinnerVR.setSelection(getSpinIndex(spinnerVR, String.valueOf(rightMAPvolume)));
            spinnerSOR.setSelection(getSpinIndex(spinnerSOR, rightMAPstimulationOrder));
            spinnerWR.setSelection(getSpinIndex(spinnerWR, rightMAPwindow));
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
            //Toast.makeText(getApplicationContext(), "MAP updated successfully.", Toast.LENGTH_SHORT).show(); // Set your toast message
            Snackbar.make(findViewById(R.id.rootSettings), "MAP updated successfully.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            //finish(); // close activity after hitting Update button if there are no errors
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