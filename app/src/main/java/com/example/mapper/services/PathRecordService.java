package com.example.mapper.services;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.hardware.SensorEvent;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.mapper.sensors.AndroidSensorCallback;
import com.example.mapper.sensors.BarometerSensor;
import com.example.mapper.sensors.LocationSensor;
import com.example.mapper.sensors.TemperatureSensor;
import com.example.mapper.services.models.Point;

public class PathRecordService extends IntentService {

    private LocationSensor mGPSSensor;
    private BarometerSensor mBarometerSensor;
    private TemperatureSensor mTempSensor;


    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public PathRecordService(String name) {
        super(name);


        mGPSSensor = new LocationSensor(getApplicationContext());
        mBarometerSensor = new BarometerSensor(getApplicationContext());
        mTempSensor = new TemperatureSensor(getApplicationContext());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

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

                Point p = new Point(lat, lng, pressure, temp, 0);
            }
            }
        };
        mGPSSensor.setSensorCallback(callback);

        mGPSSensor.startSensing();
        mBarometerSensor.startSensing();
        mTempSensor.startSensing();

        return;
    }

    private void beginRecording


}
