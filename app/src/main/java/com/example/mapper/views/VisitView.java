package com.example.mapper.views;

import android.os.Bundle;
import android.view.Menu;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.mapper.R;
import com.example.mapper.services.models.Visit;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import static com.example.mapper.views.VisitListAdapter.EXTRA_VISIT_VIEW;

public class VisitView extends AppCompatActivity {

    private TextView visitTitleView;
    private TextView visitDescriptionView;
    private TextView visitDistanceView;
    private TextView visitDateView;

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
        visitDistanceView.setText(visit.getDistance() + " km");

        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        visitDateView = findViewById(R.id.date);
        visitDateView.setText(dateFormat.format(visit.getVisitDate()));
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
}