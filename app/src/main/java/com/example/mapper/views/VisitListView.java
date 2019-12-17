package com.example.mapper.views;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mapper.ImageHandler.CacheHandler;
import com.example.mapper.R;
import com.example.mapper.SplashActivity;
import com.example.mapper.sensors.BarometerSensor;
import com.example.mapper.sensors.LocationSensor;
import com.example.mapper.sensors.TemperatureSensor;
import com.example.mapper.services.ImageFetchService;
import com.example.mapper.services.PathRecorder.PathRecorderService;
import com.example.mapper.services.models.Visit;
import com.example.mapper.viewmodels.VisitViewModel;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Date;
import java.util.List;


public class VisitListView extends AppCompatActivity {
    private VisitListAdapter adapter;
    private VisitViewModel mVisitViewModel;
    private BottomAppBar bottomAppBar;
  
    private SearchView searchView;
    private BarometerSensor mBarometer;
    private TemperatureSensor mTempSensor;
    private LocationSensor mGPSSensor;


    @Override
    public void onCreate(Bundle savedInstanceState){
        ImageFetchService.imagePermissions(getApplicationContext(),this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visitlist);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        CacheHandler cache = CacheHandler.getInstance();

        ImageFetchService.cacheImages(cache, this);

        if (PathRecorderService.checkIsRunning(getApplicationContext())) {
            // Create new intent for the MapView activity, dont pass a visit
            Intent intent = new Intent(this, MapView.class);
            startActivity(intent);
        }

//        bottomAppBar = findViewById(R.id.bar);
        //set bottom bar to Action bar as it is similar like Toolbar
//        setSupportActionBar(bottomAppBar);


        ImageButton fab = (ImageButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(VisitListView.this, NewVisitView.class);
                startActivity(intent);
            }
        });

        ImageButton open = (ImageButton) findViewById(R.id.open_gallery);
        open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(VisitListView.this, GalleryView.class);
                startActivity(intent);
            }
        });

        final CardView menubar = (CardView) findViewById(R.id.menubar);

        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        adapter = new VisitListAdapter(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener(){
            @Override
            public void onScrolled(RecyclerView recyclerView, int width, int height){
                if (height>0)
//                    menubar.setVisibility(recyclerView.GONE);
                    menubar.animate().translationY(500);
                else if(height<0)
//                    menubar.setVisibility(recyclerView.VISIBLE);
                    menubar.animate().translationY(0);
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
        });

        mVisitViewModel = ViewModelProviders.of(this).get(VisitViewModel.class);
        mVisitViewModel.getAllVisits().observe(this, new Observer<List<Visit>>() {
            @Override
            public void onChanged(@Nullable final List<Visit> visits) {
                adapter.setVisits(visits);
            }
        });
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.bottomappbar_menu, menu);
        getMenuInflater().inflate(R.menu.menu_search, menu);
        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search)
                .getActionView();
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        // listening to search query text change
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // filter recycler view when query submitted
                adapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // filter recycler view when text is changed
                adapter.getFilter().filter(query);
                return false;
            }
        });
        return true;
    }

//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
}
