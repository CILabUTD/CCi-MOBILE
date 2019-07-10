package cilab.utdallas.edu.ccimobile;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.ftdi.j2xx.D2xxManager;
import com.ftdi.j2xx.FT_Device;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by hxa098020 on 6/8/2017.
 */
public class InitializationService extends IntentService{
    private static final String TAG = "InitializationService";
    public static final int STATUS_RUNNING = 0;
    public static final int STATUS_STEP0_FINISHED = 88;
    public static final int STATUS_STEP1_FINISHED = 1;
    public static final int STATUS_STEP2_FINISHED = 2;
    public static final int STATUS_STEP3_FINISHED = 3;
    public static final int STATUS_FINISHED = 99;
    public static final int STATUS_ERROR = -1;

    public static D2xxManager ftD2xx = null;
    FT_Device ft_device_0;
    FT_Device ft_device_1;
    FT_Device ftDev; //ccimobileHardware
    int DevCount = -1;
    int currentPortIndex = -1;
    int portIndex = -1;
    Queue<Integer> availQ;

    startupThread startupThread1;
    warmupThread warmupThread1;
    byte[] writeBuffer, nullwriteBuffer;

    int baudRate = 5000000; /* baud rate */ //460800; 921600; //5000000
    byte stopBit = (byte)2; /* 1:1stop bits, 2:2 stop bits */
    byte dataBit = (byte)8; /* 8:8bit, 7: 7bit */
    byte parity = (byte)0; /* 0: none, 1: odd, 2: even, 3: mark, 4: space */
    byte flowControl = (byte)1; /* 0:none, 1: CTS/RTS, 2:DTR/DSR, 3:XOFF/XON */
    public Context global_context;
    boolean uart_configured = false;

    final byte XON = 0x11;    /* Resume transmission */
    final byte XOFF = 0x13;    /* Pause transmission */

    boolean bReadThreadEnable = false;

    short sine_stim[];
    short sin_token[]; short null_token[];
    short sine_token[];

    Stimuli stimuli;

    D2xxManager.DriverParameters s;

    final int nframes = 500;
    final int samplesPerFrame = 128;
    //final byte [] dataBytes =  new byte[4*samplesPerFrame*nframes]; // 512 *nframes

    public InitializationService() {
        super(InitializationService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        //Log.d(TAG, "Service Started!");
        final ResultReceiver receiver = intent.getParcelableExtra("receiver");
        Bundle bundle = new Bundle();

        /* Update UI: Download Service is Running */
        receiver.send(STATUS_RUNNING, Bundle.EMPTY);

        String output = "";
        int responseStep0 = 0; int responseStep1 = 0; int responseStep2 = 0; int responseStep3 = 0;
        try {

            responseStep0 = initializeConnection();
            if (responseStep0==1) { output = "Step 0 completed"; };
            if (responseStep0==0) { output = "Step 0 unsuccessful"; };
                /* Sending result back to activity */
            if (!output.isEmpty()) { // null != output
                bundle.putString("result", Integer.toString(responseStep0));
                receiver.send(STATUS_STEP0_FINISHED, bundle);            }
        } catch (Exception e) {
                /* Sending error message back to activity */
            bundle.putString(Intent.EXTRA_TEXT, e.toString());
            receiver.send(STATUS_ERROR, bundle);
        }

        if (responseStep0!=0) {

            try {
                responseStep1 = step1();
                if (responseStep1 == 1) {
                    output = "Step 1 completed";
                }
                ;
                //Sending result back to activity //
                if (!output.isEmpty()) {
                    bundle.putStringArray("result", new String[]{output});
                    receiver.send(STATUS_STEP1_FINISHED, bundle);
                }
            } catch (Exception e) {
                // Sending error message back to activity //
                bundle.putString(Intent.EXTRA_TEXT, e.toString());
                receiver.send(STATUS_ERROR, bundle);
            }
        }

        if (responseStep1!=0) {

            try {
                responseStep2 = step2();
                if (responseStep2 == 1) {
                    output = "Step 2 completed";
                }

                //Sending result back to activity //
                if (!output.isEmpty()) {
                    bundle.putStringArray("result", new String[]{output});
                    receiver.send(STATUS_STEP2_FINISHED, bundle);
                }
            } catch (Exception e) {
                // Sending error message back to activity //
                bundle.putString(Intent.EXTRA_TEXT, e.toString());
                receiver.send(STATUS_ERROR, bundle);
            }
        }

        if (responseStep2!=0) {

            try {
                responseStep3 = step3();
                if (responseStep3== 1) {
                    output = "Step 3 completed";
                }

                //Sending result back to activity //
                if (!output.isEmpty()) {
                    bundle.putStringArray("result", new String[]{output});
                    //bundle.put
                    receiver.send(STATUS_STEP3_FINISHED, bundle);
                }
            } catch (Exception e) {
                // Sending error message back to activity //
                bundle.putString(Intent.EXTRA_TEXT, e.toString());
                receiver.send(STATUS_ERROR, bundle);
            }
        }

        Log.d(TAG, "Service Stopping!");
        this.stopSelf();
    }



    private int initializeConnection() {
        boolean t = false;
        availQ = new LinkedList<Integer>();
        try{
            ftD2xx = D2xxManager.getInstance(this);
            s =new D2xxManager.DriverParameters();
            s.setBufferNumber(16);
            s.setReadTimeout(0);
            t = true;
        }
        catch (D2xxManager.D2xxException e) {
            Log.e("FTDI_HT", "getInstance fail!!");
        }

        initializeStimuli();

        try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); }
        return initializeDevice();
    }

    private int initializeDevice() {
        createDeviceList();
        try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); }
        if(DevCount > 0)
        {
            connectFunction();
            ftDev.resetDevice();
            ftDev.clrRts();
            setConfig(baudRate, dataBit, stopBit, parity, flowControl);
        }
        if (ftDev==null) {return 0;}else {
            return 1;}
        //return DevCount;
    }

    private void initializeStimuli() {
        writeBuffer = new byte[516]; nullwriteBuffer = new byte[516];
        stimuli = new Stimuli();
        sine_stim = new short[8];
        sin_token = new short[8]; null_token = new short[8];
        sine_token = new short[50];
        for (int i = 0; i < 8; ++i) {
            sin_token[i] = (short)(i + 1);
        }
        setOutputBuffer(writeBuffer,sin_token); //sine buffer
        setOutputBuffer(nullwriteBuffer,null_token); //null buffer
        for (int i = 0; i < 50; ++i) {
            sine_token[i] = (short) (200 * Math.sin(2 * Math.PI * i * 0.01)); // this is one cycle of sine wave
        }
    }

    private void restart() {
        global_context = this;
        boolean t = false;
        //int DevCount = -1;
        //int currentPortIndex = -1;
        //int portIndex = -1;
        //onDestroy();
        ftDev.resetDevice();
        ftDev.close();
        ftDev = null;
        //bReadThreadEnable = false;
        createDeviceList();

        if(DevCount > 0)
        {
            connectFunction();
            ftDev.resetDevice();
            ftDev.clrRts();
            setConfig(baudRate, dataBit, stopBit, parity, flowControl);
        }

        try{
            ftD2xx = D2xxManager.getInstance(this);
            s =new D2xxManager.DriverParameters();
            s.setBufferNumber(16);
            s.setReadTimeout(0);
        }
        catch (D2xxManager.D2xxException e) {
            Log.e("FTDI_HT", "getInstance fail!!");
        }
        //global_context = this;
        //verifyStoragePermissions(this);
        /*try {
            textOut("BOARD IS READY NOW");
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

/*
    protected void onPause() {
        super.onPause();
    }

    protected void onStop() {
        super.onStop();
    }

    protected void onDestroy() {
        disconnectFunction();
        android.os.Process.killProcess(android.os.Process.myPid());
        super.onDestroy();
    }
*/

    /*
     *function that basically generates a list of ftdi devices connected to the uart port.
     * An ftdi device is recognized as 2. Each for a different kind of communication. We always use the 2nd device.
     */
    public void createDeviceList() {
        global_context = this;
        int tempDevCount = ftD2xx.createDeviceInfoList(global_context);
        StringBuilder stringdevcount = new StringBuilder("Dev Count: ");

        if (tempDevCount > 0)
        {
            if( DevCount != tempDevCount )
            {
                DevCount = tempDevCount;
            }
        }
        else
        {
            DevCount = -1;
            currentPortIndex = -1;
        }
        stringdevcount.append(String.valueOf(DevCount));
    }

    public void disconnectFunction() {
        DevCount = -1;
        currentPortIndex = -1;
        bReadThreadEnable = false;
        try
        {
            Thread.sleep(50);
        }
        catch (InterruptedException e) {e.printStackTrace();}

        if(ftDev != null)
        {
            if(ftDev.isOpen())
            {
                ftDev.close();

            }
        }
    }
    /*
    Function that chooses the second device
     */
    public void connectFunction() {
        StringBuilder stringport = new StringBuilder("Port No.: ");
        if( portIndex + 1 > DevCount){
            portIndex = 0;
        }

        if( currentPortIndex == portIndex && ftDev != null && ftDev.isOpen()){
            stringport.append(String.valueOf(portIndex));
            return;
        }

        if(null == ftDev){
            ft_device_0 = ftD2xx.openByIndex(global_context, 0,s);
            ft_device_1 = ftD2xx.openByIndex(global_context, 1,s);
            ftDev = ft_device_0;
            ftDev.purge((byte)(D2xxManager.FT_PURGE_RX|D2xxManager.FT_PURGE_TX));
        }

        uart_configured = false;

        if(ftDev == null)
        {
            stringport.append(String.valueOf(portIndex));
            return;
        }

        if (ftDev.isOpen())
        {
            currentPortIndex = portIndex;
            ftDev.purge(D2xxManager.FT_PURGE_RX);
        }
        else
        {
            stringport.append(String.valueOf(portIndex));

        }
    }

    void setConfig(int baud, byte dataBits, byte stopBits, byte parity, byte flowControl) {
        // configure port
        // reset to UART mode for 232 devices
        ftDev.setBitMode((byte) 0, D2xxManager.FT_BITMODE_RESET);

        ftDev.setBaudRate(baud);

        switch (dataBits)
        {
            case 7:
                dataBits = D2xxManager.FT_DATA_BITS_7;
                break;
            case 8:
                dataBits = D2xxManager.FT_DATA_BITS_8;
                break;
            default:
                dataBits = D2xxManager.FT_DATA_BITS_8;
                break;
        }

        switch (stopBits)
        {
            case 1:
                stopBits = D2xxManager.FT_STOP_BITS_1;
                break;
            case 2:
                stopBits = D2xxManager.FT_STOP_BITS_2;
                break;
            default:
                stopBits = D2xxManager.FT_STOP_BITS_1;
                break;
        }

        switch (parity)
        {
            case 0:
                parity = D2xxManager.FT_PARITY_NONE;
                break;
            case 1:
                parity = D2xxManager.FT_PARITY_ODD;
                break;
            case 2:
                parity = D2xxManager.FT_PARITY_EVEN;
                break;
            case 3:
                parity = D2xxManager.FT_PARITY_MARK;
                break;
            case 4:
                parity = D2xxManager.FT_PARITY_SPACE;
                break;
            default:
                parity = D2xxManager.FT_PARITY_NONE;
                break;
        }

        ftDev.setDataCharacteristics(dataBits, stopBits, parity);

        short flowCtrlSetting;
        switch (flowControl)
        {
            case 0:
                flowCtrlSetting = D2xxManager.FT_FLOW_NONE;
                break;
            case 1:
                flowCtrlSetting = D2xxManager.FT_FLOW_RTS_CTS;
                break;
            case 2:
                flowCtrlSetting = D2xxManager.FT_FLOW_DTR_DSR;
                break;
            case 3:
                flowCtrlSetting = D2xxManager.FT_FLOW_XON_XOFF;
                break;
            default:
                flowCtrlSetting = D2xxManager.FT_FLOW_NONE;
                break;
        }

        ftDev.setFlowControl(flowCtrlSetting, XON, XOFF);
        uart_configured = true;
    }

    /*
     *function for setting the output data buffer as per the given guidelines
     */
    private void setOutputBuffer(byte outputBuffer[], short sine_token[] ) {

        short n = 8;
        short electrode_token[] = new short[8]/*, sine_token[8]*/;
        for (short i = 0; i < n; ++i) {
            electrode_token[i] = (short)(i + 1);
            //sine_token[i] = i + 1;
        }
        short ppf = 64;
        short pw = 25;

        short mode = 28;
        short elecs[] = new short[64];
        short amps[] = new short[64];
        for (int i = 0; i < ppf; ++i) {
            int j = i / n;
            amps[i] = sine_token[j];
            elecs[i] = electrode_token[i % 8];
        }
        //char outputBuffer[516] = {0};
        outputBuffer[0] = (byte)136;
        outputBuffer[1] = (byte)254;
        outputBuffer[2] = 5;
        outputBuffer[3] = 1;
        outputBuffer[4] = 4;
        outputBuffer[5] = (byte)252;
        for (int i = 0; i < 64; ++i) {
            outputBuffer[i + 6] = (byte)elecs[i];
            outputBuffer[i + 132] = (byte)amps[i];
            outputBuffer[i + 264] = (byte)elecs[i];
            outputBuffer[i + 390] = (byte)amps[i];
            //cout << i << endl;
        }
        outputBuffer[258] = (byte)136;
        outputBuffer[259] = (byte)254;
        outputBuffer[260] = (byte)5;
        outputBuffer[261] = (byte)1;
        outputBuffer[262] = (byte)4;
        outputBuffer[263] = (byte)252;

        outputBuffer[380] = (byte)mode; //mode left
        outputBuffer[381] = (byte)mode; //mode right
        outputBuffer[382] = (byte)(pw / 256); //left pulsewidth high[15:8]
        outputBuffer[383] = (byte)(pw % 256); //left pulsewidth low[7:0]
        outputBuffer[384] = (byte)(pw / 256); //right pulsewidth high[15:8]
        outputBuffer[385] = (byte)(pw % 256); //right pulsewidth low[7:0]

        outputBuffer[506] = (byte)(ppf / 256); //left pulsesPerFrame high[15:8]
        outputBuffer[507] = (byte)(ppf % 256); //left pulsesPerFrame low[7:0]
        outputBuffer[508] = (byte)(ppf / 256); //right pulsesPerFrame high[15:8]
        outputBuffer[509] = (byte)(ppf % 256); //right pulsesPerFrame low[7:0]

        short cycles = 600;
        outputBuffer[510] = (byte)(cycles / 256); //left pulsesPerFrame high[15:8]
        outputBuffer[511] = (byte)(cycles % 256); //left pulsesPerFrame low[7:0]
        outputBuffer[512] = (byte)(cycles / 256); //right pulsesPerFrame high[15:8]
        outputBuffer[513] = (byte)(cycles % 256); //right pulsesPerFrame low[7:0]

    }

    private int step1() {
        int step1Result =0;
        try {
            startupThread1 = new startupThread();
            startupThread1.start();
            Thread.sleep(1000);
            step1Result = 1;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return step1Result;
    }

    private int step2() {
        int step2Result =0;
        try {
            restart();
            Thread.sleep(1000);
            step2Result = 1;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return step2Result;
    }

    private int step3() {
        int step3Result =0;
        try {
            portIndex = 1;
            warmupThread1 = new warmupThread();
            warmupThread1.start();
            Thread.sleep(1000);
            step3Result = 1;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        disconnectFunction();
        return step3Result;
    }


    private class startupThread  extends Thread {
        //Handler mHandler;
        startupThread() {
            this.setPriority(Thread.MAX_PRIORITY);
        }

        @Override
        public void run() {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
            //if (ftDev == null) { initializeDevice();}
                int rc = ftDev.write(nullwriteBuffer, 516, true);
                bReadThreadEnable = true;
                long i = 0;
                byte[] readBuffer = new byte[512];

                while (bReadThreadEnable) {
                    int iavailable_0 = ftDev.getQueueStatus();
                    if (iavailable_0 >= 512) {
                        if (i < 10) {
                        /*clear out first 8 frames,the buffer the remaining 2*/
                            if (i < 8) {
                                byte[] temp;

                                while (iavailable_0 > 0) {
                                    temp = new byte[iavailable_0];
                                    ftDev.read(temp, iavailable_0, 0);
                                    //Log.e(">>@@", "availablebr:: " + iavailable_0);
                                    availQ.add(iavailable_0);
                                    try {
                                        //Thread.sleep(18, 0);
                                        Thread.sleep(1, 0); // 1ms sleep
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    iavailable_0 = ftDev.getQueueStatus();
                                }
                                rc = ftDev.write(nullwriteBuffer, 516, true);
                            } else {
                                //bReadThreadEnable = false
                                ftDev.read(readBuffer, 512, 0);
                                rc = ftDev.write(nullwriteBuffer, 516, true);
                                Arrays.fill(readBuffer, (byte) 0);
                            /*try {
                                Thread.sleep(50,0);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }*/
                                //bReadThreadEnable = false;
                                //break;
                            }
                            i++;
                        } else {
                            break;
                        }
                    }
                }

        }
    }

    private class warmupThread  extends Thread {
        warmupThread() {
            this.setPriority(Thread.MAX_PRIORITY);
        }

        @Override
        public void run() {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
            final byte[] buffbyte2 = new byte[512];
            final double[] leftData = new double[128];
            final double[] rightData = new double[128];
            final short[] shortbuffer = new short[256];
            byte[] readBuffer = new byte[512];

            int rc = ftDev.write(nullwriteBuffer, 516, true);
            bReadThreadEnable = true;
            int k = 0, index = 0;
            long i = 0;
            long time1, time2;

            while (bReadThreadEnable) {
                int iavailable_0 = ftDev.getQueueStatus();
                if (iavailable_0 >= 512) {

                    if (i==100){
                        //setOutputBuffer(nullwriteBuffer, null_token); // zero buffer
                        ftDev.read(readBuffer, 512, 0);
                        rc = ftDev.write(nullwriteBuffer, 516, true);
                        bReadThreadEnable = false;
                    }else
                    {
                        for (int j = 0; j < 8; ++j) {
                            sine_stim[j] = sine_token[k]; //200;
                            ++k;
                            k %= 50;
                        }
                        setOutputBuffer(writeBuffer, sine_stim);
                        Arrays.fill(buffbyte2, (byte) 0);

                        ftDev.read(readBuffer, 512, 0);
                        //System.arraycopy(readBuffer, 0, buffbyte2, 0, 512);
                        System.arraycopy(readBuffer, 0, buffbyte2, 0, 512);

                        ByteBuffer.wrap(buffbyte2).order(ByteOrder.BIG_ENDIAN).asShortBuffer().get(shortbuffer);
                        for (int m = 0; m < leftData.length; m++) {
                            leftData[m] = ((double) shortbuffer[2 * m]) / 32768.0;
                            rightData[m] = ((double) shortbuffer[(2 * m) + 1]) / 32768.0;
                            //leftData[m] = (double) ((double) shortbuffer[2 * m]) / 7124.0; //32768/4.6 = 7124 - The recorded signal is 4.6 times lower in amplitude
                        }
                        rc = ftDev.write(nullwriteBuffer, 516, true);
                        ++i;
                    }
                }

            }
        }
    }

}
