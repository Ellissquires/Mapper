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
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mapper.R;
import com.example.mapper.services.LocationFetchService;
import com.example.mapper.services.LocationResultReceiver;
import com.example.mapper.services.PathRecorderService;
import com.example.mapper.services.PathRepository;
import com.example.mapper.services.PointRepository;
import com.example.mapper.services.VisitRepository;
import com.example.mapper.services.models.Point;
import com.example.mapper.services.models.RepoInsertCallback;
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
import java.util.Timer;
import java.util.TimerTask;

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


    private int mElapsedMinutes = 0;
    private int mElapsedSeconds = 0;
    private Timer mTimer;
    private long mPathID;
    private List<Point> mRecordedPoints;

    private PathRepository mPathRepo;
    private PointRepository mPointRepo;
    private VisitRepository mVisitRepo;

    private Visit mVisit;
    private Visit pVisit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        // Make sure app has correct permissions.

        mPathRepo = new PathRepository(getApplication());
        mPointRepo = new PointRepository(getApplication());
        mVisitRepo = new VisitRepository(getApplication());


        Bundle extras = getIntent().getExtras();
        mVisit = extras.getParcelable(EXTRA_VISIT);

        final FloatingActionButton fab_camera = (FloatingActionButton) findViewById(R.id.fab_camera);
        fab_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MapView.this, CameraView.class);
                intent.putExtra(EXTRA_VISIT, mVisit);
                startActivity(intent);
            }
        });

        // Retrieve the visit from the intent
        Log.d(TAG, "Visit ID received " + mVisit.toString());
        ((TextView)findViewById(R.id.final_title)).setText(mVisit.getTitle());
        ((TextView)findViewById(R.id.title)).setText(mVisit.getTitle());


        final FloatingActionButton fab_stop = (FloatingActionButton) findViewById(R.id.fab_stop);
        final ImageButton fab_record = (ImageButton) findViewById(R.id.fab_record);
        final FloatingActionButton fab_pause = (FloatingActionButton) findViewById(R.id.fab_pause);
        final FloatingActionButton fab_resume = (FloatingActionButton) findViewById(R.id.fab_resume);
        fab_record.setVisibility(View.VISIBLE);
        fab_stop.setVisibility(View.GONE);

        fab_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fab_record.setVisibility(View.GONE);
                fab_pause.setVisibility(View.VISIBLE);
                fab_stop.setVisibility(View.VISIBLE);
                fab_camera.setVisibility(View.VISIBLE);
                beginRecording(getApplicationContext());
            }
        });

        fab_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fab_record.setVisibility(View.VISIBLE);
                fab_stop.setVisibility(View.GONE);
                fab_pause.setVisibility(View.GONE);
                fab_resume.setVisibility(View.GONE);
                fab_camera.setVisibility(View.GONE);
                finishRecording(getApplicationContext());
            }
        });

        fab_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PathRecorderService.pauseRecordingService(getApplicationContext());
                fab_pause.setVisibility(View.GONE);
                fab_resume.setVisibility(View.VISIBLE);
                fab_camera.setVisibility(View.GONE);
                stopTimer();
            }
        });

        fab_resume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PathRecorderService.resumeRecordingService(getApplicationContext());
                fab_pause.setVisibility(View.VISIBLE);
                fab_resume.setVisibility(View.GONE);
                fab_camera.setVisibility(View.VISIBLE);
                startTimer();
            }
        });

        // When save button is pressed
        final Button save_button = (Button) findViewById(R.id.save_path_button);
        save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Inserting the path
                mPathRepo.createPath(new RepoInsertCallback(){
                    @Override
                    public void OnFinishInsert(Long rowID) {
                        mPathID = rowID+1;
                        mVisit.setPathId(mPathID);
                        Log.d("MapView", "Inserted path with ID: " + mPathID);
                        // Inserting all points
                        for (Point p : mRecordedPoints) {
                            p.setPathId((int)mPathID);
                            mPointRepo.createPoint(p);
                        }
                        mVisitRepo.createVisit(mVisit);
                        finish();
                    }
                });
            }
        });

        // When save button is pressed
        final Button discard_button = (Button) findViewById(R.id.discard_path_button);
        discard_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
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
        i.setAction(PathRecorderService.ACTION_START);
        context.startService(i);

        findViewById(R.id.pBar).setVisibility(View.VISIBLE);
        startTimer();
    }

    public void startTimer() {
        mTimer = new Timer();    //declare the timer
        mTimer.scheduleAtFixedRate(new TimerTask() { //Set the schedule function and rate
            @Override
            public void run() {
                runOnUiThread(new Runnable() { // Must be on Ui Thread to access Ui
                    @Override
                    public void run() {
                        TextView elapsedTime = (TextView) findViewById(R.id.time_elapsed);
                        elapsedTime.setText(String.format("%d:%02d:%02d", mElapsedMinutes /60, mElapsedMinutes %60, mElapsedSeconds));
                        if (++mElapsedSeconds == 60) {
                            mElapsedSeconds = 0;
                            mElapsedMinutes++;
                        }
                    }
                });
            }
        }, 0L, 1000L);
    }

    public void stopTimer() {
        mTimer.cancel();
    }

    public void finishRecording(Context context) {
        //Tell the service to stop.
        Intent i= new Intent(context, PathRecorderService.class);
        context.stopService(i);
        stopTimer();

        // Set values in card view
        TextView distanceText = (TextView) findViewById(R.id.current_distance);
        TextView temperatureText = (TextView) findViewById(R.id.temperature);
        TextView pressureText = (TextView) findViewById(R.id.pressure);

        TextView finalDistanceText = (TextView) findViewById(R.id.final_distance);
        TextView finalTemperatureText = (TextView) findViewById(R.id.final_temperature);
        TextView finalPressureText = (TextView) findViewById(R.id.final_pressure);
        TextView finalTime = (TextView) findViewById(R.id.time_elapsed);

        findViewById(R.id.final_path_view).setVisibility(View.VISIBLE);

        finalTime.setText(String.format("%d:%02d:%02d", mElapsedMinutes /60, mElapsedMinutes %60, mElapsedSeconds));
        finalTemperatureText.setText(temperatureText.getText());
        finalPressureText.setText(pressureText.getText());
        finalDistanceText.setText(distanceText.getText());

        findViewById(R.id.path_view).setVisibility(View.GONE);
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
        int numpoints = points.toArray().length;
        Point[] pointsarr = new Point[numpoints];
        pointsarr = points.toArray(pointsarr);
        drawPathOnMap(pointsarr);

        // Set values in in the card
        TextView distanceText = (TextView) findViewById(R.id.current_distance);
        TextView temperatureText = (TextView) findViewById(R.id.temperature);
        TextView pressureText = (TextView) findViewById(R.id.pressure);

        // Calculate distance travelled using the distance between points
        double currDist = 0.0;
        Location lastLoc = null;
        for (Point p : points) {
            Location loc = new Location(LocationManager.GPS_PROVIDER);
            loc.setLatitude(p.getLat());
            loc.setLongitude(p.getLng());
            if (lastLoc != null) {
                currDist += loc.distanceTo(lastLoc);
            }
            lastLoc = loc;
        }

        // Set Visit distance
        mVisit.setDistance(currDist);
        // Get temperature and Pressure from last point and set it in the view
        Point refPoint = pointsarr[numpoints-1];
        temperatureText.setText(String.format("%.1f", refPoint.getTemperature()));
        pressureText.setText(String.format("%.1f", refPoint.getPressure()));

        // Set the distance text view.
        if (currDist < 100) {
            distanceText.setText(String.format("%.1f M", currDist));
        } else {
            distanceText.setText(String.format("%.2f KM", currDist / 1000.0));
        }
    }

    /**
     * Callback function for when a new point is added to the recording.
     * @param resultCode Should be 2, if not something has broken. validating this is already done by now
     * @param resultData Bundle containing the list of points.
     */
    @Override
    public void onPathFinish(int resultCode, Bundle resultData) {
        // Get the lsit of points
        mRecordedPoints = resultData.getParcelableArrayList("points");

        // Set values in card view
        TextView distanceText = (TextView) findViewById(R.id.current_distance);
        TextView temperatureText = (TextView) findViewById(R.id.temperature);
        TextView pressureText = (TextView) findViewById(R.id.pressure);

        TextView finalDistanceText = (TextView) findViewById(R.id.final_distance);
        TextView finalTemperatureText = (TextView) findViewById(R.id.final_temperature);
        TextView finalPressureText = (TextView) findViewById(R.id.final_pressure);
        TextView finalTime = (TextView) findViewById(R.id.final_time);

        findViewById(R.id.final_path_view).setVisibility(View.VISIBLE);

        finalTime.setText(String.format("%d:%02d:%02d", mElapsedMinutes /60, mElapsedMinutes %60, mElapsedSeconds));
        finalTemperatureText.setText(temperatureText.getText());
        finalPressureText.setText(pressureText.getText());
        finalDistanceText.setText(distanceText.getText());

        findViewById(R.id.path_view).setVisibility(View.GONE);
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