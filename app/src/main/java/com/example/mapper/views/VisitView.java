package com.example.mapper.views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.example.mapper.R;
import com.example.mapper.services.ImageFetchService;
import com.example.mapper.services.PathRepository;
import com.example.mapper.services.PicturePointRepository;
import com.example.mapper.services.VisitRepository;
import com.example.mapper.services.models.PicturePoint;
import com.example.mapper.services.models.Point;
import com.example.mapper.services.models.Visit;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static com.example.mapper.views.VisitListAdapter.EXTRA_VISIT_VIEW;

public class VisitView extends AppCompatActivity implements OnMapReadyCallback {

    public static final String EXTRA_EDIT_VISIT = "com.example.mapper.EDIT_VISIT";
    public static final String EXTRA_VIEW_IMAGES = "com.example.mapper.VIEW_IMAGES";

    private TextView visitTitleView;
    private TextView visitDescriptionView;
    private TextView visitDistanceView;
    private TextView visitDateView;
    private GoogleMap mMap;
    private Visit mVisit;
    private PathRepository mPathRepo;
    private PicturePointRepository mPictPointRepo;
    private VisitRepository mVisitRepo;
    private Context mContext;
    private List<Point> mPoints;


    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visit);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Retrieve the visit from the intent
        Bundle extras = getIntent().getExtras();
        mVisit = extras.getParcelable(EXTRA_VISIT_VIEW);
        mContext = getApplicationContext();

        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        visitTitleView = findViewById(R.id.title);
        visitTitleView.setText(mVisit.getTitle());
        visitDescriptionView = findViewById(R.id.description);
        visitDescriptionView.setText(mVisit.getDescription());
        visitDistanceView = findViewById(R.id.distance);
        visitDateView = findViewById(R.id.date);
        visitDateView.setText(dateFormat.format(mVisit.getVisitDate()));

        mPathRepo = new PathRepository(getApplication());
        mVisitRepo = new VisitRepository(getApplication());
        mPictPointRepo = new PicturePointRepository(getApplication());

        // Set distance (units dependant on distance, <100m = M, else KM)
        float dist = (float)mVisit.getDistance();
        if (dist < 100) {
            visitDistanceView.setText(String.format("%.1f m", dist));
        } else {
            visitDistanceView.setText(String.format("%.2f Km", dist / 1000.0));
        }

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        MaterialButton viewImagesButton = findViewById(R.id.view_images);
        viewImagesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(VisitView.this, VisitImageGallery.class);
                intent.putExtra(EXTRA_VIEW_IMAGES, mVisit.getTitle());
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_visit, menu);
        return true;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;

        // Set the maps Info window
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                // Inflate and show the image.
                View v = getLayoutInflater().inflate(R.layout.custom_infowindow, null);
                ImageView img = (ImageView) v.findViewById(R.id.infowindow_image);

                img.setImageURI(Uri.parse((String)marker.getTag()));

                return v;
            }
        }); // SetInfoWindowAdapter

        try {
            // Set the map style
             mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.map_style));
        } catch (Resources.NotFoundException e) {

        }

        // Fetching the visit path and drawing it on the map
        long pathID = mVisit.getPathId();
        LiveData<List<Point>> path = mPathRepo.getPointsOnPath(pathID - 1);

        final LifecycleOwner owner = this;
        path.observe(this, new Observer<List<Point>>() {
            @Override
            public void onChanged(@Nullable final List<Point> path) {
                if (path.size() > 0){
                    Log.d("VisitView", "Path retrieved with size " + path.size());
                    mMap.clear();
                    // Define line options
                    PolylineOptions options = new PolylineOptions().width(5).color(Color.BLUE).geodesic(true);
                    Point p1 = path.get(0);
                    // Center the map on the first point of the path
                    Log.d("VisitView", "First Point" + p1.getLat() + " - " + p1.getLng());
                    LatLng latLng = new LatLng(p1.getLat(),p1.getLng());
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
                    mMap.animateCamera(cameraUpdate);

                    // Loop points and add them to the line
                    for(Point p : path){
                        final LatLng mapPoint = new LatLng(p.getLat(), p.getLng());
                        options.add(mapPoint);

                        // For each point, check if there is a corresponding picture point.
                        // If there is, add a marker to the map.
                        LiveData<PicturePoint> pictPoint = mPictPointRepo.getPicturePoint(p.getId()-1);
                        pictPoint.observe(owner, new Observer<PicturePoint>() {
                            @Override
                            public void onChanged(PicturePoint picturePoint) {
                                if (picturePoint != null) {
                                    final BitmapFactory.Options options = new BitmapFactory.Options();
                                    Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.map_pin);
                                    bitmap = getIcon(bitmap, 150);
                                    Marker m = mMap.addMarker(new MarkerOptions()
                                            .position(mapPoint)
                                            .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                                            .title("Photo"));
                                    m.setTag(picturePoint.getPictureURI());
                                }
                            }
                        });
                    }
                    // Draw the line
                    mMap.addPolyline(options);

                    mPoints = new ArrayList<Point>();
                    mPoints.addAll(path);
                }

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.visit_delete:
                mVisitRepo.deleteVisit(mVisit);
                Snackbar.make(getWindow().getDecorView(), R.string.visit_removed_message, Snackbar.LENGTH_LONG)
                        .setAction(R.string.undo, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mVisitRepo.createVisit(mVisit);
                            }
                        }).addCallback(new Snackbar.Callback(){
                            @Override
                            public void onDismissed(Snackbar snackbar, int event) {
                                if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT) {
                                    // Snackbar closed on its own
                                    ImageFetchService.deleteImageFolder(mVisit.getTitle(), mContext);
                                    Intent intent = new Intent(VisitView.this, VisitListView.class);
                                    startActivity(intent);
                                }
                            }
                }).show();
                return true;
            case R.id.visit_edit:
                Intent intent = new Intent(VisitView.this, EditVisitView.class);
                intent.putExtra(EXTRA_EDIT_VISIT, mVisit);
                startActivity(intent);
                finish();
                return true;
            case R.id.visit_share:
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "Look at where I've just been walking!");
                sendIntent.setType("text/plain");

                Intent shareIntent = Intent.createChooser(sendIntent, "Share path to...");
                startActivity(shareIntent);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public Bitmap getIcon(Bitmap bm, int w){
        int width = bm.getWidth();
        float scaleWidth = ((float) w) / width;
        // create a matrix for the manipulation
        Matrix matrix = new Matrix();
        // resize the bit map
        matrix.postScale(scaleWidth, scaleWidth);
        // recreate the new Bitmap
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, width, matrix, true);
        return resizedBitmap;
    }
}