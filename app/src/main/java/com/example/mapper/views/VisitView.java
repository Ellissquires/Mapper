package com.example.mapper.views;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.example.mapper.R;
import com.example.mapper.services.PathRepository;
import com.example.mapper.services.models.Point;
import com.example.mapper.services.models.Visit;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import static com.example.mapper.views.VisitListAdapter.EXTRA_VISIT_VIEW;

public class VisitView extends AppCompatActivity implements OnMapReadyCallback {

    private TextView visitTitleView;
    private TextView visitDescriptionView;
    private TextView visitDistanceView;
    private TextView visitDateView;
    private GoogleMap mMap;
    private Visit mVisit;
    private PathRepository mPathRepo;



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

        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        visitTitleView = findViewById(R.id.title);
        visitTitleView.setText(mVisit.getTitle());
        visitDescriptionView = findViewById(R.id.description);
        visitDescriptionView.setText(mVisit.getDescription());
        visitDistanceView = findViewById(R.id.distance);
        visitDateView = findViewById(R.id.date);
        visitDateView.setText(dateFormat.format(mVisit.getVisitDate()));

        mPathRepo = new PathRepository(getApplication());

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

        // Fetching the visit path and drawing it on the map
        long pathID = mVisit.getPathId();
        LiveData<List<Point>> path = mPathRepo.getPointsOnPath(pathID - 1);

        path.observe(this, new Observer<List<Point>>() {
            @Override
            public void onChanged(@Nullable final List<Point> path) {
                Log.d("VisitView", "Path retrieved with size " + path.size());
                mMap.clear();
                // Define line options
                PolylineOptions options = new PolylineOptions().width(5).color(Color.BLUE).geodesic(true);
                Point p1 = path.get(0);
                CameraUpdate center=
                        CameraUpdateFactory.newLatLng(new LatLng(p1.getLat(), p1.getLng()));
                CameraUpdate zoom=CameraUpdateFactory.zoomTo(5);

                Log.d("VisitView", "First Point" + p1.getLat() + " - " + p1.getLng());
                LatLng latLng = new LatLng(p1.getLat(),p1.getLng());
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
                mMap.animateCamera(cameraUpdate);

                // Loop points and add them to the line
                for(Point p : path){
                    LatLng mapPoint = new LatLng(p.getLat(), p.getLng());
                    options.add(mapPoint);
                }
                // draw line
                mMap.addPolyline(options);
            }
        });
        
    }

}