package com.example.mapper.views;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.mapper.ImageHandler.CacheHandler;
import com.example.mapper.R;
import com.example.mapper.services.ImageFetchService;
import com.example.mapper.services.VisitRepository;
import com.example.mapper.services.models.Visit;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.example.mapper.views.VisitListAdapter.EXTRA_VISIT_VIEW;
import static com.example.mapper.views.VisitView.EXTRA_EDIT_VISIT;

public class EditVisitView extends AppCompatActivity {

    private EditText mEditTitleView;
    private EditText mEditDescriptionView;
    private TextView mDescriptionWarning;
    private CardView warningCard;
    private Visit mVisit;
    Handler handler = new Handler();

    private VisitRepository mVisitRepo;

    CacheHandler cache = CacheHandler.getInstance();
    public EditVisitView() {
    }

    public void editPictureDirectory(){

    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_visit);
        mEditTitleView = findViewById(R.id.title);
        mEditDescriptionView = findViewById(R.id.description);
        mDescriptionWarning = findViewById(R.id.warning);
        warningCard = findViewById(R.id.warning_container);

        mVisitRepo = new VisitRepository(getApplication());

        final List<String> files = new ArrayList<>();
        LiveData<List<Visit>> visits =  mVisitRepo.getAllVisits();
        visits.observe(EditVisitView.this, new Observer<List<Visit>>() {
            @Override
            public void onChanged(List<Visit> visits) {
                for(int i = 0; i<visits.size(); i++){
                    files.add(visits.get(i).getTitle());
                }
            }
        });

        Bundle extras = getIntent().getExtras();
        mVisit = extras.getParcelable(EXTRA_EDIT_VISIT);
        mEditTitleView.setText(mVisit.getTitle());
        mEditDescriptionView.setText(mVisit.getDescription());

        final Button editButton = (Button) findViewById(R.id.edit);
        // Setup pathless visit with title and description
        editButton.setOnClickListener(new View.OnClickListener() {
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
                    ImageFetchService.editImageFolder(mVisit.getTitle(),title, EditVisitView.this, cache);
                    mVisit.setTitle(title);
                    mVisit.setDescription(description);

                    mVisitRepo.updateVisit(mVisit);

                    // Create a new intent passing in the selected visit
                    Intent intent = new Intent(EditVisitView.this, VisitView.class);
                    intent.putExtra(EXTRA_VISIT_VIEW, mVisit);
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
