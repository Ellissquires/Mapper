package com.example.mapper.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

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

    public interface Receiver {
        void onReceiveResult(int resultCode, Bundle resultData);
        void onPathPut(int resultCode, Bundle resultData);
        void onPathFinish(int resultCode, Bundle resultData);
    }

    public void setReceiver (Receiver receiver) {
        mReceiver = receiver;
    }

    @Override
    public void onReceiveResult(int rc, Bundle b) {
        if (mReceiver != null) {
            if (rc == 0) //0 for location result
                mReceiver.onReceiveResult(rc, b);
            else if (rc == 1)
                mReceiver.onPathPut(rc, b);
            else if (rc == 2)
                mReceiver.onPathFinish(rc, b);
        }
    }

}
