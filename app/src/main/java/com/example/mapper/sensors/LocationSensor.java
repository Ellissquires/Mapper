package com.example.mapper.sensors;

import android.app.Activity;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.SensorEvent;
import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.tasks.OnSuccessListener;

public class LocationSensor extends AndroidSensor {

    private LocationRequest mLocationRequest;
    private static boolean mLocationPermissionGranted = true;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;

    private LocationResult mLastResult;


    /**
     * The contrcutor of this class, instantiates required objects.
     * @param context The context of the app.
     */
    public LocationSensor(Context context) {
        super(context, 0);
        TAG = "GPS Location Service";
        mFusedLocationClient = new FusedLocationProviderClient(context);

        // Create a location callback
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult (LocationResult locationResult) {
                super.onLocationResult(locationResult);
                mLastResult = locationResult;
                if (mSensorCallback != null) {
                    mSensorCallback.onSensorCallback(locationResult);
                    mSensorCallback.onSensorCallback(locationResult.getLastLocation());
                }
                Log.d(TAG, locationResult.toString());
            }
        };

        // Create location request object
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(mSamplingRateInMS);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    }

    /**
     * Extra function to get Current Location reading if needed.
     * @param callback
     */
    public void getCurrentLocation(final AndroidSensorCallback callback) {
        mFusedLocationClient.getLastLocation() // Get Last location with fusedLocationClient
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        callback.onSensorCallback(location);
                    }
                });

    }

    /**
     * Starts location updates, make sure permissions are given before this. This function is
     * overriden from the super class. just calls startLocationUpdates()
     */
    @Override
    public void startSensing () {
        if (mLocationPermissionGranted) {
            Log.d(TAG, "Starting location updates");
            startLocationUpdates();
        }
    }

    /**
     * Stops location updates. This function is
     * overriden from the super class. just calls stopLocationUpdates()
     */
    @Override
    public void stopSensing () {
        if (mLocationPermissionGranted) {
            Log.d(TAG, "Stopping location updates");
            stopLocationUpdates();
        }
    }


    /**
     * Fetch the last obtained result.
     * @return Returns the last location recorded by the sensor.
     */
    public LocationResult fetchLastLocation() {
        return mLastResult;
    }


    /**
     * Starts the location updates using mfusedlocationclient.
     */
    private void startLocationUpdates() {
        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                mLocationCallback,
                null);
    }

    /**
     * stops the location updates using mfusedlocationclient.
     */
    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    /**
     * Does Nothing.-
     * @param event
     * @param ms
     */
    @Override
    protected void onSensorChange(SensorEvent event, long ms) {
        Log.d(TAG, "");
    }

}
