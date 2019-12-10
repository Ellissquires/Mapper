package com.example.mapper.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;

import com.example.mapper.sensors.AndroidSensorCallback;
import com.example.mapper.sensors.LocationSensor;

import javax.xml.transform.Result;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * helper methods.
 */
public class LocationFetchService extends IntentService {
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_GET_LOCATION = "com.example.mapper.services.action.GET_LOCATION";


    private LocationSensor mGPSSensor;


    public LocationFetchService() {
        super("LocationFetchService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionGetLocation(Context context, ResultReceiver reciever) {
        Intent intent = new Intent(context, LocationFetchService.class);
        intent.setAction(ACTION_GET_LOCATION);
        intent.putExtra("receiverTag", reciever);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_GET_LOCATION.equals(action)) {
                handleActionGetLocation((ResultReceiver) intent.getParcelableExtra("receiverTag"));
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionGetLocation(final ResultReceiver res) {

        mGPSSensor = new LocationSensor(this);
        mGPSSensor.getCurrentLocation(new AndroidSensorCallback() {
            @Override
            public void onSensorCallback(Location location) {
                Bundle b = new Bundle();
                b.putDouble("lat", location.getLatitude());
                b.putDouble("lng", location.getLongitude());
                b.putDouble("alt", location.getAltitude());
                res.send(0, b);
                return;
            }
        });
        

    }

}
