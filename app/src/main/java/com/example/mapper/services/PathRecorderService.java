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
import com.example.mapper.services.models.Point;


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

    public PathRecorderService() {
        mBinder = new PRSBinder();
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
    public IBinder onBind(Intent intent) {
        Log.d(TAG, this.toString());
        mGPSSensor = new LocationSensor(this);
        mBarometerSensor = new BarometerSensor(this);
        mTempSensor = new TemperatureSensor(this);

        startRecording();

        return mBinder;
    }


    private void startRecording() {
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

                    Point p = new Point(lat, lng, pressure, temp, 0);
                }
            }
        };
        mGPSSensor.setSensorCallback(callback);

        mGPSSensor.startSensing();
        mBarometerSensor.startSensing();
        mTempSensor.startSensing();
    }

    private void stopRecording() {
        mGPSSensor.stopSensing();
        mBarometerSensor.stopSensing();
        mTempSensor.stopSensing();
    }

}
