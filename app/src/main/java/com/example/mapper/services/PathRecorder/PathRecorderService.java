package com.example.mapper.services.PathRecorder;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.SensorEvent;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.util.Log;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Notification;

import com.example.mapper.R;
import com.example.mapper.sensors.AndroidSensorCallback;
import com.example.mapper.sensors.BarometerSensor;
import com.example.mapper.sensors.LocationSensor;
import com.example.mapper.sensors.TemperatureSensor;
import com.example.mapper.services.models.Point;
import com.example.mapper.services.models.Visit;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class PathRecorderService extends Service {

    // The Intents this service accepts Start, Pause and resume
    public static final String ACTION_START = "com.example.mapper.services.action.START";
    public static final String ACTION_PAUSE = "com.example.mapper.services.action.PAUSE";
    public static final String ACTION_RESUME = "com.example.mapper.services.action.RESUME";
    public static final String ACTION_STOP = "com.example.mapper.services.action.STOP";
    public static final String ACTION_POST_VISIT = "com.example.mapper.services.action.POST_VISIT";
    public static final String ACTION_FETCH_VISIT = "com.example.mapper.services.action.FETCH_VISIT";


    public static final String VISIT_TAG = "VISIT";
    public static final String RECEIVER_TAG = "receiverTag";
    public static final String PATH_TAG = "POINTS";
    public static final String TIME_TAG = "ELAPSED_TIME";



    public class PRSBinder extends Binder {
        public PathRecorderService getService() {
            return PathRecorderService.this;
        }
    }

    private static final String TAG = "PathRecorderService";

    private final IBinder mBinder;

    private LocationSensor mGPSSensor;
    private BarometerSensor mBarometerSensor;
    private TemperatureSensor mTempSensor;
    private boolean mRecordValues = true;
    private boolean mAllowDestroy = false;

    // For persistance with the foreground service.
    private Visit mVisit = null;
    private long mStartTime = 0;

    long pathID = 0;
    private List<Point> mRecordedPoints;
    private ResultReceiver mReceiver;

    public PathRecorderService() {
        mBinder = new PRSBinder();
    }

    /**
     * Static fn which tells the service to pause recording.
     * @param context
     */
    public static void pauseRecordingService(Context context) {
        Intent intent = new Intent(context, PathRecorderService.class);
        intent.setAction(ACTION_PAUSE);
        context.startService(intent);
    }

    /**
     * Static fn which tells the service to resume recording.
     * @param context
     */
    public static void resumeRecordingService(Context context) {
        Intent intent = new Intent(context, PathRecorderService.class);
        intent.setAction(ACTION_RESUME);
        context.startService(intent);
    }

    /**
     * Static fn which tells the service to stop the recording,
     * This in turn allows the service to end properly.
     * @param context
     */
    public static void stopRecordingService(Context context) {
        Intent intent = new Intent(context, PathRecorderService.class);
        intent.setAction(ACTION_STOP);
        context.startService(intent);

        context.stopService(intent);
    }

    /**
     * Tells the service to store the visit in memory.
     * @param context
     */
    public static void postVisitService(Context context, Visit v) {
        Intent intent = new Intent(context, PathRecorderService.class);
        intent.setAction(ACTION_POST_VISIT);
        intent.putExtra(VISIT_TAG, v);

        context.startService(intent);
    }


    /**
     * Tells the service to send visit data to the receiever
     * @param context
     * @param receiver A new receiver to use.
     */
    public static void fetchVisitService(Context context, LocationResultReceiver receiver) {
        Intent intent = new Intent(context, PathRecorderService.class);
        intent.setAction(ACTION_FETCH_VISIT);
        intent.putExtra(RECEIVER_TAG, receiver);
        context.startService(intent);
    }


    /**
     * Called when the service is provided a new intent
     * @param intent The intent to use
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (ACTION_START.equals(action)) {
            // Initialise the values needed to record
            mGPSSensor = new LocationSensor(this);
            mBarometerSensor = new BarometerSensor(this);
            mTempSensor = new TemperatureSensor(this);

            // Set the receiever if there is one specified.
            if (intent.hasExtra(RECEIVER_TAG)) {
                mReceiver = (ResultReceiver) intent.getParcelableExtra(RECEIVER_TAG);
                Log.d(TAG, "has receiver");
            }

            // Start this service in the foreground.
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
                startInForeground();
            } else {
                startInForegroundFallback();
            }

            // start recording
            startRecording();
        } else if (ACTION_PAUSE.equals(action)) {
            mRecordValues = false;
        } else if (ACTION_RESUME.equals(action)) {
            mRecordValues = true;
        } else if (ACTION_STOP.equals(action)) {
            stopRecording();
            mAllowDestroy = true;
        } else if (ACTION_POST_VISIT.equals(action)) {
            // Sending the visit details to the service, incase the app closes and loses the visit data.
            mVisit = (Visit) intent.getParcelableExtra(VISIT_TAG);
        } else if (ACTION_FETCH_VISIT.equals(action)) {
            // Fetches visit data from the service if it can.
            // Does nothing if there is no receiver, and no visit
            if (intent.hasExtra(RECEIVER_TAG)) {
                mReceiver = (ResultReceiver) intent.getParcelableExtra(RECEIVER_TAG);
                Log.d(TAG, "Has new receiver");
            }
            if (mVisit != null) {
                Bundle b = new Bundle();
                b.putParcelable(VISIT_TAG, mVisit);
                mReceiver.send(3, b);
            }
        }

        return Service.START_NOT_STICKY;
    }

    /**
     * Either restarts this service using PathRecorderRestarter or stops recording
     */
    @Override
    public void onDestroy() {
        if (!mAllowDestroy) {
            //Code for restarting service
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction("restartPathRecorder");
            broadcastIntent.setClass(this, PathRecorderRestarter.class);
            this.sendBroadcast(broadcastIntent);
        } else {
            stopRecording();
        }

    }

    /**
     * Called when this service is bound.
     * @param intent
     * @return
     */
    @Override
    public IBinder onBind(Intent intent) {
        mGPSSensor = new LocationSensor(this);
        mBarometerSensor = new BarometerSensor(this);
        mTempSensor = new TemperatureSensor(this);

        if (intent.hasExtra(RECEIVER_TAG)) {
            mReceiver = (ResultReceiver) intent.getParcelableExtra(RECEIVER_TAG);
            Log.d(TAG, "has receiver");
        }

        startRecording();

        return mBinder;
    }

    /**
     * Starts the service as a foreground process, in environments older than oreo
     */
    private void startInForegroundFallback () {
        Notification n = new Notification.Builder(this)
                .setContentTitle("Tracking your activity!")
                .setSmallIcon(R.drawable.logo)
                .build();
        startForeground(1, n);
    }

    /**
     * Starts the service as a foreground process, in environments at least as new as oreo.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private void startInForeground () {
        String NOTIFICATION_CHANNEL_ID = "example.permanence";
        String channelName = "PathRecorderService";

        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle("Tracking your activity!")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setSmallIcon(R.drawable.logo)
                .build();
        startForeground(2, notification);
    }


    /**
     * Registers location listeners and starts recording.
     */
    private void startRecording() {
        mRecordedPoints = new ArrayList<Point>();
        mStartTime = Calendar.getInstance().getTimeInMillis();

        AndroidSensorCallback callback = new AndroidSensorCallback() {
            @Override
            public void onSensorCallback (Location location) {
                if (!mRecordValues) return; //If paused, no need to record values

                if (location != null) {
                    double temp = 0.0, pressure = 0.0;

                    // Get the last results from the sensors if possible.
                    if (mTempSensor.sensorAvailable()) {
                        SensorEvent tempResults = mTempSensor.fetchLastResults();
                        if (tempResults != null) {
                            temp = tempResults.values[0];
                        }
                    }
                    if (mBarometerSensor.sensorAvailable()) {
                        SensorEvent pressureResults = mBarometerSensor.fetchLastResults();
                        if (pressureResults != null){
                            pressure = pressureResults.values[0];
                        }
                    }

                    double lat = location.getLatitude();
                    double lng = location.getLongitude();

                    Point p = new Point(lat, lng, pressure, temp, (int)pathID);
                    mRecordedPoints.add(p);

                    // Send data to the reciever if it's possible
                    if (mReceiver != null) {
                        Bundle bundle = new Bundle();
                        bundle.putParcelableArrayList(PATH_TAG, (ArrayList)mRecordedPoints);
                        mReceiver.send(1, bundle); //Code 1 for list of points
                    }

                }
            }
        };

        // Set callbacks and start sensing.
        mGPSSensor.setSensorCallback(callback);

        mGPSSensor.startSensing();
        mBarometerSensor.startSensing();
        mTempSensor.startSensing();
    }

    /**
     * Finishes recording, hands back the list of points to the reciever if there is one.
     */
    private void stopRecording() {
        long elapsedTime = Calendar.getInstance().getTimeInMillis() - mStartTime;
        elapsedTime = elapsedTime / 1000; //Convert To seconds

        // Send the final data to the reciever when we are done.
        if (mReceiver != null) {
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList(PATH_TAG, (ArrayList)mRecordedPoints);
            bundle.putLong(TIME_TAG, elapsedTime);
            mReceiver.send(2, bundle); //Code 1 for list of points
        }

        mGPSSensor.stopSensing();
        mBarometerSensor.stopSensing();
        mTempSensor.stopSensing();
        Log.d(TAG, "Finished recording!");
    }

    /**
     * Returns true if this service is running.
     * @param context
     * @return true/false dependant on whether this service is running
     */
    public static boolean checkIsRunning (Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (PathRecorderService.class.getName().equals(service.service.getClassName())) {
                Log.i ("Service status", "Running");
                return true;
            }
        }
        Log.i ("Service status", "Not running");
        return false;
    }

}
