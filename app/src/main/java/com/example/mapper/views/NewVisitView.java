package com.example.mapper.views;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import com.example.mapper.R;
import com.example.mapper.sensors.BarometerSensor;
import com.example.mapper.sensors.TemperatureSensor;
import com.example.mapper.services.VisitRepository;
import com.example.mapper.services.models.Visit;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NewVisitView extends AppCompatActivity {
    public static final String EXTRA_VISIT = "com.example.mapper.VISIT";

    private EditText mEditTitleView;
    private EditText mEditDescriptionView;
    Handler handler = new Handler();

    public NewVisitView() {
    }

    /**
     * A default method for every class that extends AppCompatActivity that allows it to define
     * which layout it will use, as well as how define and to manipulate its contents
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_visit);
        mEditTitleView = findViewById(R.id.title);
        mEditDescriptionView = findViewById(R.id.description);
        mEditTitleView.setHint("Title");
        mEditTitleView.setHintTextColor(Color.WHITE);

        mEditDescriptionView.setHint("Description");
        mEditDescriptionView.setHintTextColor(Color.WHITE);

        final List<String> files = new ArrayList<>();
        VisitRepository mVisitRepo = new VisitRepository(getApplication());
        LiveData<List<Visit>> visits =  mVisitRepo.getAllVisits();
        visits.observe(NewVisitView.this, new Observer<List<Visit>>() {
            @Override
            public void onChanged(List<Visit> visits) {
                for(int i = 0; i<visits.size(); i++){
                    files.add(visits.get(i).getTitle());
                }
            }
        });

        // Disable checkboxes if the sensor's are unavailaable
        BarometerSensor mBS = new BarometerSensor(this);
        TemperatureSensor mTS = new TemperatureSensor(this);
        if(!mBS.sensorAvailable()) {
            CheckBox checkbox = findViewById(R.id.pressure_checkbox);
            checkbox.setVisibility(View.GONE);
        }
        if(!mTS.sensorAvailable()) {
            CheckBox checkbox = findViewById(R.id.temp_checkbox);
            checkbox.setVisibility(View.GONE);
        }

        Button recordButton = findViewById(R.id.record);
        // Setup pathless visit with title and description
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Retrieve user inputs
                String title = mEditTitleView.getText().toString();
                String description = mEditDescriptionView.getText().toString();



                //Title and description can not be left as blank.
                boolean titleOK = title.length() >= 1;
                boolean descriptionOK = description.length() >= 1;
                boolean usedName = files.contains(title);

                if (titleOK && descriptionOK && !usedName) {

                    Visit newVisit = new Visit(title, description, new Date(), 0, 0);
                    // Create new intent for the MapView activity and pass the visit
                    Intent intent = new Intent(NewVisitView.this, MapView.class);
                    intent.putExtra(EXTRA_VISIT, newVisit);
                    startActivity(intent);
                    // End this activity, as it is not needed after this.
                    finish();
                } else {
                    // Set warnings to visible if need be.
                    if (!titleOK) {
                        mEditTitleView.setHint("Required");
                        mEditTitleView.setHintTextColor(Color.RED);
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mEditTitleView.setHint("Title");
                                mEditTitleView.setHintTextColor(Color.WHITE);
                            }
                        }, 2000);


                    }
                    if (!descriptionOK) {
                        mEditDescriptionView.setHint("Required");
                        mEditDescriptionView.setHintTextColor(Color.RED);
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mEditDescriptionView.setHint("Description");
                                mEditDescriptionView.setHintTextColor(Color.WHITE);
                            }
                        }, 2000);
                    }
                    if(usedName){
                        mEditTitleView.setText("");
                        mEditTitleView.setHint("Use Different Title");
                        mEditTitleView.setHintTextColor(Color.RED);
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mEditTitleView.setHint("Title");
                                mEditTitleView.setHintTextColor(Color.WHITE);
                            }
                        }, 2000);


                    }
                }
            }
        });
    }
}
