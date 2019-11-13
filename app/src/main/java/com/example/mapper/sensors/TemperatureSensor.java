package com.example.mapper.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.util.Log;


public class TemperatureSensor extends AndroidSensor {
    public TemperatureSensor(Context context) {
        super(context, Sensor.TYPE_AMBIENT_TEMPERATURE);
    }

    @Override
    protected void onSensorChange(SensorEvent event, long ms) {
        float pressureValue = event.values[0];
        int accuracy = event.accuracy;

        Log.i(super.TAG, mSecsToString(ms) + ": current pressure:  " + pressureValue + ": accuracy: "+ accuracy);
    }
}

