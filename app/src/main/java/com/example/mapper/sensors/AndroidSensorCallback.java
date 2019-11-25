package com.example.mapper.sensors;

import android.hardware.SensorEvent;
import android.location.Location;

import com.google.android.gms.location.LocationResult;

/**
 * Class for AndroidSensor callbacks, used mainly for anonymous functions.
 */
public class AndroidSensorCallback {
    public void onSensorCallback(SensorEvent event) {}
    public void onSensorCallback(LocationResult locationResult) {}
    public void onSensorCallback(Location location) {}
}
