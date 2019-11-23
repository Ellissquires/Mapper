package com.example.mapper.views;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.hardware.SensorEvent;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.mapper.sensors.AndroidSensorCallback;
import com.example.mapper.sensors.BarometerSensor;
import com.example.mapper.sensors.LocationSensor;
import com.example.mapper.sensors.TemperatureSensor;
import com.example.mapper.services.PathRepository;
import com.example.mapper.services.PointRepository;
import com.example.mapper.services.models.Path;
import com.example.mapper.services.models.Point;
import com.example.mapper.services.models.PointDAO;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;


import com.example.mapper.R;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


public class MapView extends FragmentActivity implements GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener, OnMapReadyCallback {

    private GoogleMap mMap;

    private LocationSensor mGPSSensor;
    private BarometerSensor mBarometerSensor;
    private TemperatureSensor mTempSensor;

    private PathRepository mPathDB;
    private PointRepository mPointDB;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        FloatingActionButton fab_camera = (FloatingActionButton) findViewById(R.id.fab_camera);
        fab_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

        mPathDB = new PathRepository(this.getApplication());
        mPointDB = new PointRepository(this.getApplication());

        FloatingActionButton fab_record = (FloatingActionButton) findViewById(R.id.fab_record);
        fab_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                beginRecording();
            }
        });

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        mGPSSensor = new LocationSensor(this, this);
        mBarometerSensor = new BarometerSensor(this);
        mTempSensor = new TemperatureSensor(this);
    }

    /**
     * Starts recording the map and stores it as points in the database.
     */
    public void beginRecording () {
        Path path = new Path();
        final long pathID = mPathDB.createPath(path);


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

                Point p = new Point(lat, lng, pressure, temp, pathId);
                mPointDB.createPoint(p);
            }
            }
        };
        mGPSSensor.setSensorCallback(callback);

        mGPSSensor.startSensing();
        mBarometerSensor.startSensing();
        mTempSensor.startSensing();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;

        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.map_style));

            if (!success) {
            }
        } catch (Resources.NotFoundException e) {
        }

        getCurrentLocation();

        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
    }

    public void getCurrentLocation(){
        mGPSSensor.getCurrentLocation(new AndroidSensorCallback() {
            @Override
            public void onSensorCallback (Location location) {
            if (location != null) {
                LatLng currentLoc = new LatLng( location.getLatitude(),location.getLongitude());
                mMap.addMarker(new MarkerOptions()
                        .position(currentLoc).title("Hi There")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLoc));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLoc, 16.0f));
            }
            }
        });
    }


    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mGPSSensor.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\n" + location.getLongitude() + location.getLatitude(), Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }
}