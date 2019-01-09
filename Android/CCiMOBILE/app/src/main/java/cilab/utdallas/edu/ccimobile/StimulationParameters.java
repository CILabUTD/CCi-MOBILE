package cilab.utdallas.edu.ccimobile;

/**
 * Created by hxa098020 on 7/17/2017.
 */
public class StimulationParameters {
    int interPhaseGap = 8;            //Interphase Gap is 8us
    int durationSYNC = 6;             //Duration of Sycn Toekn in uS
    int additionalGap = 1;            //additional gap to make interpulse duration 7 uS ref. Fig. 14 in CIC4 specification


    public static MAP checkStimulationParameters(MAP map)
    {
        map = checkPulseWidth(map);
        map = checkStimulationRate(map);
        map = checkTimingParameters(map);
        map = computePulseTiming(map);
        return map;
    }


    private static MAP checkPulseWidth(MAP p) {
        if (p.pulseWidth>400) { p.pulseWidth = 400;} // Limit Pulse Width to 400us
        return p;
    }

    private static MAP checkStimulationRate(MAP p) {
        int totalStimulationRate;
        double maxPulseWidth;
        int interPhaseGap = 8;            //Interphase Gap is 8us
        int durationSYNC = 6;             //Duration of Sycn Toekn in uS
        int additionalGap = 1;            //additional gap to make interpulse duration 7 uS ref. Fig. 14 in CIC4 specification



        totalStimulationRate = (p.stimulationRate * p.nMaxima);
        if(p.stimulationRate <= 14400) //maximum stimulation rate supported by Freedom implants is 14400 Hz
        {
            if(totalStimulationRate <= 14400)
            {
                maxPulseWidth = Math.floor(0.5 * ((1000000 / totalStimulationRate) - (interPhaseGap + 11))); //for MP stimulation modes in CIC3/CIC4
                if(p.pulseWidth > maxPulseWidth)
                {
                    p.pulseWidth = (int)maxPulseWidth; //this means it is the STD protocol, PW is reduced to maxPW
                }

            }
        }

        if(totalStimulationRate > 14400)
        {
            //High Rate Protocol is currently not supported
            while (totalStimulationRate>14400){
                p.stimulationRate--;
                totalStimulationRate = (p.stimulationRate * p.nMaxima);
            }
            //print error, exit
        }
        return p;
    }

    private static MAP checkTimingParameters(MAP p) {
        int interPhaseGap = 8;            //Interphase Gap is 8us
        int durationSYNC = 6;             //Duration of Sycn Toekn in uS
        int additionalGap = 1;            //additional gap to make interpulse duration 7 uS ref. Fig. 14 in CIC4 specification



        p.pulsesPerFramePerChannel = (int) Math.round((8 * (double)p.stimulationRate) / 1000); //8ms //%floor(floor((8.0*rate_set)/1000));
        p.pulsesPerFrame = (int) (p.nMaxima * p.pulsesPerFramePerChannel);
        double totalStimulationRate = ((double)p.stimulationRate * (double)p.nMaxima);

        //Pulse-Width Centric
        // for pulse-width centric, max possible pulse-width is:
        //double maxPulseWidth = Math.floor(0.5*((8000/(double)p.nMaxima)-(interPhaseGap+durationSYNC+additionalGap)));
        double maxPulseWidth = Math.floor(0.5 * ((1000000 / totalStimulationRate) - (interPhaseGap + 11))); //for MP stimulation modes in CIC3/CIC4
        //double maxPulseWidth = Math.floor(0.5 * ((1000000 / totalStimulationRate) - (interPhaseGap + 11+200))); //for BP and CG stimulation modes in CIC3/CIC4

        if (p.pulseWidth>maxPulseWidth) { p.pulseWidth = (int)maxPulseWidth;} // Limit Pulse Width to 400us

        double pd1 = 8000/p.pulsesPerFrame; double pd2 = (p.pulseWidth * 2 + interPhaseGap + durationSYNC + additionalGap);
        if (pd1<pd2){
            while(pd1<pd2){
                p.stimulationRate--;
                p.pulsesPerFramePerChannel = (int) Math.round((8 * (double)p.stimulationRate) / 1000); //8ms //%floor(floor((8.0*rate_set)/1000));
                p.pulsesPerFrame = p.nMaxima * p.pulsesPerFramePerChannel;
                pd1 = 8000/p.pulsesPerFrame;
            }
        }
        return p;
    }

    private static MAP computePulseTiming(MAP p) {
        int frameDuration = 8;
        int interPhaseGap = 8;            //Interphase Gap is 8us
        int durationSYNC = 6;             //Duration of Sycn Toekn in uS
        int additionalGap = 1;            //additional gap to make interpulse duration 7 uS ref. Fig. 14 in CIC4 specification

        p.pulsesPerFramePerChannel = (int) Math.round((8 * (double)p.stimulationRate) / 1000); //8ms //%floor(floor((8.0*rate_set)/1000));
        p.pulsesPerFrame = (int) (p.nMaxima * p.pulsesPerFramePerChannel);
        p.stimulationRate = (int) (125 * p.pulsesPerFramePerChannel); //125 frames of 8ms in 1s
        //blockShiftL = (BLOCK_SIZE / pulsesPerFramePerChannel); //ceil(fs / p.Left.analysis_rate);
        p.interpulseDuration = (double)frameDuration*1000/((double)p.pulsesPerFrame) - 2*(double)p.pulseWidth - (double)interPhaseGap - (double)durationSYNC - (double)additionalGap;
        p.nRFcycles = (int) Math.round((p.interpulseDuration/0.1));
        return p;
    }
}
