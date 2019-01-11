package cilab.utdallas.edu.ccimobile;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

/**
 * Created by hxa098020 on 6/8/2017.
 */
@SuppressLint("ParcelCreator")
public class InitializationResultReceiver extends ResultReceiver {
    /**
     * Create a new ResultReceive to receive results.  Your
     * {@link #onReceiveResult} method will be called from the thread running
     * <var>handler</var> if given, or from an arbitrary thread if null.
     *
     * @param handler
     */
    private Receiver mReceiver;
    public InitializationResultReceiver(Handler handler) {  super(handler); }
    public void setReceiver(Receiver receiver) { mReceiver = receiver;  }
    public interface Receiver {
        public void onReceiveResult(int resultCode, Bundle resultData);
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if (mReceiver != null) {
            mReceiver.onReceiveResult(resultCode, resultData);
        }
    }
}
