package com.example.mapper.views;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mapper.ImageHandler.ImageAdapter;
import com.example.mapper.ImageHandler.ImageObj;
import com.example.mapper.ImageHandler.SortedImageAdapter;
import com.example.mapper.R;
import com.example.mapper.services.ImageFetchService;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



public class GalleryView extends AppCompatActivity {

    private List<ImageObj> myPictureList = new ArrayList<>();
    private List<File> myFolderList = new ArrayList<>();
    private RecyclerView.Adapter  mAdapter;
    private RecyclerView mRecyclerView;

    private TextView prompt;

    Handler handler = new Handler();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        ImageFetchService.imagePermissions(getApplicationContext(),this);

        final CardView menubar = (CardView) findViewById(R.id.menubar);
        prompt = (TextView) findViewById(R.id.prompt);

        mRecyclerView = (RecyclerView) findViewById(R.id.visit_gallery);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int width, int height) {
                int scrollViewHeight = recyclerView.getChildAt(0).getHeight();
                if (height <= scrollViewHeight) {
                    menubar.animate().translationY(0);
                }
                if (height > 0)
//                    menubar.setVisibility(recyclerView.GONE);
                    menubar.animate().translationY(500);
                else if (height < 0)
//                    menubar.setVisibility(recyclerView.VISIBLE);
                    menubar.animate().translationY(0);
            }
        });

        int numberOfColumns = 3;


        initData();

        mRecyclerView.setLayoutManager(new GridLayoutManager(this, numberOfColumns));
        mAdapter= new ImageAdapter(myPictureList);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener(){
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
                if(newState == RecyclerView.SCROLL_STATE_IDLE){
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            menubar.animate().translationY(0);
                        }
                    }, 3000);

                }

            }
        });
        // set up the RecyclerView
        if(mAdapter.getItemCount() < 1){
            mRecyclerView.setVisibility(View.GONE);
            prompt.setVisibility(View.VISIBLE);
        }
        else{
            mRecyclerView.setVisibility(View.VISIBLE);
            prompt.setVisibility(View.GONE);
        }


        ImageButton fab_unsorted = (ImageButton) findViewById(R.id.fab_unsorted);
        ImageButton fab_sorted = (ImageButton) findViewById(R.id.fab_sorted);

        LinearLayout image_container = (LinearLayout) findViewById(R.id.Image_Container);
        LinearLayout sorted_container = (LinearLayout) findViewById(R.id.Sorted_Container);
        LinearLayout gallery_container = (LinearLayout) findViewById(R.id.Gallery_Container);
        LinearLayout camera_container = (LinearLayout) findViewById(R.id.Camera_Container);


        camera_container.setVisibility(View.GONE);
        gallery_container.setVisibility(View.GONE);
        sorted_container.setVisibility(View.VISIBLE);
        image_container.setVisibility(View.VISIBLE);

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
}
