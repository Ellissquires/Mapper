package com.example.mapper.services;

import android.app.Service;
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

    private PathRepository mPathRepo;
    private PointRepository mPointRepo;

    long pathID = 0;
    private List<Point> mRecordedPoints;

    private ResultReceiver mReceiver;

    public PathRecorderService() {
        mBinder = new PRSBinder();
        mPathRepo = new PathRepository(getApplication());
        mPointRepo = new PointRepository(getApplication());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, this.toString());
        mGPSSensor = new LocationSensor(this);
        mBarometerSensor = new BarometerSensor(this);
        mTempSensor = new TemperatureSensor(this);

        if (intent.hasExtra("receiverTag")) {
            mReceiver = (ResultReceiver) intent.getParcelableExtra("receiverTag");
            Log.d(TAG, "has receiver");
        }

        startRecording();

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


    private void startRecording() {
        mPathRepo.createPath(new RepoInsertCallback(){
            @Override
            public void OnFinishInsert(Long rowID) {
                pathID = rowID;
                Log.d(TAG, "Inserted path with ID: " + pathID);
            }
        });
        mRecordedPoints = new ArrayList<Point>();

        AndroidSensorCallback callback = new AndroidSensorCallback() {
            @Override
            public void onSensorCallback (Location location) {
                if (location != null) {
                    double temp = 0.0, pressure = 0.0;

                    if (mTempSensor.sensorAvailable()) {
                        SensorEvent tempResults = mTempSensor.fetchLastResults();
                        temp = tempResults.values[0];
                    }
                    if (mBarometerSensor.sensorAvailable()) {
                        SensorEvent pressureResults = mBarometerSensor.fetchLastResults();
                        pressure = pressureResults.values[0];
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

    private void stopRecording() {
        for (Point p : mRecordedPoints) {
            mPointRepo.createPoint(p);
        }

        mGPSSensor.stopSensing();
        mBarometerSensor.stopSensing();
        mTempSensor.stopSensing();
        Log.d(TAG, "Finished recording!");
    }

}
