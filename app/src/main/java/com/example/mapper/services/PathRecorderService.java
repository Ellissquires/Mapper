package com.example.mapper.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.SensorEvent;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.util.Log;

import com.example.mapper.sensors.AndroidSensorCallback;
import com.example.mapper.sensors.BarometerSensor;
import com.example.mapper.sensors.LocationSensor;
import com.example.mapper.sensors.TemperatureSensor;
import com.example.mapper.services.models.Path;
import com.example.mapper.services.models.Point;
import com.example.mapper.services.models.RepoInsertCallback;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;


public class PathRecorderService extends Service {

    // The Intents this service accepts Start, Pause and resume
    public static final String ACTION_START = "com.example.mapper.services.action.START";
    public static final String ACTION_PAUSE = "com.example.mapper.services.action.PAUSE";
    public static final String ACTION_RESUME = "com.example.mapper.services.action.RESUME";


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

            if (intent.hasExtra("receiverTag")) {
                mReceiver = (ResultReceiver) intent.getParcelableExtra("receiverTag");
                Log.d(TAG, "has receiver");
            }
            // Start recording.
            startRecording();
        } else if (ACTION_PAUSE.equals(action)) {
            mRecordValues = false;
        } else if (ACTION_RESUME.equals(action)) {
            mRecordValues = true;
        }

        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        stopRecording();

        //TODO: Cleanup variables here
    }

    @Override
    public IBinder onBind(Intent intent) {
        mGPSSensor = new LocationSensor(this);
        mBarometerSensor = new BarometerSensor(this);
        mTempSensor = new TemperatureSensor(this);

        if (intent.hasExtra("receiverTag")) {
            mReceiver = (ResultReceiver) intent.getParcelableExtra("receiverTag");
            Log.d(TAG, "has receiver");
        }

        startRecording();

        return mBinder;
    }


    /**
     * Registers location listeners and starts recording.
     */
    private void startRecording() {
        mRecordedPoints = new ArrayList<Point>();

        AndroidSensorCallback callback = new AndroidSensorCallback() {
            @Override
            public void onSensorCallback (Location location) {
                if (!mRecordValues) return;

                if (location != null) {
                    double temp = 0.0, pressure = 0.0;

                    // Get the last results from the sensors i9f possible.
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

                    Log.d(TAG, "Recorded values");

                    Point p = new Point(lat, lng, pressure, temp, (int)pathID);
                    mRecordedPoints.add(p);

                    if (mReceiver != null) {
                        Bundle bundle = new Bundle();
                        bundle.putParcelableArrayList("points", (ArrayList)mRecordedPoints);
                        mReceiver.send(1, bundle); //Code 1 for list of points
                    }

                }
            }
        };
        mGPSSensor.setSensorCallback(callback);

        mGPSSensor.startSensing();
        mBarometerSensor.startSensing();
        mTempSensor.startSensing();
    }

    /**
     * Finishes recording, hands back the list of points to the reciever if there is one.
     */
    private void stopRecording() {
        if (mReceiver != null) {
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList("points", (ArrayList)mRecordedPoints);
            mReceiver.send(2, bundle); //Code 1 for list of points
        }

        mGPSSensor.stopSensing();
        mBarometerSensor.stopSensing();
        mTempSensor.stopSensing();
        Log.d(TAG, "Finished recording!");
    }

}
