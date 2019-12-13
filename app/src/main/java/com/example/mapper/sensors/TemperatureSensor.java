package com.example.mapper.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.util.Log;


public class TemperatureSensor extends AndroidSensor {
    public TemperatureSensor(Context context) {
        super(context, Sensor.TYPE_AMBIENT_TEMPERATURE);
    }

    /**
     * Called when the sensor is queried. No need to really call this function really.
     * This function calls the registered callback function if there is one.
     * @param event The SensorEvent object holding the sensor data being passed through.
     * @param ms The time in ms that the sensor returned values.
     */
    @Override
    protected void onSensorChange(SensorEvent event, long ms) {
        if (mSensorCallback != null) {
            mSensorCallback.onSensorCallback(event);
        }
    }
}

