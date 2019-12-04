package com.example.mapper.services;

import android.app.Service;
import android.content.Intent;
import android.hardware.SensorEvent;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
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
        Log.d(TAG, this.toString());
        mGPSSensor = new LocationSensor(this);
        mBarometerSensor = new BarometerSensor(this);
        mTempSensor = new TemperatureSensor(this);

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
                    SensorEvent pressureResults = mBarometerSensor.fetchLastResults();
                    SensorEvent tempResults = mTempSensor.fetchLastResults();

                    double temp = tempResults.values[0];
                    double pressure = pressureResults.values[0];

                    double lat = location.getLatitude();
                    double lng = location.getLongitude();

                    Log.d(TAG, "Recorded values");

                    Point p = new Point(lat, lng, pressure, temp, (int)pathID);
                    mRecordedPoints.add(p);
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
