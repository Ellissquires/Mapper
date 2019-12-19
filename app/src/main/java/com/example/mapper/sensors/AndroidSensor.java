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

/**
 * Android Sensor is an abstract class representing a single sensor.
 * @author Tom Croasdale
 * @version 1.0
 * @since 1.0
 */
public abstract class AndroidSensor {

    protected String TAG = AndroidSensor.class.getSimpleName();
    // Sampling rates
    protected long mSamplingRateInMS;
    protected long mSamplingRateNano;

    // Hardware sensors.
    private SensorEventListener mListener = null;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private String mSensorType;
    // Variables for timings
    private long lastRebootTime = 0;
    private long READING_FREQUENCY = 20000; //20 seconds in ms
    private long lastReportTime = 0;

    protected SensorEvent mLastResult;
    protected AndroidSensorCallback mSensorCallback;



    /**
     *
     * @param context The Activity this sensor is being used in.
     * @param type The android sensor id
     */
    public AndroidSensor(Context context, int type) {
        lastRebootTime = System.currentTimeMillis() - SystemClock.elapsedRealtime();
        mSamplingRateNano = (long) (READING_FREQUENCY) * 1000000;
        mSamplingRateInMS = (long) READING_FREQUENCY;
        // If type is invalid, don't initialize most values
        if (type > -1) {
            mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
            mSensor = mSensorManager.getDefaultSensor(type);
            if (sensorAvailable()) {
                mSensorType = mSensor.getStringType();
            }
            TAG = mSensorType;
            initListener();
        }

    }

    /**
     * Sets the callback function, which will be used when returning sensor values.
     * @param callback The AndroidSensorCallback object to use.
     */
    public void setSensorCallback(AndroidSensorCallback callback) {
        mSensorCallback = callback;
    }


    /**
     * Initiliases the sensor, and registers listeners.
     */
    protected void initListener() {
        if(!sensorAvailable()) {
            Log.d(TAG, "Sensor type " + mSensorType + " unavailable.");
        } else {
            Log.d(TAG, "Using " + mSensorType);
            mListener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    long diff = event.timestamp - lastReportTime;
                    if (diff >= mSamplingRateNano) {
                        long actualTimeInMS = lastRebootTime + (long) (event.timestamp / 1000000.0);

                        mLastResult = event;
                        // Call the fn with the event and time in ms
                        onSensorChange(event, actualTimeInMS);

                        lastReportTime = event.timestamp;
                        Log.d(TAG, "Listener listening!");
                    }
                }

                @Override
                public void onAccuracyChanged(android.hardware.Sensor sensor, int accuracy) {

                }
            };
        }
    }

    /**
     * Called when the sensor is changed, overridden by subclasses to call specific functions
     * if need be.
     * @param event SensorEvent object, containing sensor data.
     * @param ms The time the Sensor returned values.
     */
    protected abstract void onSensorChange(SensorEvent event, long ms);

    /**
     * Returns the last values returned by the sensor.
     * @return SensorEvent, the last values returned by the listener
     */
    public SensorEvent fetchLastResults () {
        return mLastResult;
    }

    /**
     * This function can be used to see if the object has been initialised, or if this particular
     * sensor is available on the device in use.
     * @return False if the sensor is null.
     */
    public boolean sensorAvailable() {
        return (mSensor != null);
    }

    /**
     * Converts ms to a date
     * @param ms milliseconds since 1/1/1970
     * @return the date string in HH:mm:ss
     */
    public static String mSecsToString(long ms) {
        Date date = new Date(ms);
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        return formatter.format(date);
    }


    /**
     * Starts the sensor, if it's available.
     */
    public void startSensing() {
        if(sensorAvailable()) {
            Log.d(TAG, "starting listener");
            mSensorManager.registerListener(mListener, mSensor, (int) (mSamplingRateInMS * 1000));
        } else {
            Log.i(TAG, mSensorType + " unavailable or in use!");
        }
    }

    /**
     * Stops the sensor
     */
    public void stopSensing() {
        if(sensorAvailable()) {
            Log.d(TAG, "starting listener");
            try {
                mSensorManager.unregisterListener(mListener);
            } catch(Exception e) {
                Log.d(TAG, "Already unregistered?");
            }

        }
    }
}
