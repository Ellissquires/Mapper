package com.example.mapper.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import com.example.mapper.R;
import com.example.mapper.sensors.BarometerSensor;
import com.example.mapper.sensors.TemperatureSensor;
import com.example.mapper.services.models.Visit;
import com.example.mapper.viewmodels.NewVisitViewModel;
import java.util.Date;

public class NewVisitView extends AppCompatActivity {
    public static final String EXTRA_VISIT = "com.example.mapper.VISIT";

    private EditText mEditTitleView;
    private EditText mEditDescriptionView;
    private NewVisitViewModel mNewVisitViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_visit);
        mEditTitleView = findViewById(R.id.title);
        mEditDescriptionView = findViewById(R.id.description);
        mNewVisitViewModel = ViewModelProviders.of(this).get(NewVisitViewModel.class);

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

        Button recordButton = (Button) findViewById(R.id.record);
        // Setup pathless visit with title and description
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Retrieve user inputs
                String title = mEditTitleView.getText().toString();
                String description = mEditDescriptionView.getText().toString();
                Visit newVisit = new Visit(title, description, new Date(), 0);
                // Create new intent for the MapView activity and pass the visit
                Intent intent = new Intent(NewVisitView.this, MapView.class);
                intent.putExtra(EXTRA_VISIT, newVisit);
                startActivity(intent);
            }
        });
    }
}
