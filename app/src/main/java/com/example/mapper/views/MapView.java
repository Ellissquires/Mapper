package com.example.mapper.views;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.app.Activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
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
import com.example.mapper.services.models.Visit;
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

import java.util.ArrayList;
import java.util.List;

public class MapView extends FragmentActivity implements GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener, OnMapReadyCallback, ServiceConnection, LocationResultReceiver.Receiver {
    public static final String EXTRA_VISIT = "com.example.mapper.VISIT";

    private GoogleMap mMap;
    private Polyline currentPath;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted = false;
    private MapViewModel mMapViewModel;
    private static final String TAG = "MapViewActivity";
    private LocationResultReceiver mReceiver;
    private PathRecorderService mService;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        // Make sure app has correct permissions.

        FloatingActionButton fab_camera = (FloatingActionButton) findViewById(R.id.fab_camera);
        fab_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MapView.this, CameraView.class);
                startActivity(intent);
            }
        });

        // Retrieve the visit from the intent
        Bundle extras = getIntent().getExtras();
        Visit visit = (Visit) extras.getParcelable(EXTRA_VISIT);
        Log.d(TAG, "Visit ID received " + visit.toString());



        final FloatingActionButton fab_stop = (FloatingActionButton) findViewById(R.id.fab_stop);
        final FloatingActionButton fab_record = (FloatingActionButton) findViewById(R.id.fab_record);
        fab_record.setVisibility(View.VISIBLE);
        fab_stop.setVisibility(View.GONE);

        fab_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fab_record.setVisibility(View.GONE);
                fab_stop.setVisibility(View.VISIBLE);
                beginRecording(getApplicationContext());
            }
        });

        fab_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fab_record.setVisibility(View.VISIBLE);
                fab_stop.setVisibility(View.GONE);
                finishRecording(getApplicationContext());
            }
        });

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mReceiver = new LocationResultReceiver(new Handler());
        fetchPermission(this);
    }

    /**
     * Tells the Path Recorder service to start.
     */
    public void beginRecording (Context context) {

        // Make sure app has correct permissions.
        fetchPermission(context);
        // Start PathRecorderService
        Intent i= new Intent(context, PathRecorderService.class);
        i.putExtra("receiverTag", mReceiver);
        context.startService(i);
    }

    public void finishRecording(Context context) {
        //Tell the service to stop.
        Intent i= new Intent(context, PathRecorderService.class);
        context.stopService(i);
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


        // If permissions have not yet been granted, these lines get called when they are.
        if (mLocationPermissionGranted) {
            setUpMapPostPermissionCheck();
        }
    }

    public void setUpMapPostPermissionCheck() {
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

    /**
     * Callback function for when a new point is added to the recording.
     * @param resultCode Should be 1, if not something has broken validating this is already done by now
     * @param resultData Bundle containing the list of points.
     */
    @Override
    public void onPathPut(int resultCode, Bundle resultData) {
        ArrayList<Point> points = resultData.getParcelableArrayList("points");
        Point[] pointsarr = new Point[points.toArray().length];
        pointsarr = points.toArray(pointsarr);
        drawPathOnMap(pointsarr);
    }

    /**
     * Callback for when permissions are granted/denied.
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                    setUpMapPostPermissionCheck();
                }
            }
        }
    }

    /**
     * Asks the user for location permission.
     * @param context
     */
    public void fetchPermission (Context context) {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions((Activity)context, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }
}