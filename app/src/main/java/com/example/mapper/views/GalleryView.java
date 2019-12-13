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
import com.example.mapper.R;
import com.example.mapper.services.ImageFetchService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GalleryView extends AppCompatActivity {

    private List<ImageObj> myPictureList = new ArrayList<>();
    private RecyclerView.Adapter  mAdapter;
    private RecyclerView mRecyclerView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        mRecyclerView = (RecyclerView) findViewById(R.id.visit_gallery);
        // set up the RecyclerView

        int numberOfColumns = 3;

        mRecyclerView.setLayoutManager(new GridLayoutManager(this, numberOfColumns));

        mAdapter= new ImageAdapter(myPictureList);
        mRecyclerView.setAdapter(mAdapter);

        FloatingActionButton camera = (FloatingActionButton) findViewById(R.id.camera);
        FloatingActionButton fab_gallery = (FloatingActionButton) findViewById(R.id.fab_gallery);
        FloatingActionButton fab_unsorted = (FloatingActionButton) findViewById(R.id.fab_unsorted);
        FloatingActionButton fab_sorted = (FloatingActionButton) findViewById(R.id.fab_sorted);

        camera.setVisibility(View.GONE);
        fab_gallery.setVisibility(View.GONE);
        fab_sorted.setVisibility(View.VISIBLE);
        fab_unsorted.setVisibility(View.VISIBLE);

        checkPermissions(getApplicationContext());
        findImages();
    }

    private void findImages(){
        File storageDir = new File((getApplicationContext().getExternalFilesDir(null).getAbsolutePath()) + "/Mapper/");
        if (storageDir.exists()){
            File[] files = storageDir.listFiles();
            List<File> folders = (Arrays.asList(files));
            List<File> imageFile = new ArrayList<>();
            for(File file : folders){
                File[] visitFolder = file.listFiles();
                imageFile.addAll(Arrays.asList(visitFolder));
            }

            myPictureList.addAll(ImageFetchService.getImageElements(imageFile));
            mAdapter.notifyDataSetChanged();
            mRecyclerView.scrollToPosition(imageFile.size() - 1);
        }
    }

    private void checkPermissions(final Context context) {
        ImageFetchService.imagePermissions(context,this);
    }
}
