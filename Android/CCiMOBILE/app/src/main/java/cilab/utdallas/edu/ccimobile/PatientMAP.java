package cilab.utdallas.edu.ccimobile;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static cilab.utdallas.edu.ccimobile.SharedHelper.readExternalFile;

public class PatientMAP implements Parcelable {
    private final int frameDuration = 8; // 8 ms frame
    private final int interPhaseGap = 8; // inter-phase gap is 8us
    private final int durationSYNC = 6; // duration of Sync token in us
    private final int additionalGap = 1; // additional gap to make inter-pulse duration 7 uS ref. Fig. 14 in CIC4 specification
    private final int BLOCK_SIZE = 128; // BLK_SIZE

    private boolean exists;
    private boolean dataMissing;
    private String ear, implantType, implantGeneration, soundProcessingStrategy, stimulationMode, stimulationOrder, frequencyTable, window;
    private int samplingFrequency, volume, numberOfChannels, nMaxima, stimulationRate, pulseWidth, stimulationModeCode, nbands, pulsesPerFrame, pulsesPerFramePerChannel, nRFcycles;
    private double sensitivity, gain, Qfactor, baseLevel, saturationLevel, interpulseDuration;
    private int[] electrodes, THR, MCL;
    private double[] gains, lowerCutOffFrequencies, higherCutOffFrequencies;

    /**
     * Default constructor
     */
    public PatientMAP() {

    }

    /**
     * Parcelable constructor
     * @param in
     */
    public PatientMAP(Parcel in) {
        exists = in.readByte() != 0;
        dataMissing = in.readByte() != 0;
        ear = in.readString();
        implantType = in.readString();
        implantGeneration = in.readString();
        soundProcessingStrategy = in.readString();
        stimulationMode = in.readString();
        stimulationOrder = in.readString();
        frequencyTable = in.readString();
        window = in.readString();
        samplingFrequency = in.readInt();
        volume = in.readInt();
        numberOfChannels = in.readInt();
        nMaxima = in.readInt();
        stimulationRate = in.readInt();
        pulseWidth = in.readInt();
        stimulationModeCode = in.readInt();
        nbands = in.readInt();
        pulsesPerFrame = in.readInt();
        pulsesPerFramePerChannel = in.readInt();
        nRFcycles = in.readInt();
        sensitivity = in.readDouble();
        gain = in.readDouble();
        Qfactor = in.readDouble();
        baseLevel = in.readDouble();
        saturationLevel = in.readDouble();
        interpulseDuration = in.readDouble();
        electrodes = in.createIntArray();
        THR = in.createIntArray();
        MCL = in.createIntArray();
        gains = in.createDoubleArray();
        lowerCutOffFrequencies = in.createDoubleArray();
        higherCutOffFrequencies = in.createDoubleArray();
    }

    public static final Creator<PatientMAP> CREATOR = new Creator<PatientMAP>() {
        @Override
        public PatientMAP createFromParcel(Parcel in) {
            return new PatientMAP(in);
        }

        @Override
        public PatientMAP[] newArray(int size) {
            return new PatientMAP[size];
        }
    };

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

            String data = readExternalFile(FILE_NAME);

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
                        && sideObject.has(side + ".frequencyTable")
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

    public void updateChangedParameters(PatientMAP pmap) {
        // insert sound processing strategy
        this.setnMaxima(pmap.getnMaxima());
        // insert stimulation mode
        this.setStimulationModeCode(pmap.getStimulationModeCode());
        this.setStimulationRate(pmap.getStimulationRate());
        this.setPulseWidth(pmap.getPulseWidth());
        this.setSensitivity(pmap.getSensitivity());
        this.setGain(pmap.getGain());
        this.setVolume(pmap.getVolume());
        this.setQfactor(pmap.getQfactor());
        this.setBaseLevel(pmap.getBaseLevel());
        this.setSaturationLevel(pmap.getSaturationLevel());
        this.setStimulationOrder(pmap.getStimulationOrder());
        this.setWindow(pmap.getWindow());

        this.checkLevels(); // checks sensitivity and gain
        // put in full check?

        // other
        this.setImplantGeneration(pmap.getImplantGeneration());
        this.setPulsesPerFramePerChannel(pmap.getPulsesPerFramePerChannel());
        this.setPulsesPerFrame(pmap.getPulsesPerFrame());
        this.setInterpulseDuration(pmap.getInterpulseDuration());
        this.setnRFcycles(pmap.getnRFcycles());
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
        if (sensitivity > 10) {
            sensitivity = 10;
        } else if (sensitivity < 0) {
            sensitivity = 0;
        }

        if (gain > 50) {
            gain = 50;
        } else if (gain < 0) {
            gain = 0;
        }
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
                maxPulseWidth = Math.floor(0.5 * ((1000000 / (double) totalStimulationRate) - (interPhaseGap + 11))); //for MP stimulation modes in CIC3/CIC4
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

        double pd1 = 8000 / (double) pulsesPerFrame;
        double pd2 = (pulseWidth * 2 + interPhaseGap + durationSYNC + additionalGap);
        if (pd1 < pd2) {
            while (pd1 < pd2) {
                stimulationRate--;
                pulsesPerFramePerChannel = (int) Math.round((8 * (double) stimulationRate) / 1000); //8ms //%floor(floor((8.0*rate_set)/1000));
                pulsesPerFrame = nMaxima * pulsesPerFramePerChannel;
                pd1 = 8000 / (double) pulsesPerFrame;
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
        // blockShiftL = (BLOCK_SIZE / pulsesPerFramePerChannel);
        // ceil(fs / p.Left.analysis_rate);
        interpulseDuration = (double) frameDuration * 1000 / ((double) pulsesPerFrame) - 2 * (double) pulseWidth - (double) interPhaseGap - (double) durationSYNC - (double) additionalGap;
        nRFcycles = (int) Math.round((interpulseDuration / 0.1));
    }

    public int getFrameDuration() {
        return frameDuration;
    }

    public int getInterPhaseGap() {
        return interPhaseGap;
    }

    public int getDurationSYNC() {
        return durationSYNC;
    }

    public int getAdditionalGap() {
        return additionalGap;
    }

    public int getBLOCK_SIZE() {
        return BLOCK_SIZE;
    }

    public boolean isExists() {
        return exists;
    }

    public void setExists(boolean exists) {
        this.exists = exists;
    }

    public boolean isDataMissing() {
        return dataMissing;
    }

    public void setDataMissing(boolean dataMissing) {
        this.dataMissing = dataMissing;
    }

    public String getEar() {
        return ear;
    }

    public void setEar(String ear) {
        this.ear = ear;
    }

    public String getImplantType() {
        return implantType;
    }

    public void setImplantType(String implantType) {
        this.implantType = implantType;
    }

    public String getImplantGeneration() {
        return implantGeneration;
    }

    public void setImplantGeneration(String implantGeneration) {
        this.implantGeneration = implantGeneration;
    }

    public String getSoundProcessingStrategy() {
        return soundProcessingStrategy;
    }

    public void setSoundProcessingStrategy(String soundProcessingStrategy) {
        this.soundProcessingStrategy = soundProcessingStrategy;
    }

    public String getStimulationMode() {
        return stimulationMode;
    }

    public void setStimulationMode(String stimulationMode) {
        this.stimulationMode = stimulationMode;
    }

    public String getStimulationOrder() {
        return stimulationOrder;
    }

    public void setStimulationOrder(String stimulationOrder) {
        this.stimulationOrder = stimulationOrder;
    }

    public String getFrequencyTable() {
        return frequencyTable;
    }

    public void setFrequencyTable(String frequencyTable) {
        this.frequencyTable = frequencyTable;
    }

    public String getWindow() {
        return window;
    }

    public void setWindow(String window) {
        this.window = window;
    }

    public int getSamplingFrequency() {
        return samplingFrequency;
    }

    public void setSamplingFrequency(int samplingFrequency) {
        this.samplingFrequency = samplingFrequency;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public int getNumberOfChannels() {
        return numberOfChannels;
    }

    public void setNumberOfChannels(int numberOfChannels) {
        this.numberOfChannels = numberOfChannels;
    }

    public int getnMaxima() {
        return nMaxima;
    }

    public void setnMaxima(int nMaxima) {
        this.nMaxima = nMaxima;
    }

    public int getStimulationRate() {
        return stimulationRate;
    }

    public void setStimulationRate(int stimulationRate) {
        this.stimulationRate = stimulationRate;
    }

    public int getPulseWidth() {
        return pulseWidth;
    }

    public void setPulseWidth(int pulseWidth) {
        this.pulseWidth = pulseWidth;
    }

    public int getStimulationModeCode() {
        return stimulationModeCode;
    }

    public void setStimulationModeCode(int stimulationModeCode) {
        this.stimulationModeCode = stimulationModeCode;
    }

    public int getNbands() {
        return nbands;
    }

    public void setNbands(int nbands) {
        this.nbands = nbands;
    }

    public int getPulsesPerFrame() {
        return pulsesPerFrame;
    }

    public void setPulsesPerFrame(int pulsesPerFrame) {
        this.pulsesPerFrame = pulsesPerFrame;
    }

    public int getPulsesPerFramePerChannel() {
        return pulsesPerFramePerChannel;
    }

    public void setPulsesPerFramePerChannel(int pulsesPerFramePerChannel) {
        this.pulsesPerFramePerChannel = pulsesPerFramePerChannel;
    }

    public int getnRFcycles() {
        return nRFcycles;
    }

    public void setnRFcycles(int nRFcycles) {
        this.nRFcycles = nRFcycles;
    }

    public double getSensitivity() {
        return sensitivity;
    }

    public void setSensitivity(double sensitivity) {
        this.sensitivity = sensitivity;
    }

    public double getGain() {
        return gain;
    }

    public void setGain(double gain) {
        this.gain = gain;
    }

    public double getQfactor() {
        return Qfactor;
    }

    public void setQfactor(double qfactor) {
        Qfactor = qfactor;
    }

    public double getBaseLevel() {
        return baseLevel;
    }

    public void setBaseLevel(double baseLevel) {
        this.baseLevel = baseLevel;
    }

    public double getSaturationLevel() {
        return saturationLevel;
    }

    public void setSaturationLevel(double saturationLevel) {
        this.saturationLevel = saturationLevel;
    }

    public double getInterpulseDuration() {
        return interpulseDuration;
    }

    public void setInterpulseDuration(double interpulseDuration) {
        this.interpulseDuration = interpulseDuration;
    }

    public int[] getElectrodes() {
        return electrodes;
    }

    public int getElectrodes(int index) {
        return electrodes[index];
    }

    public void setElectrodes(int[] electrodes) {
        this.electrodes = electrodes;
    }

    public int[] getTHR() {
        return THR;
    }

    public int getTHR(int index) {
        return THR[index];
    }

    public void setTHR(int[] THR) {
        this.THR = THR;
    }

    public int[] getMCL() {
        return MCL;
    }

    public int getMCL(int index) {
        return MCL[index];
    }

    public void setMCL(int[] MCL) {
        this.MCL = MCL;
    }

    public double[] getGains() {
        return gains;
    }

    public double getGains(int index) {
        return gains[index];
    }

    public void setGains(double[] gains) {
        this.gains = gains;
    }

    public double[] getLowerCutOffFrequencies() {
        return lowerCutOffFrequencies;
    }

    public void setLowerCutOffFrequencies(double[] lowerCutOffFrequencies) {
        this.lowerCutOffFrequencies = lowerCutOffFrequencies;
    }

    public double[] getHigherCutOffFrequencies() {
        return higherCutOffFrequencies;
    }

    public void setHigherCutOffFrequencies(double[] higherCutOffFrequencies) {
        this.higherCutOffFrequencies = higherCutOffFrequencies;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (exists ? 1 : 0));
        dest.writeByte((byte) (dataMissing ? 1 : 0));
        dest.writeString(ear);
        dest.writeString(implantType);
        dest.writeString(implantGeneration);
        dest.writeString(soundProcessingStrategy);
        dest.writeString(stimulationMode);
        dest.writeString(stimulationOrder);
        dest.writeString(frequencyTable);
        dest.writeString(window);
        dest.writeInt(samplingFrequency);
        dest.writeInt(volume);
        dest.writeInt(numberOfChannels);
        dest.writeInt(nMaxima);
        dest.writeInt(stimulationRate);
        dest.writeInt(pulseWidth);
        dest.writeInt(stimulationModeCode);
        dest.writeInt(nbands);
        dest.writeInt(pulsesPerFrame);
        dest.writeInt(pulsesPerFramePerChannel);
        dest.writeInt(nRFcycles);
        dest.writeDouble(sensitivity);
        dest.writeDouble(gain);
        dest.writeDouble(Qfactor);
        dest.writeDouble(baseLevel);
        dest.writeDouble(saturationLevel);
        dest.writeDouble(interpulseDuration);
        dest.writeIntArray(electrodes);
        dest.writeIntArray(THR);
        dest.writeIntArray(MCL);
        dest.writeDoubleArray(gains);
        dest.writeDoubleArray(lowerCutOffFrequencies);
        dest.writeDoubleArray(higherCutOffFrequencies);
    }
}
