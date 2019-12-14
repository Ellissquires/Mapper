package com.example.mapper.views;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mapper.ImageHandler.ImageAdapter;
import com.example.mapper.ImageHandler.ImageObj;
import com.example.mapper.ImageHandler.SortedImageAdapter;
import com.example.mapper.R;
import com.example.mapper.services.ImageFetchService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



public class GalleryView extends AppCompatActivity {

    private List<ImageObj> myPictureList = new ArrayList<>();
    private List<File> myFolderList = new ArrayList<>();
    private RecyclerView.Adapter  mAdapter;
    private RecyclerView mRecyclerView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        mRecyclerView = (RecyclerView) findViewById(R.id.visit_gallery);
        int numberOfColumns = 3;

        mRecyclerView.setLayoutManager(new GridLayoutManager(this, numberOfColumns));
        mAdapter= new ImageAdapter(myPictureList);
        mRecyclerView.setAdapter(mAdapter);
        // set up the RecyclerView

        FloatingActionButton camera = (FloatingActionButton) findViewById(R.id.camera);
        FloatingActionButton fab_gallery = (FloatingActionButton) findViewById(R.id.fab_gallery);
        FloatingActionButton fab_unsorted = (FloatingActionButton) findViewById(R.id.fab_unsorted);
        FloatingActionButton fab_sorted = (FloatingActionButton) findViewById(R.id.fab_sorted);

        camera.setVisibility(View.GONE);
        fab_gallery.setVisibility(View.GONE);
        fab_sorted.setVisibility(View.VISIBLE);
        fab_unsorted.setVisibility(View.VISIBLE);

        fab_sorted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findSortedImages();
            }
        });

        fab_unsorted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findImages();
            }
        });
        checkPermissions(getApplicationContext());
        initData();
        findImages();
    }

    private void findImages(){
        int numberOfColumns = 3;

        mRecyclerView.setLayoutManager(new GridLayoutManager(this, numberOfColumns));
        mAdapter= new ImageAdapter(myPictureList);
        mRecyclerView.setAdapter(mAdapter);


    }

    private void initData(){
        File storageDir = new File((getApplicationContext().getExternalFilesDir(null).getAbsolutePath()) + "/Mapper/");
        if (storageDir.exists()){
            File[] files = storageDir.listFiles();
            myFolderList.addAll(Arrays.asList(files));

            List<File> folders = (Arrays.asList(files));
            List<File> imageFile = new ArrayList<>();
            for(File file : folders){
                File[] visitFolder = file.listFiles();
                imageFile.addAll(Arrays.asList(visitFolder));
            }

            myPictureList.addAll(ImageFetchService.getImageElements(imageFile));
        }
    }

    private void findSortedImages(){
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter= new SortedImageAdapter(myFolderList);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void checkPermissions(final Context context) {
        ImageFetchService.imagePermissions(context,this);
    }
}
