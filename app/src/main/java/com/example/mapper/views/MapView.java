package com.example.mapper.views;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.view.View;
import android.widget.Toast;

import com.example.mapper.sensors.LocationSensor;
import com.example.mapper.services.LocationFetchService;
import com.example.mapper.services.LocationResultReceiver;
import com.example.mapper.services.PathRecorderService;
import com.example.mapper.services.PathRepository;
import com.example.mapper.services.PointRepository;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;


import com.example.mapper.R;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


public class MapView extends FragmentActivity implements GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener, OnMapReadyCallback, ServiceConnection, LocationResultReceiver.Receiver {

    private GoogleMap mMap;

    private LocationResultReceiver mReceiver;

    private PathRepository mPathDB;
    private PointRepository mPointDB;

    private PathRecorderService mService;


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
                beginRecording(view.getContext());
            }
        });

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mReceiver = new LocationResultReceiver(new Handler());

    }

    /**
     * Tells the Path Recorder service to start.
     */
    public void beginRecording (Context context) {

        // Make sure app has correct permissions.
        LocationSensor.fetchPermission(context);
        // Start PathRecorderService
        Intent i= new Intent(context, PathRecorderService.class);
        context.startService(i);
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

        // Make sure app has correct permissions.
        LocationSensor.fetchPermission(this);
        getCurrentLocation();

        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
    }

    /**
     * Starts the IntentService to get the current location, and attaches this class as the result receiver.
     */
    public void getCurrentLocation(){
        mReceiver.setReceiver(this);
        LocationFetchService.startActionGetLocation(this, mReceiver);
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

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        PathRecorderService.PRSBinder b  = (PathRecorderService.PRSBinder) binder;
        mService = b.getService();
    }


    @Override
    public void onServiceDisconnected(ComponentName name) {
        mService = null;
    }

    /**
     * When the location result is received, this is called.
     * @param resultCode Whether it has been successful or not.
     * @param bundle The bundle of data sent from the FetchService Thread.
     */
    @Override
    public void onReceiveResult(int resultCode, Bundle bundle) {
        LatLng currentLoc = new LatLng(bundle.getDouble("lat"), bundle.getDouble("lng"));
        mMap.addMarker(new MarkerOptions()
                .position(currentLoc).title("Hi There")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLoc));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLoc, 16.0f));
    }
}