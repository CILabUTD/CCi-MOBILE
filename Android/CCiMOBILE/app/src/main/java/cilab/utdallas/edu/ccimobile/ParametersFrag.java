package cilab.utdallas.edu.ccimobile;

import android.app.Activity;
import android.content.SharedPreferences;
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

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ParametersFrag extends Fragment {
    private TextView textViewITL, textViewITR, textViewSFL, textViewSFR, textViewNCL, textViewNCR, textViewFTL, textViewFTR;
    private EditText editTextSRL, editTextSRR, editTextPWL, editTextPWR, editTextSL, editTextSR, editTextGL, editTextGR, editTextQFL, editTextQFR, editTextBLL, editTextBLR, editTextSLL, editTextSLR;
    private Spinner spinnerSPSL, spinnerSPSR, spinnerNML, spinnerNMR, spinnerSML, spinnerSMR, spinnerVL, spinnerVR, spinnerSOL, spinnerSOR, spinnerWL, spinnerWR;

    private String leftMAPimplantType, leftMAPfrequencyTable, leftMAPsoundProcessingStrategy, leftMAPstimulationMode, leftMAPstimulationOrder, leftMAPwindow, rightMAPimplantType, rightMAPfrequencyTable, rightMAPsoundProcessingStrategy,
            rightMAPstimulationMode, rightMAPstimulationOrder, rightMAPwindow, leftImplantGeneration, rightImplantGeneration;

    private int leftMAPsamplingFrequency, leftMAPnumberOfChannels, leftMAPnMaxima, leftMAPstimulationRate, leftMAPpulseWidth, leftMAPvolume, leftMAPnbands, rightMAPsamplingFrequency, rightMAPnumberOfChannels, rightMAPnMaxima,
            rightMAPstimulationRate, rightMAPpulseWidth, rightMAPvolume, rightMAPnbands, leftStimulationModeCode, rightStimulationModeCode, leftPulsesPerFramePerChannel, rightPulsesPerFramePerChannel, leftPulsesPerFrame,
            rightPulsesPerFrame, leftnRFcycles, rightnRFcycles;

    private double leftMAPsensitivity, leftMAPgain, leftMAPQfactor, leftMAPbaseLevel, leftMAPsaturationLevel, rightMAPsensitivity, rightMAPgain, rightMAPQfactor, rightMAPbaseLevel, rightMAPsaturationLevel, leftInterpulseDuration,
            rightInterpulseDuration;

    private int[] leftMAPTHR, rightMAPTHR, leftMAPMCL, rightMAPMCL, leftMAPelectrodes, rightMAPelectrodes;
    private double[] leftMAPgains, rightMAPgains;
    private boolean leftExists, rightExists;

    private final int leftInterPhaseGap = 8;            //Interphase Gap is 8us
    private final int rightInterPhaseGap = 8;            //Interphase Gap is 8us

    private final int leftDurationSYNC = 6;             //Duration of Sycn Toekn in uS
    private final int rightDurationSYNC = 6;             //Duration of Sycn Toekn in uS

    private final int leftAdditionalGap = 1;            //additional gap to make interpulse duration 7 uS ref. Fig. 14 in CIC4 specification
    private final int rightAdditionalGap = 1;            //additional gap to make interpulse duration 7 uS ref. Fig. 14 in CIC4 specification

    private OnParametersSelectedListener mCallback;

    public void setOnParametersSelectedListener(Activity activity) {
        mCallback = (OnParametersSelectedListener) activity;
    }

    // Container Activity must implement this interface
    public interface OnParametersSelectedListener {
        public void onArticleSelected(int position);
    }


    public ParametersFrag() {
        // required empty constructor
    }

    /**
     * Returns a new instance of this fragment.
     */
    public static ParametersFrag newInstance(int sectionNumber) {
        ParametersFrag fragment = new ParametersFrag();
//        Bundle args = new Bundle();
//        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
//        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Updates GUI with MAP data
     * @param side left or right
     */
    private void UpdateMAPText(String side) {
        if (side.equals("left")) {
            ArrayAdapter<CharSequence> soundProcessAdapterLeft = ArrayAdapter.createFromResource(Objects.requireNonNull(getContext()),
                    R.array.strategySpinnerItems, android.R.layout.simple_spinner_item);
            soundProcessAdapterLeft.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            ArrayAdapter<CharSequence> stimOrderAdapterLeft = ArrayAdapter.createFromResource(getContext(),
                    R.array.stimOrderSpinnerItems, android.R.layout.simple_spinner_item);
            stimOrderAdapterLeft.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            ArrayAdapter<CharSequence> stimModeAdapterLeft = ArrayAdapter.createFromResource(getContext(),
                    R.array.stimModeSpinnerItems, android.R.layout.simple_spinner_item);
            stimModeAdapterLeft.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            ArrayAdapter<CharSequence> windowAdapterLeft = ArrayAdapter.createFromResource(getContext(),
                    R.array.windowSpinnerItems, android.R.layout.simple_spinner_item);
            windowAdapterLeft.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            spinnerSPSL.setAdapter(soundProcessAdapterLeft);
            spinnerSOL.setAdapter(stimOrderAdapterLeft);
            spinnerSML.setAdapter(stimModeAdapterLeft);
            spinnerWL.setAdapter(windowAdapterLeft);

            ArrayAdapter<CharSequence> volumeAdapterLeft = ArrayAdapter.createFromResource(getContext(),
                    R.array.volumeSpinnerItems, android.R.layout.simple_spinner_item);
            volumeAdapterLeft.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerVL.setAdapter(volumeAdapterLeft);

            String[] nMaximaItemsLeft = new String[leftMAPnumberOfChannels];
            for (int i = 0; i < leftMAPnumberOfChannels; i++) {
                nMaximaItemsLeft[i] = Integer.toString(i+1);
            }

            ArrayAdapter<String> nMaximaAdapterLeft = new ArrayAdapter<>(getContext(),
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
        }

        else if (side.equals("right")) {
            ArrayAdapter<CharSequence> soundProcessAdapterRight = ArrayAdapter.createFromResource(Objects.requireNonNull(getContext()),
                    R.array.strategySpinnerItems, android.R.layout.simple_spinner_item);
            soundProcessAdapterRight.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            ArrayAdapter<CharSequence> stimOrderAdapterRight = ArrayAdapter.createFromResource(getContext(),
                    R.array.stimOrderSpinnerItems, android.R.layout.simple_spinner_item);
            stimOrderAdapterRight.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            ArrayAdapter<CharSequence> stimModeAdapterRight = ArrayAdapter.createFromResource(getContext(),
                    R.array.stimModeSpinnerItems, android.R.layout.simple_spinner_item);
            stimModeAdapterRight.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            ArrayAdapter<CharSequence> windowAdapterRight = ArrayAdapter.createFromResource(getContext(),
                    R.array.windowSpinnerItems, android.R.layout.simple_spinner_item);
            windowAdapterRight.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            spinnerSPSR.setAdapter(soundProcessAdapterRight);
            spinnerSOR.setAdapter(stimOrderAdapterRight);
            spinnerSMR.setAdapter(stimModeAdapterRight);
            spinnerWR.setAdapter(windowAdapterRight);

            ArrayAdapter<CharSequence> volumeAdapterRight = ArrayAdapter.createFromResource(getContext(),
                    R.array.volumeSpinnerItems, android.R.layout.simple_spinner_item);
            volumeAdapterRight.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            spinnerVR.setAdapter(volumeAdapterRight);

            String[] nMaximaItemsRight = new String[rightMAPnumberOfChannels];
            for (int i = 0; i < rightMAPnumberOfChannels; i++) {
                nMaximaItemsRight[i] = Integer.toString(i+1);
            }

            ArrayAdapter<String> nMaximaAdapterRight = new ArrayAdapter<>(getContext(),
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
        }
    }

    /**
     * Disables left or right text
     * @param side left or right
     */
    private void disableMAPtext(String side) {
        if (side.equals("left")) {
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
        }

        else if (side.equals("right")) {
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
        }

    }

    /**
     * Gets data from the MAP using SharedPreferences
     */
    private void getMAPFromPreferences() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());

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

            UpdateMAPText("left");

        } else {
            disableMAPtext("left");
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

            UpdateMAPText("right");
        } else {
            disableMAPtext("right");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View settingsView = inflater.inflate(R.layout.settings, container, false);

        // Retrieve views
        textViewITL = settingsView.findViewById(R.id.textViewITL);
        textViewSFL = settingsView.findViewById(R.id.textViewSFL);
        textViewNCL = settingsView.findViewById(R.id.textViewNCL);
        textViewFTL = settingsView.findViewById(R.id.textViewFTL);
        textViewITR = settingsView.findViewById(R.id.textViewITR);
        textViewSFR = settingsView.findViewById(R.id.textViewSFR);
        textViewNCR = settingsView.findViewById(R.id.textViewNCR);
        textViewFTR = settingsView.findViewById(R.id.textViewFTR);

        editTextSRL = settingsView.findViewById(R.id.editTextSRL);
        editTextPWL = settingsView.findViewById(R.id.editTextPWL);
        editTextSL = settingsView.findViewById(R.id.editTextSL);
        editTextGL = settingsView.findViewById(R.id.editTextGL);
        editTextQFL = settingsView.findViewById(R.id.editTextQFL);
        editTextBLL = settingsView.findViewById(R.id.editTextBLL);
        editTextSLL = settingsView.findViewById(R.id.editTextSLL);
        editTextSRR = settingsView.findViewById(R.id.editTextSRR);
        editTextPWR = settingsView.findViewById(R.id.editTextPWR);
        editTextSR = settingsView.findViewById(R.id.editTextSR);
        editTextGR = settingsView.findViewById(R.id.editTextGR);
        editTextQFR = settingsView.findViewById(R.id.editTextQFR);
        editTextBLR = settingsView.findViewById(R.id.editTextBLR);
        editTextSLR = settingsView.findViewById(R.id.editTextSLR);

        spinnerVL = settingsView.findViewById(R.id.spinnerVL);
        spinnerVR = settingsView.findViewById(R.id.spinnerVR);
        spinnerSPSL = settingsView.findViewById(R.id.spinnerSPSL);
        spinnerSPSR = settingsView.findViewById(R.id.spinnerSPSR);
        spinnerSOL = settingsView.findViewById(R.id.spinnerSOL);
        spinnerSOR = settingsView.findViewById(R.id.spinnerSOR);
        spinnerSML = settingsView.findViewById(R.id.spinnerSML);
        spinnerSMR = settingsView.findViewById(R.id.spinnerSMR);
        spinnerWL = settingsView.findViewById(R.id.spinnerWL);
        spinnerWR = settingsView.findViewById(R.id.spinnerWR);
        spinnerNML = settingsView.findViewById(R.id.spinnerNML);
        spinnerNMR = settingsView.findViewById(R.id.spinnerNMR);

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

        return settingsView;
    }

    @Override
    public void onStart() {
        super.onStart();
        getMAPFromPreferences();
    }

    /**
     * Updates the MAP when the update button is hit
     * @return true if adjustments made
     */
    boolean updateMAPButton() {
        boolean userProblems = false;
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
            updateMAPPreferences("left");

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
            updateMAPPreferences("right");

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

        return userProblems;
    }

    /**
     * Updates the MAP
     * @param side left or right
     */
    private void updateMAPPreferences(String side) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = preferences.edit();

        if (side.equals("left")) {
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
        }

        else if (side.equals("right")) {
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
        }
        editor.apply();
    }

    private void leftMAPcheckStimulationParameters() {
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

    private void rightMAPcheckStimulationParameters() {
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

    private void checkImplantTypeLeft() {
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

    private void checkImplantTypeRight() {
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

    private void generateStimulationModeCodeLeft() {
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

    String getLeftImplantGeneration() {
        return leftImplantGeneration;
    }

    String getRightImplantGeneration() {
        return rightImplantGeneration;
    }

    int getLeftStimulationModeCode() {
        return leftStimulationModeCode;
    }

    int getRightStimulationModeCode() {
        return rightStimulationModeCode;
    }

    int getLeftPulsesPerFramePerChannel() {
        return leftPulsesPerFramePerChannel;
    }

    int getRightPulsesPerFramePerChannel() {
        return rightPulsesPerFramePerChannel;
    }

    int getLeftPulsesPerFrame() {
        return leftPulsesPerFrame;
    }

    int getRightPulsesPerFrame() {
        return rightPulsesPerFrame;
    }

    double getLeftInterpulseDuration() {
        return leftInterpulseDuration;
    }

    double getRightInterpulseDuration() {
        return rightInterpulseDuration;
    }

    int getLeftnRFcycles() {
        return leftnRFcycles;
    }

    int getRightnRFcycles() {
        return rightnRFcycles;
    }

    private void generateStimulationModeCodeRight() {
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

    private void checkPulseWidthLeft() {
        if (leftMAPpulseWidth > 400) {
            leftMAPpulseWidth = 400;
        } // Limit Pulse Width to 400us
    }

    private void checkPulseWidthRight() {
        if (rightMAPpulseWidth > 400) {
            rightMAPpulseWidth = 400;
        } // Limit Pulse Width to 400us
    }

    private void checkStimulationRateLeft() {
        int totalStimulationRate;
        double maxPulseWidth;

        totalStimulationRate = (leftMAPstimulationRate * leftMAPnMaxima);
        if (leftMAPstimulationRate <= 14400) //maximum stimulation rate supported by Freedom implants is 14400 Hz
        {
            if (totalStimulationRate <= 14400) {
                maxPulseWidth = Math.floor(0.5 * (((double) 1000000 / totalStimulationRate) - (leftInterPhaseGap + 11))); //for MP stimulation modes in CIC3/CIC4
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

    private void checkStimulationRateRight() {
        int totalStimulationRate;
        double maxPulseWidth;

        totalStimulationRate = (rightMAPstimulationRate * rightMAPnMaxima);
        if (rightMAPstimulationRate <= 14400) //maximum stimulation rate supported by Freedom implants is 14400 Hz
        {
            if (totalStimulationRate <= 14400) {
                maxPulseWidth = Math.floor(0.5 * (((double) 1000000 / totalStimulationRate) - (rightInterPhaseGap + 11))); //for MP stimulation modes in CIC3/CIC4
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

    private void checkTimingParametersLeft() {
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

        double pd1 = (double) 8000 / leftPulsesPerFrame;
        double pd2 = (leftMAPpulseWidth * 2 + leftInterPhaseGap + leftDurationSYNC + leftAdditionalGap);
        if (pd1 < pd2) {
            while (pd1 < pd2) {
                leftMAPstimulationRate--;
                leftPulsesPerFramePerChannel = (int) Math.round((8 * (double) leftMAPstimulationRate) / 1000); //8ms //%floor(floor((8.0*rate_set)/1000));
                leftPulsesPerFrame = leftMAPnMaxima * leftPulsesPerFramePerChannel;
                pd1 = (double) 8000 / leftPulsesPerFrame;
            }
        }
    }

    private void checkTimingParametersRight() {
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

        double pd1 = (double) 8000 / rightPulsesPerFrame;
        double pd2 = (rightMAPpulseWidth * 2 + rightInterPhaseGap + rightDurationSYNC + rightAdditionalGap);
        if (pd1 < pd2) {
            while (pd1 < pd2) {
                rightMAPstimulationRate--;
                rightPulsesPerFramePerChannel = (int) Math.round((8 * (double) rightMAPstimulationRate) / 1000); //8ms //%floor(floor((8.0*rate_set)/1000));
                rightPulsesPerFrame = rightMAPnMaxima * rightPulsesPerFramePerChannel;
                pd1 = (double) 8000 / rightPulsesPerFrame;
            }
        }
    }

    private void computePulseTimingLeft() {
        leftPulsesPerFramePerChannel = (int) Math.round((8 * (double) leftMAPstimulationRate) / 1000); //8ms //%floor(floor((8.0*rate_set)/1000));
        leftPulsesPerFrame = leftMAPnMaxima * leftPulsesPerFramePerChannel;
        leftMAPstimulationRate = 125 * leftPulsesPerFramePerChannel; //125 frames of 8ms in 1s
        //blockShiftL = (BLOCK_SIZE / leftPulsesPerFramePerChannel); //ceil(fs / p.Left.analysis_rate);
        int leftFrameDuration = 8;
        leftInterpulseDuration = (double) leftFrameDuration * 1000 / ((double) leftPulsesPerFrame) - 2 * (double) leftMAPpulseWidth - (double) leftInterPhaseGap - (double) leftDurationSYNC - (double) leftAdditionalGap;
        leftnRFcycles = (int) Math.round((leftInterpulseDuration / 0.1));
    }

    private void computePulseTimingRight() {
        rightPulsesPerFramePerChannel = (int) Math.round((8 * (double) rightMAPstimulationRate) / 1000); //8ms //%floor(floor((8.0*rate_set)/1000));
        rightPulsesPerFrame = rightMAPnMaxima * rightPulsesPerFramePerChannel;
        rightMAPstimulationRate = 125 * rightPulsesPerFramePerChannel; //125 frames of 8ms in 1s
        //blockShiftL = (BLOCK_SIZE / rightPulsesPerFramePerChannel); //ceil(fs / p.Right.analysis_rate);
        int rightFrameDuration = 8;
        rightInterpulseDuration = (double) rightFrameDuration * 1000 / ((double) rightPulsesPerFrame) - 2 * (double) rightMAPpulseWidth - (double) rightInterPhaseGap - (double) rightDurationSYNC - (double) rightAdditionalGap;
        rightnRFcycles = (int) Math.round((rightInterpulseDuration / 0.1));
    }

    private void checkLevelsLeft() {
        // do something
    }

    private void checkLevelsRight() {
        // do something
    }

    private void checkVolumeLeft() {
        if (leftMAPvolume > 10) {
            leftMAPvolume = 10;
        }
    }

    private void checkVolumeRight() {
        if (rightMAPvolume > 10) {
            rightMAPvolume = 10;
        }
    }

    private double getDouble(final SharedPreferences prefs, final String key) {
        return Double.longBitsToDouble(prefs.getLong(key, Double.doubleToLongBits((double) 0)));
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

    private boolean isInteger(String s) {
        boolean result = isInteger(s, 10);
        if (!result) {
            Toast.makeText(getContext(), "Please enter an integer.", Toast.LENGTH_LONG).show(); // Set your toast message
        }
        return result;
    }

    private boolean isInteger(String s, int radix) {
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

    private boolean isIntInRange(int number, int upperbound, int lowerbound) {
        if ((number >= lowerbound) && (number <= upperbound)) {
            return true;
        } else {
            Toast.makeText(getContext(), "Value is out bounds.", Toast.LENGTH_LONG).show(); // Set your toast message
            return false;
        }
    }

    private boolean isDoubleInRange(double number, int upperbound, int lowerbound) {
        if ((number >= lowerbound) && (number <= upperbound)) {
            return true;
        } else {
            Toast.makeText(getContext(), "Value is out bounds.", Toast.LENGTH_LONG).show(); // Set your toast message
            return false;
        }
    }

    private void putDouble(final SharedPreferences.Editor edit, final String key, final double value) {
        edit.putLong(key, Double.doubleToRawLongBits(value));
    }
}
