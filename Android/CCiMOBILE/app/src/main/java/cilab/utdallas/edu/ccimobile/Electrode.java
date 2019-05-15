package cilab.utdallas.edu.ccimobile;

class Electrode {

    private int electrodeNum, THR1, THR2, MCL1, MCL2;
    private double gain1, gain2;
    private boolean onOrOff;

    Electrode(int electrodeNum, boolean onOrOff, int THR1, int MCL1, double gain1,
              int THR2, int MCL2, double gain2) {
        this.electrodeNum = electrodeNum;
        this.onOrOff = onOrOff;
        this.THR1 = THR1;
        this.MCL1 = MCL1;
        this.gain1 = gain1;
        this.THR2 = THR2;
        this.MCL2 = MCL2;
        this.gain2 = gain2;
    }

    int getElectrodeNum() {
        return electrodeNum;
    }

    double getGain1() { return gain1; }

    double getGain2() {
        return gain2;
    }

    int getMCL1() {
        return MCL1;
    }

    int getMCL2() {
        return MCL2;
    }

    int getTHR1() {
        return THR1;
    }

    int getTHR2() {
        return THR2;
    }

    boolean getOnOrOff() {
        return onOrOff;
    }
}
