package com.example.mapper.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.util.Log;


public class BarometerSensor extends AndroidSensor {
    public BarometerSensor(Context context) {
        super(context, Sensor.TYPE_PRESSURE);
    }

    /**
     * Calls the sensor registered AndroiedSensorCallback if there is one.
     * @param event
     * @param ms
     */
    @Override
    protected void onSensorChange(SensorEvent event, long ms) {
        if (mSensorCallback != null) {
            mSensorCallback.onSensorCallback(event);
        }
    }
}

