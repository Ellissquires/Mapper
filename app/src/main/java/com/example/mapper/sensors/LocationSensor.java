package com.example.mapper.sensors;

import android.content.Context;
import android.hardware.SensorEvent;
import android.util.Log;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;

public class LocationSensor extends HardwareSensor {

    private LocationRequest mLocationRequest;

    public LocationSensor(Context context) {
        super(context, 0, true);

        mLocationRequest.setInterval(mSamplingRateInMS);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        startLocationUpdates();
    }

    @Override
    protected void onSensorChange(SensorEvent event, long ms) {

    }

    private void startLocationUpdates() {
        LocationCallback mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult (LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Log.d(TAG, locationResult.toString());
            }
        };

        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                mLocationCallback,
                null);
    }
}
