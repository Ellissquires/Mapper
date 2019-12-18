package com.example.mapper.views;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import com.example.mapper.R;
import com.example.mapper.sensors.BarometerSensor;
import com.example.mapper.sensors.TemperatureSensor;
import com.example.mapper.services.VisitRepository;
import com.example.mapper.services.models.Point;
import com.example.mapper.services.models.Visit;
import com.example.mapper.viewmodels.NewVisitViewModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NewVisitView extends AppCompatActivity {
    public static final String EXTRA_VISIT = "com.example.mapper.VISIT";

    private EditText mEditTitleView;
    private EditText mEditDescriptionView;
    private TextView mTitleWarning;
    private TextView mDescriptionWarning;
    private CardView warningCard;
    private NewVisitViewModel mNewVisitViewModel;
    Handler handler = new Handler();

    private VisitRepository mVisitRepo;

    public NewVisitView() {
    }

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

//        mTitleWarning = findViewById(R.id.title_warning);
//        mDescriptionWarning = findViewById(R.id.description_warning);
        mDescriptionWarning = findViewById(R.id.warning);
        warningCard = findViewById(R.id.warning_container);
        final List<String> files = new ArrayList<>();
        mVisitRepo = new VisitRepository(getApplication());
        LiveData<List<Visit>> visits =  mVisitRepo.getAllVisits();
        visits.observe(NewVisitView.this, new Observer<List<Visit>>() {
            @Override
            public void onChanged(List<Visit> visits) {
                for(int i = 0; i<visits.size(); i++){
                    files.add(visits.get(i).getTitle());
                }
            }
        });

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



                //Title and description can not be left as blank.
                boolean titleOK = title.length() >= 1;
                boolean descriptionOK = description.length() >= 1;
                boolean usedName = files.contains(title);
//                for(int i = 0; i < visits.size(); i++){
//                    if(visits.get(i).getTitle() == title)
//                        usedName = true;
//                }

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
