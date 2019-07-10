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

    /**
     * Updates the MAP data
     * @param FILE_NAME MAP filename
     * @param side left or right
     */
    void getMAPData(String FILE_NAME, String side) {

        if (side.equalsIgnoreCase("left"))
            side = "Left";
        else if (side.equalsIgnoreCase("right"))
            side = "Right";

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

            JSONArray generalArray = parentObject.getJSONArray("General");
            JSONObject generalObject = generalArray.getJSONObject(0);

/*              String subjectName = generalObject.getString("subjectName");
                String subjectID = generalObject.getString("subjectID");
                String mapTitle = generalObject.getString("mapTitle");
                int numberOfImplants = generalObject.getInt("numberOfImplants");
                String implantedEar = generalObject.getString("implantedEar");*/

            ear = generalObject.getString("ear");

            if (ear.equalsIgnoreCase("both") || ear.equalsIgnoreCase(side)) {

                JSONArray sideArray = parentObject.getJSONArray(side);
                JSONObject sideObject = sideArray.getJSONObject(0);

                ear = side;

                if (sideObject.has(side + ".implantType")
                        && sideObject.has(side + ".samplingFrequency")
                        && sideObject.has(side + ".numberOfChannels")
                        && sideObject.has(side + ".soundProcessingStrategy")
                        && sideObject.has(side + ".nMaxima")
                        && sideObject.has(side + ".stimulationMode")
                        && sideObject.has(side + ".stimulationRate")
                        && sideObject.has(side + ".pulseWidth")
                        && sideObject.has(side + ".sensitivity")
                        && sideObject.has(side + ".gain")
                        && sideObject.has(side + ".volume")
                        && sideObject.has(side + ".Qfactor")
                        && sideObject.has(side + ".baseLevel")
                        && sideObject.has(side + ".saturationLevel")
                        && sideObject.has(side + ".stimulationOrder")
                        && sideObject.has(side + ".frequencyTable")
                        && sideObject.has(side + ".window")
                        && sideObject.has(side + ".El_CF1_CF2_THR_MCL_Gain")) {

                    exists = true;

                    implantType = sideObject.getString(side + ".implantType");
                    samplingFrequency = sideObject.getInt(side + ".samplingFrequency");
                    numberOfChannels = sideObject.getInt(side + ".numberOfChannels");
                    soundProcessingStrategy = sideObject.getString(side + ".soundProcessingStrategy");
                    nMaxima = sideObject.getInt(side + ".nMaxima");
                    stimulationMode = sideObject.getString(side + ".stimulationMode");
                    stimulationRate = sideObject.getInt(side + ".stimulationRate");
                    pulseWidth = sideObject.getInt(side + ".pulseWidth");
                    sensitivity = sideObject.getDouble(side + ".sensitivity");
                    gain = sideObject.getDouble(side + ".gain");
                    volume = sideObject.getInt(side + ".volume");
                    Qfactor = sideObject.getDouble(side + ".Qfactor");
                    baseLevel = sideObject.getDouble(side + ".baseLevel");
                    saturationLevel = sideObject.getDouble(side + ".saturationLevel");
                    stimulationOrder = sideObject.getString(side + ".stimulationOrder");
                    frequencyTable = sideObject.getString(side + ".frequencyTable");
                    window = sideObject.getString(side + ".window");

                    JSONArray sideElectrodeArray = sideObject.getJSONArray(side + ".El_CF1_CF2_THR_MCL_Gain");
                    int sideInnerArrayLength = sideElectrodeArray.length();

                    electrodes = new int[sideInnerArrayLength];
                    THR = new int[sideInnerArrayLength];
                    MCL = new int[sideInnerArrayLength];

                    lowerCutOffFrequencies = new double[sideInnerArrayLength];
                    higherCutOffFrequencies = new double[sideInnerArrayLength];
                    gains = new double[sideInnerArrayLength];

                    for (int i = 0; i < sideInnerArrayLength; i++) {
                        JSONObject sideElectrodeArrayObj = sideElectrodeArray.getJSONObject(i);
                        electrodes[i] = sideElectrodeArrayObj.getInt("electrodes");
                        THR[i] = sideElectrodeArrayObj.getInt("THR");
                        MCL[i] = sideElectrodeArrayObj.getInt("MCL");
                        lowerCutOffFrequencies[i] = sideElectrodeArrayObj.getInt("lowerCutOffFrequencies");
                        higherCutOffFrequencies[i] = sideElectrodeArrayObj.getInt("higherCutOffFrequencies");
                        gains[i] = sideElectrodeArrayObj.getInt("gains");
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
            if ("MP1+2".equals(stimulationMode)) {
                stimulationModeCode = 28;
                //add other codes for other stimulation modes here
            } else {
                stimulationModeCode = 28;
            }
        }
        if (implantGeneration.equals("CIC3")) {
            if ("MP1+2".equals(stimulationMode)) {
                stimulationModeCode = 30;
                //add other codes for other stimulation modes here
            } else {
                stimulationModeCode = 30;
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