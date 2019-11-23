package com.example.mapper.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.hardware.SensorEvent;
import android.location.Location;

import com.example.mapper.sensors.AndroidSensorCallback;
import com.example.mapper.sensors.BarometerSensor;
import com.example.mapper.sensors.LocationSensor;
import com.example.mapper.sensors.TemperatureSensor;
import com.example.mapper.services.models.Point;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class PathRecorderService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_FOO = "com.example.mapper.services.action.FOO";
    private static final String ACTION_BAZ = "com.example.mapper.services.action.BAZ";
    private static final String ACTION_START_RECORDING = "com.example.mapper.services.action.START_RECORDING";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "com.example.mapper.services.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "com.example.mapper.services.extra.PARAM2";

    private LocationSensor mGPSSensor;
    private BarometerSensor mBarometerSensor;
    private TemperatureSensor mTempSensor;

    public PathRecorderService() {
        super("PathRecorderService");

        mGPSSensor = new LocationSensor(getApplicationContext());
        mBarometerSensor = new BarometerSensor(getApplicationContext());
        mTempSensor = new TemperatureSensor(getApplicationContext());
    }

    public static void setActionStartRecording(Context context) {
        Intent intent = new Intent(context, PathRecorderService.class);
        intent.setAction(ACTION_FOO);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionFoo(Context context, String param1, String param2) {
        Intent intent = new Intent(context, PathRecorderService.class);
        intent.setAction(ACTION_FOO);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionBaz(Context context, String param1, String param2) {
        Intent intent = new Intent(context, PathRecorderService.class);
        intent.setAction(ACTION_BAZ);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_FOO.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionFoo(param1, param2);
            } else if (ACTION_BAZ.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionBaz(param1, param2);
            } else if (ACTION_START_RECORDING.equals(action)) {
                handleActionStartRecording();
            }
        }
    }

    private void handleActionStartRecording() {
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

    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo(String param1, String param2) {
        // TODO: Handle action Foo
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBaz(String param1, String param2) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
