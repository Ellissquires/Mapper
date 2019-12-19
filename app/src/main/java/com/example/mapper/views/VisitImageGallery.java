package com.example.mapper.views;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mapper.ImageHandler.CacheHandler;
import com.example.mapper.ImageHandler.ImageAdapter;
import com.example.mapper.ImageHandler.ImageObj;
import com.example.mapper.R;
import com.example.mapper.services.ImageFetchService;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.example.mapper.views.VisitView.EXTRA_VIEW_IMAGES;

public class VisitImageGallery extends AppCompatActivity {

    private List<ImageObj> mPictureList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visit_image_gallery);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        Intent intent = getIntent();
        String title = intent.getStringExtra(EXTRA_VIEW_IMAGES);
        fetchVisitImages(title);

        RecyclerView mRecyclerView = findViewById(R.id.visit_gallery);
        int numberOfColumns = 3;
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, numberOfColumns));
        RecyclerView.Adapter mAdapter = new ImageAdapter(mPictureList);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void fetchVisitImages(String title){
        File storageDir = new File((getApplicationContext().getExternalFilesDir(null).getAbsolutePath()) + "/Mapper/" + title);
        if (storageDir.exists()){
            File[] files = storageDir.listFiles();
            List<File> images = null;
            if (files != null) {
                images = (Arrays.asList(files));
                mPictureList.addAll(ImageFetchService.getImageElements(images, null));
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
