package com.example.mapper.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.util.Log;


public class BarometerSensor extends HardwareSensor{
    public BarometerSensor(Context context) {
        super(context, Sensor.TYPE_PRESSURE);
    }

    @Override
    protected void onSensorChange(SensorEvent event, long ms) {
        float pressureValue = event.values[0];
        int accuracy = event.accuracy;

        Log.i(super.TAG, mSecsToString(ms) + ": current temp:  " + pressureValue + ": accuracy: "+ accuracy);
    }
}

