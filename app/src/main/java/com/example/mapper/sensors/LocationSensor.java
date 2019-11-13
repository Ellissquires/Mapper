package com.example.mapper.sensors;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.SensorEvent;
import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.tasks.OnSuccessListener;

public class LocationSensor extends AndroidSensor {

    private LocationRequest mLocationRequest;
    private boolean mLocationPermissionGranted = false;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;


    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;

    private LocationResult mLastResult;

    public LocationSensor(Context context, Activity activity) {
        super(context, 0);
        TAG = "GPS Location Service";

        mFusedLocationClient = new FusedLocationProviderClient(context);
        mFusedLocationClient.getLastLocation() // Get Last location with fusedLocationClient
                .addOnSuccessListener(activity, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            Log.d(TAG, location.toString());
                        }
                    }
            });

        // Create a location callback
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult (LocationResult locationResult) {
                super.onLocationResult(locationResult);
                mLastResult = locationResult;
                Log.d(TAG, locationResult.toString());
            }
        };

        // Create location request object
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(mSamplingRateInMS);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        // Make sure app has correct permissions.
        fetchPermission(context, activity);
    }

    @Override
    public void startSensing () {
        if (mLocationPermissionGranted) {
            Log.d(TAG, "Starting location updates");
            startLocationUpdates();
        }
    }

    @Override
    public void stopSensing () {
        if (mLocationPermissionGranted) {
            Log.d(TAG, "Stopping location updates");
            stopLocationUpdates();
        }
    }


    // Fetch the last obtained result.
    public LocationResult fetchLastLocation() {
        return mLastResult;
    }


    /**
     * Callback for when permissions are granted/denied.
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
    }

    /**
     * Asks the user for location permission.
     * @param context
     * @param activity
     */
    public void fetchPermission (Context context, Activity activity) {
        if (ContextCompat.checkSelfPermission(context,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(activity,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Starts the location updatesnusing mfusedlocationclient.
     */
    private void startLocationUpdates() {
        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                mLocationCallback,
                null);
    }

    /**
     * stops the location updatesnusing mfusedlocationclient.
     */
    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    /**
     * Does Nothing.
     * @param event
     * @param ms
     */
    @Override
    protected void onSensorChange(SensorEvent event, long ms) {
        Log.d(TAG, "");
    }
}
