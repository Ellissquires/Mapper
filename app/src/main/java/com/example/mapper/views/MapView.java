package com.example.mapper.views;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.example.mapper.services.ImageFetchService;
import com.example.mapper.services.PathRecorder.LocationFetchService;
import com.example.mapper.services.PathRecorder.LocationResultReceiver;
import com.example.mapper.services.PathRecorder.PathRecorderService;
import com.example.mapper.services.PathRepository;
import com.example.mapper.services.PicturePointRepository;
import com.example.mapper.services.PointRepository;
import com.example.mapper.services.VisitRepository;
import com.example.mapper.services.models.PicturePoint;
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
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Ellis Squires
 * @author Tom Croasdale
 * @author Neville Kitala
 * @version 1.0
 * @since 1.0
 */
public class MapView extends FragmentActivity implements GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener, OnMapReadyCallback, ServiceConnection, LocationResultReceiver.Receiver {
    public static final String EXTRA_VISIT = "com.example.mapper.VISIT";


    private GoogleMap mMap;
    private Polyline currentPath;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted = false;
    private boolean mStarted = false;
    private boolean stopped = false;
    private MapViewModel mMapViewModel;
    private static final String TAG = "MapViewActivity";
    private LocationResultReceiver mReceiver;
    private PathRecorderService mService;
    private int mElapsedMinutes = 0;
    private int mElapsedSeconds = 0;
    private float mStartTime = 0;
    private Timer mTimer;
    private long mPathID;
    private List<Point> mRecordedPoints;
    private PathRepository mPathRepo;
    private PointRepository mPointRepo;
    private VisitRepository mVisitRepo;
    private PicturePointRepository mPictPointRepo;
    private MaterialCardView card;
    private CardView menu_card;

    private Visit mVisit;

    private Map<String, String> pointToPictureDict;

    /**
     * A default method for every class that extends AppCompatActivity that allows it to define
     * which layout it will use, as well as how define and to manipulate its contents
     * This method makes use of the activity_map layout.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        // Make sure app has correct permissions.
        fetchPermission(this);

        mReceiver = new LocationResultReceiver(new Handler());
        mPathRepo = new PathRepository(getApplication());
        mPointRepo = new PointRepository(getApplication());
        mVisitRepo = new VisitRepository(getApplication());
        mPictPointRepo = new PicturePointRepository(getApplication());
        pointToPictureDict = new HashMap<>();

        // If the visit has not been passed through the context,
        // The the service is probably storing it for use.
        // This is used when the app is closed mid-way through recording.
        // Data needed to display it as it was before the close is given to the service,
        // and here we get it back from the service.
        Bundle extras = getIntent().getExtras();
        if (getIntent().hasExtra(EXTRA_VISIT)) {
            mVisit = extras.getParcelable(EXTRA_VISIT);
        } else {
            PathRecorderService.fetchVisitService(getApplicationContext(), mReceiver);
        }

        final FloatingActionButton fab_camera = findViewById(R.id.fab_camera);
        fab_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MapView.this, CameraView.class);
                intent.putExtra(EXTRA_VISIT, mVisit);
                // start activity for result, because we want the filename back.
                startActivityForResult(intent, 1);
            }
        });

        // Retrieve the visit from the intent
        if (mVisit != null) {
            ((TextView) findViewById(R.id.final_title)).setText(mVisit.getTitle());
            ((TextView) findViewById(R.id.title)).setText(mVisit.getTitle());
        }

        final FloatingActionButton fab_stop = findViewById(R.id.fab_stop);
        final ImageButton fab_record = findViewById(R.id.fab_record);
        final FloatingActionButton fab_pause = findViewById(R.id.fab_pause);
        final FloatingActionButton fab_resume = findViewById(R.id.fab_resume);
        menu_card = findViewById(R.id.map_menu);

        if(mLocationPermissionGranted) {
            //starting the visit immediately the map starts
            fab_record.setVisibility(View.GONE);
            fab_pause.setVisibility(View.VISIBLE);
            fab_stop.setVisibility(View.GONE);
            fab_camera.setVisibility(View.VISIBLE);
            beginRecording(getApplicationContext());
        }
        else{
            fab_record.setVisibility(View.VISIBLE);
            fab_pause.setVisibility(View.GONE);
            fab_stop.setVisibility(View.GONE);
        }

        fab_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fab_record.setVisibility(View.GONE);
                fab_pause.setVisibility(View.VISIBLE);
                fab_stop.setVisibility(View.GONE);
                fab_camera.setVisibility(View.VISIBLE);
                beginRecording(getApplicationContext());
            }
        });

        fab_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fab_record.setVisibility(View.GONE);
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
                fab_stop.setVisibility(View.VISIBLE);
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
                fab_stop.setVisibility(View.GONE);
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
                        mPathID = rowID;
                        mVisit.setPathId(mPathID);
                        // Inserting all points
                        for (int i = 0; i < mRecordedPoints.size(); i++) {
                            Point p  = mRecordedPoints.get(i);
                            p.setPathId((int)mPathID);

                            // Check to see if this point has an associated picture point.
                            // If it does, create a picture point with the associated point Id
                            if (!pointToPictureDict.containsKey("" + i)) {
                                // no picture point
                                mPointRepo.createPoint(p);
                            } else {
                                // there is a picture point, insert it with the file path.
                                final String index = "" + i;
                                mPointRepo.createPoint(p, new RepoInsertCallback() {
                                    @Override
                                    public void OnFinishInsert(Long rowID) {
                                        long pointID = rowID;

                                        String fn = pointToPictureDict.get(index);
                                        PicturePoint pp = new PicturePoint((int)pointID, fn);
                                        mPictPointRepo.createPicturePoint(pp);
                                    }
                                });
                            }
                        }
                        mVisitRepo.createVisit(mVisit);
                        finish();
                    }
                });
            }
        });

        // When save button is pressed
        final Button discard_button =  findViewById(R.id.discard_path_button);
        discard_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageFetchService.deleteImageFolder(mVisit.getTitle(), MapView.this);
                finish();
            }
        });

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        card = findViewById(R.id.path_view);
    }

    /**
     * Called when camera view returns, with either cancel or a new picture
     * @param requestCode should be 1
     * @param resultCode RESULT_OK or RESULT_CANCELLED
     * @param data An Intent with returned filename bundled inside it.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){;
                Bundle b = data.getExtras();
                ArrayList<String> fileNames = b.getStringArrayList("filename");
                for(String filepath: fileNames){

                    Point lastPoint = mRecordedPoints.get(mRecordedPoints.size() - 1);
                    pointToPictureDict.put("" + (mRecordedPoints.size() - 1), filepath);
                }

            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }

    /**
     * Tells the Path Recorder service to start.
     */
    public void beginRecording (Context context) {

        // Make sure app has correct permissions.
        fetchPermission(context);
        mStarted = true;
        // Start PathRecorderService
        Intent i= new Intent(context, PathRecorderService.class);
        i.putExtra("receiverTag", mReceiver);
        i.setAction(PathRecorderService.ACTION_START);
        context.startService(i);

        // Give the service the visit for persistence
        PathRecorderService.postVisitService(context, mVisit);
        startTimer();
    }

    /**
     * Starts a timer for the elapsed time UI Element.
     */
    public void startTimer() {
        mTimer = new Timer();    //declare the timer
        mTimer.scheduleAtFixedRate(new TimerTask() { //Set the schedule function and rate
            @Override
            public void run() {
                runOnUiThread(new Runnable() { // Must be on Ui Thread to access Ui
                    @Override
                    public void run() {
                        TextView elapsedTime = findViewById(R.id.time_elapsed);
                        elapsedTime.setText(String.format(Locale.ENGLISH,"%d:%02d:%02d", mElapsedMinutes /60, mElapsedMinutes %60, mElapsedSeconds));
                        if (++mElapsedSeconds == 60) {
                            mElapsedSeconds = 0;
                            mElapsedMinutes++;
                        }
                    }
                });
            }
        }, 0L, 1000L);
    }

    /**
     * Stops the timer.
     */
    public void stopTimer() {
        if (mTimer != null) {
            mTimer.cancel();
        }
    }

    /**
     * returns a string of image paths that have been collected from all the new images added in this activity
     * to the previous activity. This overrides the method onBackPressed(), that typically just returns to the
     * previous activity
     */
    @Override
    public void onBackPressed()
    {
        if(mStarted && !stopped) {
            menu_card.setVisibility(View.GONE);
            card.setVisibility(View.GONE);
            finishRecording(getApplicationContext());
            stopped = false;
        }
        if(!mStarted){
            super.onBackPressed();
        }
    }

    /**
     * Ends the recording, stops the timer and sets up Ui Values in the save/disccard box.
     * @param context
     */
    public void finishRecording(Context context) {
        //Tell the service to stop.
        PathRecorderService.stopRecordingService(context);
        stopTimer();

        // Set values in card view
        TextView distanceText = findViewById(R.id.current_distance);
        TextView temperatureText = findViewById(R.id.temperature);
        TextView pressureText = findViewById(R.id.pressure);

        TextView finalDistanceText = findViewById(R.id.final_distance);
        TextView finalTemperatureText = findViewById(R.id.final_temperature);
        TextView finalPressureText = findViewById(R.id.final_pressure);
        TextView finalTime = findViewById(R.id.time_elapsed);

        findViewById(R.id.final_path_view).setVisibility(View.VISIBLE);

        finalTime.setText(String.format(Locale.ENGLISH,"%d:%02d:%02d", mElapsedMinutes /60, mElapsedMinutes %60, mElapsedSeconds));
        finalTemperatureText.setText(temperatureText.getText());
        finalPressureText.setText(pressureText.getText());
        finalDistanceText.setText(distanceText.getText());

        findViewById(R.id.path_view).setVisibility(View.GONE);
    }

    /*
        Google map initialisation
     */
    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        // Hide the path information card on map movement
        mMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int reason) {
                card.animate().translationY(-700);
            }
        });

        // Unhide the path information when the map is idle
        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                card.animate().translationY(0);
            }
        });
         mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.map_style));

        // If permissions have not yet been granted, these lines get called when they are.
        if (mLocationPermissionGranted) {
            setUpMapPostPermissionCheck();
        }
    }

    /**
     * Check for the map permissions after the map has loaded.
     */
    public void setUpMapPostPermissionCheck() {
        getCurrentLocation();

        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
    }

    /**
     * Gets the points passed in as parameters, and looks for an association between that point and the
     * the images taken during the visit. If the point the images was taken is equal to the current point
     * then an image marker is drawn.
     * It also uses a Google provided poly line to draw lines between the images to form a path.
     * @param points
     */
    public void drawPathOnMap(Point... points){

        // Clear current path
        if (currentPath != null) { mMap.clear(); }

        // Define line options
        PolylineOptions options = new PolylineOptions().width(15).color(Color.BLUE).geodesic(true);

        // Loop points and add them to the line
        // for(Point p : points){
        for(int i = 0; i < points.length; i ++) {
            Point p = points[i];
            LatLng mapPoint = new LatLng(p.getLat(), p.getLng());
            options.add(mapPoint);

            if(i == 0){
                Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.map_start_pin);
                bitmap = ImageFetchService.getIcon(bitmap, 200);
                mMap.addMarker(new MarkerOptions()
                        .position(mapPoint)
                        .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                        .title("Start"));
            }
            else if(i == (points.length -1)){
                Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.map_finish_pin);
                bitmap = ImageFetchService.getIcon(bitmap, 200);
                mMap.addMarker(new MarkerOptions()
                        .position(mapPoint)
                        .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                        .title("Finish"));
            }

            if (pointToPictureDict.containsKey("" + i)) {
                Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.map_pin);
                bitmap = ImageFetchService.getIcon(bitmap, 150);
                mMap.addMarker(new MarkerOptions()
                        .position(mapPoint)
                        .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                        .title("Photo"));
            }
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

    /**
     * What happens when my current location is clicked.
     * @param location
     */
    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\n" + location.getLongitude() + location.getLatitude(), Toast.LENGTH_LONG).show();
    }

    /**
     * What happens when the get_my_current_location button is clicked.
     */
    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    /**
     * Initialise the path recorder service and binds it to the service.
     * @param name
     * @param binder
     */
    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        PathRecorderService.PRSBinder b  = (PathRecorderService.PRSBinder) binder;
        mService = b.getService();
    }


    /*
        Nullifies the current service once its disconnected
     */
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
        ArrayList<Point> points = resultData.getParcelableArrayList(PathRecorderService.PATH_TAG);
        int numpoints = points.toArray().length;
        Point[] pointsarr = new Point[numpoints];
        pointsarr = points.toArray(pointsarr);
        mRecordedPoints = points;
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
        // Get the list of points, and elapsed time
        mRecordedPoints = resultData.getParcelableArrayList(PathRecorderService.PATH_TAG);
        long mElapsedTime = resultData.getLong(PathRecorderService.TIME_TAG);


        // Set values in card view
        TextView distanceText = (TextView) findViewById(R.id.current_distance);
        TextView temperatureText = (TextView) findViewById(R.id.temperature);
        TextView pressureText = (TextView) findViewById(R.id.pressure);

        TextView finalDistanceText = (TextView) findViewById(R.id.final_distance);
        TextView finalTemperatureText = (TextView) findViewById(R.id.final_temperature);
        TextView finalPressureText = (TextView) findViewById(R.id.final_pressure);
        TextView finalTime = (TextView) findViewById(R.id.final_time);

        findViewById(R.id.final_path_view).setVisibility(View.VISIBLE);

        long seconds = mElapsedTime % 60;
        long hours = mElapsedTime / 60;
        long minutes = hours % 60;
        hours = hours / 60;
        finalTime.setText(String.format("%d:%02d:%02d", hours, minutes, seconds));
        finalTemperatureText.setText(temperatureText.getText());
        finalPressureText.setText(pressureText.getText());
        finalDistanceText.setText(distanceText.getText());

        findViewById(R.id.path_view).setVisibility(View.GONE);
    }

    /**
     * Called when visit information is retreived from the PathRecorderService
     * @param resultCode integer, should only ever be = to 3
     * @param resultData contains the Visit information
     */
    @Override
    public void onVisitFetch(int resultCode, Bundle resultData) {
        // Retrieve the visit from the service
        mVisit = resultData.getParcelable("VISIT");
        Log.d(TAG, "Visit ID received from service" + mVisit.toString());
        ((TextView)findViewById(R.id.final_title)).setText(mVisit.getTitle());
        ((TextView)findViewById(R.id.title)).setText(mVisit.getTitle());


        final FloatingActionButton fab_stop = findViewById(R.id.fab_stop);
        final ImageButton fab_record = findViewById(R.id.fab_record);
        final FloatingActionButton fab_pause = findViewById(R.id.fab_pause);
        final FloatingActionButton fab_camera = findViewById(R.id.fab_camera);
        fab_record.setVisibility(View.GONE);
        fab_pause.setVisibility(View.VISIBLE);
        fab_stop.setVisibility(View.VISIBLE);
        fab_camera.setVisibility(View.VISIBLE);
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