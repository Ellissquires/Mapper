package com.example.mapper.views;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.mapper.R;
import com.example.mapper.services.models.Point;
import com.example.mapper.services.models.Visit;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import static com.example.mapper.views.VisitListAdapter.EXTRA_VISIT_VIEW;

public class VisitView extends AppCompatActivity implements OnMapReadyCallback {

    private TextView visitTitleView;
    private TextView visitDescriptionView;
    private TextView visitDistanceView;
    private TextView visitDateView;
    private GoogleMap mMap;


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
        Visit visit = (Visit) extras.getParcelable(EXTRA_VISIT_VIEW);

        visitTitleView = findViewById(R.id.title);
        visitTitleView.setText(visit.getTitle());

        visitDescriptionView = findViewById(R.id.description);
        visitDescriptionView.setText(visit.getDescription());

        visitDistanceView = findViewById(R.id.distance);


        // Set distance (units dependant on distance, <100m = M, else KM)
        float dist = (float)visit.getDistance();
        if (dist < 100) {
            visitDistanceView.setText(String.format("%.1f m", dist));
        } else {
            visitDistanceView.setText(String.format("%.2f Km", dist / 1000.0));
        }

        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        visitDateView = findViewById(R.id.date);
        visitDateView.setText(dateFormat.format(visit.getVisitDate()));


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

    }




}