package com.example.mapper.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mapper.R;
import com.example.mapper.sensors.BarometerSensor;
import com.example.mapper.sensors.TemperatureSensor;
import com.example.mapper.services.models.Visit;
import com.example.mapper.viewmodels.VisitViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class NewVisitView extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_visit);

        Button recordButton = (Button) findViewById(R.id.record);
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            Intent intent = new Intent(NewVisitView.this, MapView.class);
            startActivity(intent);
            }
        });

        // Disable checkboxes if the sensor's are unavailaable
        BarometerSensor mBS = new BarometerSensor(this);
        TemperatureSensor mTS = new TemperatureSensor(this);
        if(!mBS.sensorAvailable()) {
            CheckBox checkbox = (CheckBox) findViewById(R.id.pressure_checkbox);
            checkbox.setVisibility(View.GONE);
        }
        if(!mTS.sensorAvailable()) {
            CheckBox checkbox = (CheckBox) findViewById(R.id.temp_checkbox);
            checkbox.setVisibility(View.GONE);
        }



    }
}
