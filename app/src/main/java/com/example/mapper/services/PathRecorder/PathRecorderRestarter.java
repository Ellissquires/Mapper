package com.example.mapper.services.PathRecorder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

/**
 * Used for when the PathRecorderService is stopped by the android system,
 * When a message is sent to this classes it restarts it.
 */
public class PathRecorderRestarter extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("Broadcast Listened", "PathRecordingService tried to stop");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(new Intent(context, PathRecorderService.class));
        } else {
            context.startService(new Intent(context, PathRecorderService.class));
        }
    }
}
