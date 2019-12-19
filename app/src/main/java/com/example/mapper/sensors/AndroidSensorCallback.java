package com.example.mapper.sensors;

import android.hardware.SensorEvent;
import android.location.Location;

import com.google.android.gms.location.LocationResult;

/**
 * Class for AndroidSensor callbacks, used mainly for anonymous callback functions.
 * @author Tom Croasdale
 * @version 1.0
 * @since 1.0
 */
public class AndroidSensorCallback {
    public void onSensorCallback(SensorEvent event) {}
    public void onSensorCallback(LocationResult locationResult) {}
    public void onSensorCallback(Location location) {}
}
