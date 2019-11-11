package com.example.mapper.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;
import android.util.Log;

import com.google.android.material.snackbar.Snackbar;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Barometer {
    private static final String TAG = Barometer.class.getSimpleName();
    private long mSamplingRateInMSecs;
    private long mSamplingRateNano;
    private SensorEventListener mPressureListener = null;
    private SensorManager mSensorManager;
    private Sensor mBarometerSensor;
    private long lastRebootTime = 0;
    private long BAROMETER_READING_FREQUENCY = 30000;
    private long lastReportTime = 0;

    public Barometer (Context context) {
        lastRebootTime = System.currentTimeMillis() - SystemClock.elapsedRealtime();
        mSamplingRateNano = (long) (BAROMETER_READING_FREQUENCY) * 1000000;
        mSamplingRateInMSecs = (long) BAROMETER_READING_FREQUENCY;
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mBarometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        initBarometerListener();
    }

    private void initBarometerListener() {
        if(!standardPressureSensorAvailable()) {
            Log.d(TAG, "Standard barometer unavailable.");
        } else {
            Log.d(TAG, "Using Barometer");
            mPressureListener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    long diff = event.timestamp - lastReportTime;
                     if (diff >= mSamplingRateNano) {
                         long actualTimeInMS = lastRebootTime + (long) (event.timestamp / 1000000.0);
                         float pressureValue = event.values[0];
                         int accuracy = event.accuracy;

                         Log.i(TAG, mSecsToString(actualTimeInMS) + ": current pressure:  " +
                                 pressureValue + ": accuracy: "+ accuracy);

                         lastReportTime = event.timestamp;
                     }
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {

                }
            };
        }
    }

    public String mSecsToString(long ms) {
        Date date = new Date(ms);
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        return formatter.format(date);
    }

    public boolean standardPressureSensorAvailable() {
        return (mBarometerSensor != null);
    }

    public void startSensingPressure() {
        if(standardPressureSensorAvailable()) {
            Log.d(TAG, "starting listener");
            mSensorManager.registerListener(mPressureListener, mBarometerSensor, (int) (mSamplingRateInMSecs * 1000));
        } else {
            Log.i(TAG, "barometer unavailable or in use!");
        }
    }

    public void stopBarometer() {
        if(standardPressureSensorAvailable()) {
            Log.d(TAG, "starting listener");
            try {
                mSensorManager.unregisterListener(mPressureListener);
            } catch(Exception e) {
                Log.d(TAG, "Already unregistered?");
            }

        }
    }
}

