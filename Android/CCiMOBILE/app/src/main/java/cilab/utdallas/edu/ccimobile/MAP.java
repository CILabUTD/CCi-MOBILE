package cilab.utdallas.edu.ccimobile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

class MAP {
    private final int frameDuration = 8;             //8 ms frame
    private final int interPhaseGap = 8;            //Interphase Gap is 8us
    private final int durationSYNC = 6;             //Duration of Sync token in us
    private final int additionalGap = 1;            //additional gap to make interpulse duration 7 uS ref. Fig. 14 in CIC4 specification
    public final int BLOCK_SIZE = 128;             //BLK_SIZE
    boolean exists = false;
    boolean dataMissing = false;

    String
            ear,
            implantType,
            implantGeneration,
            soundProcessingStrategy,
            stimulationMode,
            stimulationOrder,
            frequencyTable,
            window;

    int samplingFrequency,
            volume,
            numberOfChannels,
            nMaxima,
            stimulationRate,
            pulseWidth,
            stimulationModeCode,
            nbands,
            pulsesPerFrame,
            pulsesPerFramePerChannel,
            nRFcycles;

    double sensitivity,
            gain,
            Qfactor,
            baseLevel,
            saturationLevel,
            interpulseDuration;

    int[] electrodes,
            THR, MCL;
    double[] gains,
            lowerCutOffFrequencies, higherCutOffFrequencies;

    void getLeftMapData(String FILE_NAME) {

        BufferedReader reader;
        try {

            String data = FileOperations.getInstance().readExternalFile(FILE_NAME);

            // convert String to InputStreamReader
            InputStream stream = new ByteArrayInputStream(data.getBytes());

            reader = new BufferedReader(new InputStreamReader(stream));
            StringBuilder buffer = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }

            String finalJson = buffer.toString();
            JSONObject parentObject = new JSONObject(finalJson);

            JSONArray generalArray = parentObject.getJSONArray("general");
            JSONObject generalObject = generalArray.getJSONObject(0);

/*              String subjectName = generalObject.getString("subjectName");
                String subjectID = generalObject.getString("subjectID");
                String mapTitle = generalObject.getString("mapTitle");
                int numberOfImplants = generalObject.getInt("numberOfImplants");
                String implantedEar = generalObject.getString("implantedEar");*/

            ear = generalObject.getString("ear");

            if (ear.equalsIgnoreCase("both") || ear.equalsIgnoreCase("left")) {

                JSONArray leftArray = parentObject.getJSONArray("left");
                JSONObject leftObject = leftArray.getJSONObject(0);

                ear = "Left";

                if (leftObject.has("Left.implantType") && leftObject.has("Left.samplingFrequency")
                        && leftObject.has("Left.numberOfChannels")
                        && leftObject.has("Left.soundProcessingStrategy")
                        && leftObject.has("Left.nMaxima") && leftObject.has("Left.stimulationMode")
                        && leftObject.has("Left.stimulationRate")
                        && leftObject.has("Left.pulseWidth") && leftObject.has("Left.sensitivity")
                        && leftObject.has("Left.gain") && leftObject.has("Left.volume")
                        && leftObject.has("Left.Qfactor") && leftObject.has("Left.baseLevel")
                        && leftObject.has("Left.saturationLevel")
                        && leftObject.has("Left.stimulationOrder")
                        && leftObject.has("Left.frequencyTable") && leftObject.has("Left.window")
                        && leftObject.has("Left.El_CF1_CF2_THR_MCL_Gain")) {

                    exists = true;

                    implantType = leftObject.getString("Left.implantType");
                    samplingFrequency = leftObject.getInt("Left.samplingFrequency");
                    numberOfChannels = leftObject.getInt("Left.numberOfChannels");
                    soundProcessingStrategy = leftObject.getString("Left.soundProcessingStrategy");
                    nMaxima = leftObject.getInt("Left.nMaxima");
                    stimulationMode = leftObject.getString("Left.stimulationMode");
                    stimulationRate = leftObject.getInt("Left.stimulationRate");
                    pulseWidth = leftObject.getInt("Left.pulseWidth");
                    sensitivity = leftObject.getDouble("Left.sensitivity");
                    gain = leftObject.getDouble("Left.gain");
                    volume = leftObject.getInt("Left.volume");
                    Qfactor = leftObject.getDouble("Left.Qfactor");
                    baseLevel = leftObject.getDouble("Left.baseLevel");
                    saturationLevel = leftObject.getDouble("Left.saturationLevel");
                    stimulationOrder = leftObject.getString("Left.stimulationOrder");
                    frequencyTable = leftObject.getString("Left.frequencyTable");
                    window = leftObject.getString("Left.window");

                    JSONArray leftElectrodeArray = leftObject.getJSONArray("Left.El_CF1_CF2_THR_MCL_Gain");
                    int leftInnerArrayLength = leftElectrodeArray.length();

                    electrodes = new int[leftInnerArrayLength];
                    THR = new int[leftInnerArrayLength];
                    MCL = new int[leftInnerArrayLength];

                    lowerCutOffFrequencies = new double[leftInnerArrayLength];
                    higherCutOffFrequencies = new double[leftInnerArrayLength];
                    gains = new double[leftInnerArrayLength];

                    for (int i = 0; i < leftInnerArrayLength; i++) {
                        JSONObject leftElectrodeArrayObj = leftElectrodeArray.getJSONObject(i);
                        electrodes[i] = leftElectrodeArrayObj.getInt("electrodes");
                        THR[i] = leftElectrodeArrayObj.getInt("THR");
                        MCL[i] = leftElectrodeArrayObj.getInt("MCL");
                        lowerCutOffFrequencies[i] = leftElectrodeArrayObj.getInt("lowerCutOffFrequencies");
                        higherCutOffFrequencies[i] = leftElectrodeArrayObj.getInt("higherCutOffFrequencies");
                        gains[i] = leftElectrodeArrayObj.getInt("gains");
                    }

                    nbands = electrodes.length;

                    checkStimulationParameters();


                } else {
                    dataMissing = true;
                }

            }

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

    }

    void getRightMapData(String FILE_NAME) {

        BufferedReader reader;
        try {
            String data = FileOperations.getInstance().readExternalFile(FILE_NAME);

            // convert String to InputStreamReader
            InputStream stream = new ByteArrayInputStream(data.getBytes());

            reader = new BufferedReader(new InputStreamReader(stream));
            StringBuilder buffer = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }

            String finalJson = buffer.toString();
            JSONObject parentObject = new JSONObject(finalJson);

            // get the first array (doesn't matter left/right)
            JSONArray generalArray = parentObject.getJSONArray("general");
            JSONObject generalObject = generalArray.getJSONObject(0);

/*              String subjectName = generalObject.getString("subjectName");
                String subjectID = generalObject.getString("subjectID");
                String mapTitle = generalObject.getString("mapTitle");
                int numberOfImplants = generalObject.getInt("numberOfImplants");
                String implantedEar = generalObject.getString("implantedEar");*/

            ear = generalObject.getString("ear");

            if (ear.equalsIgnoreCase("both") || ear.equalsIgnoreCase("right")) {

                JSONArray rightArray = parentObject.getJSONArray("right");
                JSONObject rightObject = rightArray.getJSONObject(0);

                ear = "Right";

                if (rightObject.has("Right.implantType")
                        && rightObject.has("Right.samplingFrequency")
                        && rightObject.has("Right.numberOfChannels")
                        && rightObject.has("Right.soundProcessingStrategy")
                        && rightObject.has("Right.nMaxima")
                        && rightObject.has("Right.stimulationMode")
                        && rightObject.has("Right.stimulationRate")
                        && rightObject.has("Right.pulseWidth")
                        && rightObject.has("Right.sensitivity")
                        && rightObject.has("Right.gain") && rightObject.has("Right.volume")
                        && rightObject.has("Right.Qfactor") && rightObject.has("Right.baseLevel")
                        && rightObject.has("Right.saturationLevel")
                        && rightObject.has("Right.stimulationOrder")
                        && rightObject.has("Right.frequencyTable")
                        && rightObject.has("Right.window")
                        && rightObject.has("Right.El_CF1_CF2_THR_MCL_Gain")) {

                    exists = true;

                    implantType = rightObject.getString("Right.implantType");
                    samplingFrequency = rightObject.getInt("Right.samplingFrequency");
                    numberOfChannels = rightObject.getInt("Right.numberOfChannels");
                    soundProcessingStrategy = rightObject.getString("Right.soundProcessingStrategy");
                    nMaxima = rightObject.getInt("Right.nMaxima");
                    stimulationMode = rightObject.getString("Right.stimulationMode");
                    stimulationRate = rightObject.getInt("Right.stimulationRate");
                    pulseWidth = rightObject.getInt("Right.pulseWidth");
                    sensitivity = rightObject.getDouble("Right.sensitivity");
                    gain = rightObject.getDouble("Right.gain");
                    volume = rightObject.getInt("Right.volume");
                    Qfactor = rightObject.getDouble("Right.Qfactor");
                    baseLevel = rightObject.getDouble("Right.baseLevel");
                    saturationLevel = rightObject.getDouble("Right.saturationLevel");
                    stimulationOrder = rightObject.getString("Right.stimulationOrder");
                    window = rightObject.getString("Right.window");
                    frequencyTable = rightObject.getString("Right.frequencyTable");

                    JSONArray rightElectrodeArray = rightObject.getJSONArray("Right.El_CF1_CF2_THR_MCL_Gain");
                    int rightInnerArrayLength = rightElectrodeArray.length();

                    electrodes = new int[rightInnerArrayLength];
                    THR = new int[rightInnerArrayLength];
                    MCL = new int[rightInnerArrayLength];

                    lowerCutOffFrequencies = new double[rightInnerArrayLength];
                    higherCutOffFrequencies = new double[rightInnerArrayLength];
                    gains = new double[rightInnerArrayLength];

                    for (int i = 0; i < rightInnerArrayLength; i++) {
                        JSONObject rightElectrodeArrayObj = rightElectrodeArray.getJSONObject(i);
                        electrodes[i] = rightElectrodeArrayObj.getInt("electrodes");
                        THR[i] = rightElectrodeArrayObj.getInt("THR");
                        MCL[i] = rightElectrodeArrayObj.getInt("MCL");
                        lowerCutOffFrequencies[i] = rightElectrodeArrayObj.getInt("lowerCutOffFrequencies");
                        higherCutOffFrequencies[i] = rightElectrodeArrayObj.getInt("higherCutOffFrequencies");
                        gains[i] = rightElectrodeArrayObj.getInt("gains");
                    }
                    nbands = electrodes.length;
                    checkStimulationParameters();
                } else {
                    dataMissing = true;
                }
            }
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }

    private void checkStimulationParameters() {
        checkImplantType();
        generateStimulationModeCode();
        checkPulseWidth();
        checkStimulationRate();
        checkTimingParameters();
        computePulseTiming();
        checkLevels();
        checkVolume();
    }

    private void checkVolume() {
        if (volume > 10) {
            volume = 10;
        }
    }

    private void checkLevels() {
    }

    private void checkImplantType() {
        switch (implantType) {
            case "CI24RE":
                implantGeneration = "CIC4";
                break;
            case "CI24R":
                implantGeneration = "CIC4";
                break;
            case "CI24M":
                implantGeneration = "CIC3";
                break;
            default:
                implantGeneration = "CIC4";
                break;
        }
    }

    private void generateStimulationModeCode() {
        if (implantGeneration.equals("CIC4")) {
            switch (stimulationMode) {
                case "MP1+2":
                    stimulationModeCode = 28;
                    break;
                //add other codes for other stimulation modes here
                default:
                    stimulationModeCode = 28;
                    break;
            }
        }
        if (implantGeneration.equals("CIC3")) {
            switch (stimulationMode) {
                case "MP1+2":
                    stimulationModeCode = 30;
                    break;
                //add other codes for other stimulation modes here
                default:
                    stimulationModeCode = 30;
                    break;
            }
        }
    }

    private void checkPulseWidth() {
        if (pulseWidth > 400) {
            pulseWidth = 400;
        } // Limit Pulse Width to 400us
    }

    private void checkStimulationRate() {
        int totalStimulationRate;
        double maxPulseWidth;

        totalStimulationRate = (stimulationRate * nMaxima);
        if (stimulationRate <= 14400) //maximum stimulation rate supported by Freedom implants is 14400 Hz
        {
            if (totalStimulationRate <= 14400) {
                maxPulseWidth = Math.floor(0.5 * ((1000000 / totalStimulationRate) - (interPhaseGap + 11))); //for MP stimulation modes in CIC3/CIC4
                if (pulseWidth > maxPulseWidth) {
                    pulseWidth = (int) maxPulseWidth; //this means it is the STD protocol, PW is reduced to maxPW
                }

            }
        }

        if (totalStimulationRate > 14400) {
            //High Rate Protocol is currently not supported
            while (totalStimulationRate > 14400) {
                stimulationRate--;
                totalStimulationRate = (stimulationRate * nMaxima);
            }
            //print error, exit
        }

    }


    private void checkTimingParameters() {

        pulsesPerFramePerChannel = (int) Math.round((8 * (double) stimulationRate) / 1000); //8ms //%floor(floor((8.0*rate_set)/1000));
        pulsesPerFrame = nMaxima * pulsesPerFramePerChannel;
        double totalStimulationRate = ((double)stimulationRate * (double)nMaxima);
        //Pulse-Width Centric
        // for pulse-width centric, max possible pulse-width is:
        //double maxPulseWidth = Math.floor(0.5 * ((8000 / (double) nMaxima) - (interPhaseGap + durationSYNC + additionalGap)));
        double maxPulseWidth = Math.floor(0.5 * ((1000000 / totalStimulationRate) - (interPhaseGap + 11))); //for MP stimulation modes in CIC3/CIC4
        //double maxPulseWidth = Math.floor(0.5 * ((1000000 / totalStimulationRate) - (interPhaseGap + 11+200))); //for BP and CG stimulation modes in CIC3/CIC4

        if (pulseWidth > maxPulseWidth) {
            pulseWidth = (int) maxPulseWidth;
        } // Limit Pulse Width to 400us

        double pd1 = 8000 / pulsesPerFrame;
        double pd2 = (pulseWidth * 2 + interPhaseGap + durationSYNC + additionalGap);
        if (pd1 < pd2) {
            while (pd1 < pd2) {
                stimulationRate--;
                pulsesPerFramePerChannel = (int) Math.round((8 * (double) stimulationRate) / 1000); //8ms //%floor(floor((8.0*rate_set)/1000));
                pulsesPerFrame = nMaxima * pulsesPerFramePerChannel;
                pd1 = 8000 / pulsesPerFrame;
            }
        }

        /* // Rate Centric
        if(T_pw > T_rate)
        {
            while(T_pw > T_rate)
            {
                pulseWidth--;
                T_pw = Math.floor(((pulseWidth * 2 + interPhaseGap + durationSYNC + additionalGap) * nMaxima));
            }

            pulseWidthStatus = 2;
        }
        */
    }

    private void computePulseTiming() {
        pulsesPerFramePerChannel = (int) Math.round((8 * (double) stimulationRate) / 1000); //8ms //%floor(floor((8.0*rate_set)/1000));
        pulsesPerFrame = nMaxima * pulsesPerFramePerChannel;
        stimulationRate = 125 * pulsesPerFramePerChannel; //125 frames of 8ms in 1s
        //blockShiftL = (BLOCK_SIZE / pulsesPerFramePerChannel); //ceil(fs / p.Left.analysis_rate);
        interpulseDuration = (double) frameDuration * 1000 / ((double) pulsesPerFrame) - 2 * (double) pulseWidth - (double) interPhaseGap - (double) durationSYNC - (double) additionalGap;
        nRFcycles = (int) Math.round((interpulseDuration / 0.1));
    }
}