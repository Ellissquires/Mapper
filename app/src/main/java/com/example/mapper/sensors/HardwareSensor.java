package com.example.mapper.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public abstract class HardwareSensor {
    protected String TAG = HardwareSensor.class.getSimpleName();
    private long mSamplingRateInMS;
    private long mSamplingRateNano;
    private SensorEventListener mListener = null;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private long lastRebootTime = 0;
    private long BAROMETER_READING_FREQUENCY = 30000;
    private long lastReportTime = 0;
    private String mSensorType;

    public HardwareSensor (Context context, int type) {
        lastRebootTime = System.currentTimeMillis() - SystemClock.elapsedRealtime();
        mSamplingRateNano = (long) (BAROMETER_READING_FREQUENCY) * 1000000;
        mSamplingRateInMS = (long) BAROMETER_READING_FREQUENCY;
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(type);
        mSensorType = mSensor.getStringType();
        TAG = mSensorType;
        initListener();
    }


    protected void initListener() {
        if(!standardSensorAvailable()) {
            Log.d(TAG, "Sensor type " + mSensorType + " unavailable.");
        } else {
            Log.d(TAG, "Using " + mSensorType);
            mListener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    long diff = event.timestamp - lastReportTime;
                    if (diff >= mSamplingRateNano) {
                        long actualTimeInMS = lastRebootTime + (long) (event.timestamp / 1000000.0);

                        onSensorChange(event, actualTimeInMS);

                        lastReportTime = event.timestamp;
                    }
                }

                @Override
                public void onAccuracyChanged(android.hardware.Sensor sensor, int accuracy) {

                }
            };
        }
    }

    protected abstract void onSensorChange(SensorEvent event, long ms);

    protected boolean standardSensorAvailable () {
        return (mSensor != null);
    }

    public static String mSecsToString(long ms) {
        Date date = new Date(ms);
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        return formatter.format(date);
    }


    public void startSensing() {
        if(standardSensorAvailable()) {
            Log.d(TAG, "starting listener");
            mSensorManager.registerListener(mListener, mSensor, (int) (mSamplingRateInMS * 1000));
        } else {
            Log.i(TAG, mSensorType + " unavailable or in use!");
        }
    }

    public void stopSensing() {
        if(standardSensorAvailable()) {
            Log.d(TAG, "starting listener");
            try {
                mSensorManager.unregisterListener(mListener);
            } catch(Exception e) {
                Log.d(TAG, "Already unregistered?");
            }

        }
    }
}
