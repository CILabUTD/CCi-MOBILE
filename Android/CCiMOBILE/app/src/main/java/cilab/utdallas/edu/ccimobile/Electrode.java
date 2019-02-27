package cilab.utdallas.edu.ccimobile;

public class Electrode {

    private int electrodeNum, THR1, THR2, MCL1, MCL2, gain1, gain2;
    private boolean onOrOff;

    public Electrode(int electrodeNum, boolean onOrOff, int THR1, int MCL1, int gain1,
                     int THR2, int MCL2, int gain2) {
        this.electrodeNum = electrodeNum;
        this.onOrOff = onOrOff;
        this.THR1 = THR1;
        this.MCL1 = MCL1;
        this.gain1 = gain1;
        this.THR2 = THR2;
        this.MCL2 = MCL2;
        this.gain2 = gain2;
    }

    public int getElectrodeNum() {
        return electrodeNum;
    }

    public int getGain1() {
        return gain1;
    }

    public int getGain2() {
        return gain2;
    }

    public int getMCL1() {
        return MCL1;
    }

    public int getMCL2() {
        return MCL2;
    }

    public int getTHR1() {
        return THR1;
    }

    public int getTHR2() {
        return THR2;
    }

    public boolean getonOrOff() {
        return onOrOff;
    }
}
