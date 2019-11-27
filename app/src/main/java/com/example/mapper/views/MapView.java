package com.example.mapper.views;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.example.mapper.R;
import com.example.mapper.sensors.LocationSensor;
import com.example.mapper.services.LocationFetchService;
import com.example.mapper.services.LocationResultReceiver;
import com.example.mapper.services.PathRecorderService;
import com.example.mapper.services.models.Point;
import com.example.mapper.viewmodels.MapViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import pl.aprilapps.easyphotopicker.EasyImage;

public class MapView extends FragmentActivity implements GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener, OnMapReadyCallback, ServiceConnection, LocationResultReceiver.Receiver {
    public static final String EXTRA_VISIT = "com.example.mapper.VISIT";

    private GoogleMap mMap;
    private Polyline currentPath;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;
    private MapViewModel mMapViewModel;

    private LocationResultReceiver mReceiver;
    private PathRecorderService mService;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        EasyImage.configuration(this)
                .setImagesFolderName("EasyImage sample")
                .setCopyTakenPhotosToPublicGalleryAppFolder(true)
                .setCopyPickedImagesToPublicGalleryAppFolder(true)
                .setAllowMultiplePickInGallery(true);

        FloatingActionButton fab_camera = (FloatingActionButton) findViewById(R.id.fab_camera);
        fab_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent = new Intent(MapView.this, CameraView.class);
//                startActivity(intent);
            EasyImage.openCameraForImage(MapView.this, 0);
            }
        });

        String testing = getIntent().getStringExtra(EXTRA_VISIT);


        final FloatingActionButton fab_stop = (FloatingActionButton) findViewById(R.id.fab_stop);
        final FloatingActionButton fab_record = (FloatingActionButton) findViewById(R.id.fab_record);
        fab_record.setVisibility(View.VISIBLE);
        fab_stop.setVisibility(View.GONE);

        fab_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fab_record.setVisibility(View.GONE);
                fab_stop.setVisibility(View.VISIBLE);
            }
        });

        fab_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fab_record.setVisibility(View.VISIBLE);
                fab_stop.setVisibility(View.GONE);
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

    public void drawPathOnMap(Point... points){

        // Clear current path
        if (currentPath != null) { mMap.clear(); }

        // Define line options
        PolylineOptions options = new PolylineOptions().width(5).color(Color.WHITE).geodesic(true);

        // Loop points and add them to the line
        for(Point p : points){
            LatLng mapPoint = new LatLng(p.getLat(), p.getLng());
            options.add(mapPoint);
        }

        // draw line
        currentPath = mMap.addPolyline(options);

    }

    public void drawPathOnMap(int pathId){

        // Clear current path
        if (currentPath != null) { mMap.clear(); }

        // Define line options
        PolylineOptions options = new PolylineOptions().width(5).color(Color.WHITE).geodesic(true);
        List<Point> points = mMapViewModel.getPoints(pathId);

        // Loop points and add them to the line
        for(Point p : points){
            LatLng mapPoint = new LatLng(p.getLat(), p.getLng());
            options.add(mapPoint);
        }

        // draw line
        currentPath = mMap.addPolyline(options);

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