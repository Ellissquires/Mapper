package com.example.mapper.services.PathRecorder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;


/**
 * Class for sending Location from service to activity.
 */
public class LocationResultReceiver extends ResultReceiver {

    private Receiver mReceiver;

    /**
     * Create a new ResultReceive to receive results.  Your
     * {@link #onReceiveResult} method will be called from the thread running
     * <var>handler</var> if given, or from an arbitrary thread if null.
     *
     * @param handler
     */
    public LocationResultReceiver(Handler handler) {
        super(handler);
    }

    /**
     * Implement this reciever if you want to be able to receive results from a service in an
     * activity,
     */
    public interface Receiver {
        void onReceiveResult(int resultCode, Bundle resultData);
        void onPathPut(int resultCode, Bundle resultData);
        void onPathFinish(int resultCode, Bundle resultData);
        void onVisitFetch(int resultCode, Bundle resultData);
    }

    public void setReceiver (Receiver receiver) {
        mReceiver = receiver;
    }

    /**
     *
     * @param rc An integer result code 0 = LocationResult, 1 = path update, 2 = path finish.
     * @param b
     */
    @Override
    public void onReceiveResult(int rc, Bundle b) {
        if (mReceiver != null) {
            if (rc == 0) // 0 for location result (such as one from LocationSensor)
                mReceiver.onReceiveResult(rc, b);
            else if (rc == 1) // For passing the semi-completed path back
                mReceiver.onPathPut(rc, b);
            else if (rc == 2) // For passing the final path back
                mReceiver.onPathFinish(rc, b);
            else if (rc == 3) // For passing visit data back
                mReceiver.onVisitFetch(rc, b);
        }
    }

}
