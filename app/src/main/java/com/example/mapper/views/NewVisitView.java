package com.example.mapper.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mapper.R;
import com.example.mapper.services.models.Path;
import com.example.mapper.services.models.Visit;
import com.example.mapper.viewmodels.NewVisitViewModel;
import com.example.mapper.viewmodels.VisitViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Date;
import java.util.List;

public class NewVisitView extends AppCompatActivity {

    private EditText mEditTitleView;
    private EditText mEditDescriptionView;
    private NewVisitViewModel mNewVisitViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_visit);
        Button recordButton = (Button) findViewById(R.id.record);
        mEditTitleView = findViewById(R.id.title);
        mEditDescriptionView = findViewById(R.id.description);
        mNewVisitViewModel = ViewModelProviders.of(this).get(NewVisitViewModel.class);

        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = mEditTitleView.getText().toString();
                String description = mEditDescriptionView.getText().toString();

                Visit newVisit = new Visit(title, description, new Date());
                mNewVisitViewModel.createVisit(newVisit);
                Intent intent = new Intent(NewVisitView.this, MapView.class);
                startActivity(intent);
            }
    });

    }
}
