package cilab.utdallas.edu.ccimobile;

import org.jtransforms.fft.DoubleFFT_1D;


/**
 * This class manages the ACE processing strategy.
 * Created by hxa098020 on 6/14/2017.
 */
public class ACE {
    final private int Fs              = 16000;
    final private int BLOCK_SIZE      = 128;            //This is the variable length in ace.c
    final private int NUMBER_OF_BINS  = BLOCK_SIZE/2 + 1;             //NUM_BINS from ace.c //nfft/2 +1 = 65
    final private int BIN_FREQUENCY   = Fs/BLOCK_SIZE;

    private int blockShift,
            sizeOfBufferHistory;

    private double volumeLevel, scalarMultiplier, lgf_alpha;

    private int [] ranges, channelIndex, bandBins, current_levels, electrode_numbers, electrodeBands;

    private double [] window, windowFrequencyResponse, powerGains, bandGains, inputBuffer, workingData, magnitudes;   //

    private double [][] weights;

    private DoubleFFT_1D fft_1D;

    private MAP map;

    /**
     * This function creates a new MAP.
     * @param map m
     */
    ACE (MAP map) {
        this.map = map;
        blockShift = (int)(Math.ceil((double)map.samplingFrequency/map.stimulationRate));
        int NHIST = BLOCK_SIZE - blockShift;
        sizeOfBufferHistory= map.pulsesPerFramePerChannel*blockShift - blockShift; //
        window = new double[BLOCK_SIZE];
        windowFrequencyResponse = new double[3];
        weights = new double[map.nbands][NUMBER_OF_BINS];
        powerGains = new double[map.nbands];
        bandGains = new double[map.nbands];
        ranges = new int[map.nbands];
        inputBuffer = new double[sizeOfBufferHistory+BLOCK_SIZE];
        workingData = new double[BLOCK_SIZE];
        fft_1D = new DoubleFFT_1D((long) BLOCK_SIZE);
        magnitudes = new double[map.nMaxima];
        channelIndex = new int[map.nMaxima];
        scalarMultiplier = 1/(map.saturationLevel - map.baseLevel);
        lgf_alpha = 416.2063;
        electrodeBands = new int[map.nbands];
        bandBins = new int[map.nbands];
        current_levels = new int[map.nMaxima];
        electrode_numbers = new int[map.nMaxima];
        initialize();
    }


    /**
     * This function calls the initialization functions.
     */
    private void initialize() {
        createWindow(map.window);
        fftBandBins();
        initializeFilters();
        initializeChannelIndices();
        initializeGains();
        calculateDynamicRange();
        initializeVolumeSettings();
    }

    /**
     * Creates the stimuli.
     * @param inputData i
     * @return s
     */
    public Stimuli processAudio(double [] inputData)
    {
        Stimuli stimuli = new Stimuli();
        int j = 0; int indx =0; int offset = 0;

        stimuli.Amplitudes = new int[map.pulsesPerFrame];
        stimuli.Electrodes = new int[map.pulsesPerFrame];

        //System.out.println("Frame No: " +frame_no+ "\n");
        //startTimeFrame = System.currentTimeMillis();
        //inputData = audioFile.readFrames();

        for (int i = sizeOfBufferHistory; i < BLOCK_SIZE + sizeOfBufferHistory; i++) {
            inputBuffer[i] = inputData[i-sizeOfBufferHistory];
        }

        for (int subframe = 0; subframe < map.pulsesPerFramePerChannel; subframe++)
        {
            workingData = new double[BLOCK_SIZE];

            j = 0;
            for(int i = offset; i < BLOCK_SIZE + offset; i++) {
                workingData[j++] = inputBuffer[i]; }

            applyWindow(); // Apply Window
            fft();
            magnitudeSquaredSpectrum();
            weightedSquareSum();
            applyChannelGains();
            sorting();
            loudnessGrowthFunction();
            applyPatientMap();
            stimulationOrder();

            for (int i = 0; i < map.nMaxima; i++) {
                stimuli.Amplitudes[indx] = current_levels[i];
                stimuli.Electrodes[indx] = electrode_numbers[i];
                indx=indx+1;
            }
            offset+= blockShift;
        }
        // Save last NHIST samples of current buffer
        for (int i = 0; i < sizeOfBufferHistory; i++) {
            inputBuffer[i] = inputData[BLOCK_SIZE-sizeOfBufferHistory+i]; }

        //endTimeFrame = System.currentTimeMillis();
        //s+= "It took " + (endTimeFrame - startTimeFrame) + " ms to process this frame." + "\n";
        //s+= "frame_no: " + frame_no + "\n";
        ///s = "success"
        //frame_no ++;
        return stimuli;
    }
/////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * Applies the window
     */
    private void applyWindow() {
        for(int i = 0; i < workingData.length; i++) {
            workingData[i] = workingData[i] * window[i]; }
    }

    /**
     * FFT
     */
    private void fft() {
        fft_1D.realForward(workingData); //it appears that nfft = size of fftArray
        /* public void realForward(double[] a)
        Computes 1D forward DFT of real data leaving the result in a . The physical layout of the output data is as follows:
        if n is even then
        a[2*k] = Re[k], 0<=k<n/2
        a[2*k+1] = Im[k], 0<k<n/2
        a[1] = Re[n/2] */

        //fftArray[0] = Re[0] , fftArray[1] = Re[64]
        //fftArray[2] = Re[1] , fftArray[3] = Img[1]
        //
        //fftArray[126] = Re[63] , fftArray[127] = Img[63]
    }

    /**
     * Computes the squared magnitude of the entries of the frequency response vector
     */
    public void magnitudeSquaredSpectrum()
    {
        double [] tempMagSquaredArray = new double[NUMBER_OF_BINS];

        tempMagSquaredArray[0] = workingData[0]*workingData[0]; // dc value
        for(int j = 1; j < NUMBER_OF_BINS-1; j++) {
            tempMagSquaredArray[j] = (workingData[2*j] * workingData[2*j]) + (workingData[2*j+1] * workingData[2*j+1]); }
        tempMagSquaredArray[NUMBER_OF_BINS-1] = workingData[1]*workingData[1]; // Jtransform stores last value in the second index, i.e. a[1] = Re[n/2]

        workingData = tempMagSquaredArray;    //workingData is now size of 65 holding magnitude squared spectrum
    }

    /**
     * Computes multiplication with weights/triangular filtering to get the 22 frequency bands
     */
    public void weightedSquareSum() {
        double weightedMagTemp; //temporary variable to store the weighted magnitude
        double [] tempChannelMagnitudes = new double[map.nbands];

        for (int j = 0; j < map.nbands; j++) {
            weightedMagTemp = 0;
            for (int k = 0; k < NUMBER_OF_BINS; k++) {
                weightedMagTemp = weightedMagTemp + weights[j][k] * workingData[k];
            }
            tempChannelMagnitudes[j] = Math.sqrt(weightedMagTemp);
        }
        workingData = tempChannelMagnitudes;
    }

    public void applyChannelGains(){
        for (int j = 0; j < map.nbands; j++) {
            workingData[j] = workingData[j] * bandGains[j];
        }
    }

    public void sorting() {
        int [] channelIndices = new int[map.nbands];
        for(int i = 0; i < map.nbands; i++) {
            channelIndices[i] = i;   }
        shell_sort(workingData, channelIndices, map.nbands);
        //gets the highest nmaxima values
        for(int i = 0; i < map.nMaxima; i++) {
            magnitudes[i] = workingData[map.nbands-i-1];
            channelIndex[i] = channelIndices[map.nbands-i-1];
        }
    }

    //Loudness Growth Function
    public void loudnessGrowthFunction() {
        double workingVal;
        int chk = 0;

        //% Scale the input between base_level and sat_level:
        //r = (u - p.base_level)/(p.sat_level - p.base_level);

        for (int j = 0; j < map.nMaxima; j++) {
            //workingVal = (leftChannelMagnitudes[j] - baseLevel)/(saturationLevel-baseLevel);
            workingVal = scalarMultiplier * (magnitudes[j] - map.baseLevel);
            if (workingVal > 1)
                workingVal = 1;
            if (workingVal < 0) {
                workingVal = 0;
                chk = 1; }

            if (chk == 1) {
                magnitudes[j] = 0; //-1e-10 or a very small value
                chk = 0;
            } else {
                magnitudes[j] = (Math.log(1 + (lgf_alpha * workingVal))) / (Math.log(1 + lgf_alpha));
            }
        }
    }

    //converts magnitudes to current levels
    public void applyPatientMap()
    {
        double workingVal; int index;
        //Convert magnitudes to current levels i.e. apply patient map
        for(int j = 0; j < map.nMaxima; j++) {
            if(magnitudes[j] != 0) {
                //index = electrodeBands[channelIndex[j]]-1; //channelIndex[j];
                index = channelIndex[j]; //channelIndex[j];
                workingVal = ranges[index] * magnitudes[j] * volumeLevel;
                magnitudes[j] = map.THR[index] + workingVal;  // + (int)voltage_magnitudes;
                if(magnitudes[j] < map.THR[index]) //zero out signal less than THR
                    magnitudes[j] = 0;
                if(magnitudes[j] > map.MCL[index])
                    magnitudes[j] = map.MCL[index];
                if(magnitudes[j] < 0)
                    magnitudes[j] = 0; //from original code: this is so that it does not wrap around to give negative values
            }
        }
    }

    //Stimulation order
    public void stimulationOrder()
    {
        int [] channelIndices = new int[map.nMaxima];
        for (int j = 0; j < map.nMaxima; j++) // initialize all for 1:nmaxima
            channelIndices[j] = j;

        if (map.stimulationOrder.equals("apex-to-base")) { //apex to base
            shell_sort(channelIndex, channelIndices, map.nMaxima); // get the order
            for (int j = 0; j < map.nMaxima; j++) {
                current_levels[j] = (int) magnitudes[channelIndices[j]]; //reorder
                electrode_numbers[j] = map.electrodes[channelIndex[j]];
            }
        }

        else if (map.stimulationOrder.equals("base-to-apex")) { //base to apex
            shell_sort(channelIndex, channelIndices, map.nMaxima);
            for (int j = 0; j < map.nMaxima; j++) {
                electrode_numbers[j] = map.electrodes[(channelIndex[map.nMaxima - j - 1])];// highest to lowest
                current_levels[j] = (int) magnitudes[channelIndices[map.nMaxima - j - 1]];
            }
        }
        else { // if no stimulation order is intended
            for (int j = 0; j < map.nMaxima; j++) {
                current_levels[j] = (int)magnitudes[j];
                electrode_numbers[j] = map.electrodes[channelIndex[j]]; }
        }
    }

    private void createWindow(String windowType) {
        switch (windowType) {
            case "Blackman":
                for (int i = 0; i < BLOCK_SIZE; i++) {
                    window[i] = 0.42 - 0.5 * Math.cos(2 * Math.PI * i / (BLOCK_SIZE - 1)) + 0.08 * Math.cos(4 * Math.PI * i / (BLOCK_SIZE - 1));}
                windowFrequencyResponse[0]=722.5; windowFrequencyResponse[1]=1122.0; windowFrequencyResponse[2]=1234.5;
                break;
            case "Hanning":
                for (int i = 0; i < BLOCK_SIZE; i++) {
                    //double r = 2* Math.PI*i/BLOCK_SIZE; //Angle vector (in radians)
                    window[i] = 0.5 - 0.5* Math.cos(2 * Math.PI * i / BLOCK_SIZE);}
                windowFrequencyResponse[0]=1024.0; windowFrequencyResponse[1]=1475.6; windowFrequencyResponse[2]=1536.0;
                break;
            case "Hamming":
                for (int i = 0; i < BLOCK_SIZE; i++) {
                    window[i] = 0.54 - 0.46* Math.cos(2 * Math.PI * i / BLOCK_SIZE );}
                windowFrequencyResponse[0]=1194.4; windowFrequencyResponse[1]=1596.0; windowFrequencyResponse[2]=1627.8;
                break;
            default: // Hanning window
                for (int i = 0; i < BLOCK_SIZE; i++) {
                    window[i] = 0.5 - 0.5* Math.cos(2 * Math.PI * i / BLOCK_SIZE);}
                windowFrequencyResponse[0]=1024.0; windowFrequencyResponse[1]=1475.6; windowFrequencyResponse[2]=1536.0;
                break;
        }
        /*window FrequencyResponse is given as follows:
        freq_response  = freqz(window/2, 1, block_size);
        power_response = freq_response .* conj(freq_response);
        P1 = power_response(1);
        P2 = 2 * power_response(2);
        P3 = power_response(1) + 2 * power_response(3);
        */
    }
    private void initializeFilters() {
        int width; int bin = 3;  int j = 0;

        // Filter gains
        for(int i = 0; i < map.nbands; i++){
            width = bandBins[i];
            if (width == 1) { powerGains[i] = windowFrequencyResponse[0]; } // P1
            if (width == 2) { powerGains[i] = windowFrequencyResponse[1]; } // P2
            if (width > 2)  { powerGains[i] = windowFrequencyResponse[2];}  // P3
        }

        // Initialize weights
        for(int i = 0; i < map.nbands; i++){
            for(int z = 0; j < NUMBER_OF_BINS; j++) {
                weights[i][z] = 0; } }

        for(int i = 0; i < map.nbands; i++) {
            width = bandBins[i];
            for(int z = bin; z < bin + width; z++) {
                weights[i][z-1] = 1; }
            bin = bin + width; }

        for(int i = 0; i < map.nbands; i++) {
            for(int z = 0; z < NUMBER_OF_BINS; z++) {
                weights[i][z] = (weights[i][z]/(powerGains[i])); }}
    }

    private void calculateDynamicRange() {
        for(int i = 0; i < map.nbands; i++)  {
            ranges[i] = map.MCL[i] - map.THR[i]; }
    }

    private void initializeGains() {
        for(int i = 0; i < map.nbands; i++) {
            bandGains[i] = Math.pow(10.0, (map.gain + map.gains[i]) / 20.0); } // convert from dB to linear scale
    }

    private void initializeVolumeSettings() {
        volumeLevel = (double)map.volume/10; // Volume is on a scale of 0 to 10;
    }

    private void initializeChannelIndices() {
        for(int i = 0; i < map.nMaxima; i++) {
            channelIndex[i] = i; }
        for(int i = 0; i < map.nbands; i++) {
            electrodeBands[i] = map.numberOfChannels - map.electrodes[i] +1; }
    }

    //This is the nMaximaL sorting stuff
    public void shell_sort(int channelMagnitudes[], int channelIndex[], int number_of_bands)
    {
        int i,
                j,
                temp,
                tempIndex;
        int increment = 3;

        while(increment > 0)
        {
            for(i = 0; i <number_of_bands; i++)
            {
                j = i;
                temp = channelMagnitudes[i];
                tempIndex = channelIndex[i];

                while((j >= increment) && (channelMagnitudes[j-increment] > temp))
                {
                    channelMagnitudes[j] = channelMagnitudes[j - increment];
                    channelIndex[j] = channelIndex[j - increment];
                    j -= increment;
                }

                channelMagnitudes[j] = temp;
                channelIndex[j] = tempIndex;
            }

            increment >>= 1;
        }
    }

    //This is the nMaximaL sorting stuff
    public void shell_sort(double channelMagnitudes[], int channelIndex[], int number_of_bands)
    {
        int     i,
                j,
                tempIndex,
                increment = 3;
        double  temp;

        while(increment > 0)
        {
            for(i = 0; i <number_of_bands; i++)
            {
                j = i;
                temp = channelMagnitudes[i];
                tempIndex = channelIndex[i];

                while((j >= increment) && (channelMagnitudes[j-increment] > temp))
                {
                    channelMagnitudes[j] = channelMagnitudes[j - increment];
                    channelIndex[j] = channelIndex[j - increment];
                    j -= increment;
                }

                channelMagnitudes[j] = temp;
                channelIndex[j] = tempIndex;
            }

            increment >>= 1;
        }
    }

    private void fftBandBins() {
        switch (map.nbands) {
            case 22:
                bandBins = new int[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 4, 4, 5, 5, 6, 7, 8};
                break;
            case 21:
                bandBins = new int[]{1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 4, 4, 5, 6, 6, 7, 8};
                break;
            case 20:
                bandBins = new int[]{1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 3, 3, 4, 4, 5, 6, 7, 8, 8};
                break;
            case 19:
                bandBins = new int[]{1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 3, 3, 4, 4, 5, 6, 7, 8, 9};
                break;
            case 18:
                bandBins = new int[]{1, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 4, 4, 5, 6, 7, 8, 9};
                break;
            case 17:
                bandBins = new int[]{1, 1, 1, 2, 2, 2, 2, 2, 3, 3, 4, 4, 5, 6, 7, 8, 9};
                break;
            case 16:
                bandBins = new int[]{1, 1, 1, 2, 2, 2, 2, 2, 3, 4, 4, 5, 6, 7, 9, 11};
                break;
            case 15:
                bandBins = new int[]{1, 1, 1, 2, 2, 2, 2, 3, 3, 4, 5, 6, 8, 9, 13};
                break;
            case 14:
                bandBins = new int[]{1, 2, 2, 2, 2, 2, 3, 3, 4, 5, 6, 8, 9, 13};
                break;
            case 13:
                bandBins = new int[]{1, 2, 2, 2, 2, 3, 3, 4, 5, 7, 8, 10, 13};
                break;
            case 12:
                bandBins = new int[]{1, 2, 2, 2, 2, 3, 4, 5, 7, 9, 11, 14};
                break;
            case 11:
                bandBins = new int[]{1, 2, 2, 2, 3, 4, 5, 7, 9, 12, 15};
                break;
            case 10:
                bandBins = new int[]{2, 2, 3, 3, 4, 5, 7, 9, 12, 15};
                break;
            case 9:
                bandBins = new int[]{2, 2, 3, 3, 5, 7, 9, 13, 18};
                break;
            case 8:
                bandBins = new int[]{2, 2, 3, 4, 6, 9, 14, 22};
                break;
            case 7:
                bandBins = new int[]{3, 4, 4, 6, 9, 14, 22};
                break;
            case 6:
                bandBins = new int[]{3, 4, 6, 9, 15, 25};
                break;
            case 5:
                bandBins = new int[]{3, 4, 8, 16, 31};
                break;
            case 4:
                bandBins = new int[]{7, 8, 16, 31};
                break;
            case 3:
                bandBins = new int[]{7, 15, 40};
                break;
            case 2:
                bandBins = new int[]{7, 55};
                break;
            case 1:
                bandBins = new int[]{62};
                break;
            default:
                bandBins = new int[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 4, 4, 5, 5, 6, 7, 8};
                break;
        }
    }


}
