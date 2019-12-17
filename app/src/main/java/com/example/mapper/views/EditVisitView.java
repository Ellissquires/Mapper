package com.example.mapper.views;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.ViewModelProviders;
import com.example.mapper.R;
import com.example.mapper.services.VisitRepository;
import com.example.mapper.services.models.Visit;
import java.util.Date;

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


    public EditVisitView() {
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

                if (titleOK && descriptionOK) {
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
                    if (!titleOK && descriptionOK) {
                        warningCard.setVisibility(View.VISIBLE);
                        mDescriptionWarning.setText("Must have a title");
                        warningCard.animate().scaleY(1);
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                warningCard.animate().scaleY(0);
                                warningCard.setVisibility(View.GONE);
                            }
                        }, 2000);

                    } else if (!descriptionOK) {
                        warningCard.setVisibility(View.VISIBLE);
                        mDescriptionWarning.setText("Must have a description");
                        warningCard.animate().scaleY(1);
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                warningCard.animate().scaleY(0);
                                warningCard.setVisibility(View.GONE);
                            }
                        }, 2000);
                    } else if(!titleOK){
                        warningCard.setVisibility(View.VISIBLE);
                        mDescriptionWarning.setText("Must have a decription and title");
                        warningCard.animate().scaleY(1);
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                warningCard.animate().scaleY(0);
                                warningCard.setVisibility(View.GONE);
                            }
                        }, 2000);
                    } else {
                        mDescriptionWarning.setVisibility(View.GONE);
                        warningCard.animate().scaleY(0);
                        warningCard.setVisibility(View.GONE);

                    }
                }

            }
        });
    }
}
