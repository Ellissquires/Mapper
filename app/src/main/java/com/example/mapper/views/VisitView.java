package com.example.mapper.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mapper.R;
import com.example.mapper.sensors.BarometerSensor;
import com.example.mapper.sensors.LocationSensor;
import com.example.mapper.sensors.TemperatureSensor;
import com.example.mapper.services.models.Visit;
import com.example.mapper.viewmodels.VisitViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;


public class VisitView extends AppCompatActivity {

    private VisitViewModel mVisitViewModel;
    private BarometerSensor mBarometer;
    private TemperatureSensor mTempSensor;
    private LocationSensor mGPSSensor;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visit);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(VisitView.this, MapView.class);
                startActivity(intent);
            }
        });

        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        final VisitListAdapter adapter = new VisitListAdapter(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mVisitViewModel = ViewModelProviders.of(this).get(VisitViewModel.class);
        mVisitViewModel.getAllVisits().observe(this, new Observer<List<Visit>>() {
            @Override
            public void onChanged(@Nullable final List<Visit> visits) {
                // Update the cached copy of the words in the adapter.

                if(visits.size() == 0){
                    TextView tv = findViewById(R.id.visitInfoMessage);
                    tv.setText("No Visits Recorded");
                } else  {
                    TextView tv = findViewById(R.id.visitInfoMessage);
                    tv.setText("Number of visits " + visits.size());
                }
                adapter.setVisits(visits);
            }
        });

        mBarometer = new BarometerSensor(this);
        mTempSensor = new TemperatureSensor(this);
        mGPSSensor = new LocationSensor(this, this);
        mBarometer.startSensing();
        mTempSensor.startSensing();
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
    protected void onResume() {
        super.onResume();
        mBarometer.startSensing();
        mTempSensor.startSensing();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mBarometer.stopSensing();
        mTempSensor.stopSensing();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
