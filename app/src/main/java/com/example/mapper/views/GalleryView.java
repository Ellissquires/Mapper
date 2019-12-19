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

import org.jetbrains.annotations.NotNull;

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
    private Boolean loaded = false;

    Handler handler = new Handler();

    /**
     * A default method for every class that extends AppCompatActivity that allows it to define
     * which layout it will use, as well as how define and to manipulate its contents
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        ImageFetchService.imagePermissions(getApplicationContext(),this);

        final CardView menubar = findViewById(R.id.menubar);
        prompt = findViewById(R.id.prompt);

        mRecyclerView = findViewById(R.id.visit_gallery);
 
        int numberOfColumns = 3;

        mRecyclerView.setLayoutManager(new GridLayoutManager(this, numberOfColumns));
        mAdapter= new ImageAdapter(myPictureList);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener(){
            @Override
            public void onScrolled(@NotNull RecyclerView recyclerView, int width, int height){

                if (height>0)
//                    menubar.setVisibility(recyclerView.GONE);
                    menubar.animate().translationY(500);
                else if(height<0)
//                    menubar.setVisibility(recyclerView.VISIBLE);
                    menubar.animate().translationY(0);
            }

            @Override
            public void onScrollStateChanged(@NotNull RecyclerView recyclerView, int newState) {
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

        ImageButton fab_unsorted = findViewById(R.id.fab_unsorted);
        ImageButton fab_sorted =  findViewById(R.id.fab_sorted);

        LinearLayout image_container =  findViewById(R.id.Image_Container);
        LinearLayout sorted_container =  findViewById(R.id.Sorted_Container);
        LinearLayout gallery_container =  findViewById(R.id.Gallery_Container);
        LinearLayout camera_container =  findViewById(R.id.Camera_Container);


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


        initData();

    }

    protected void onStart(){
        super.onStart();
        while(!loaded){

            mAdapter.notifyDataSetChanged();

            // set up the RecyclerView
            if(mAdapter.getItemCount() < 1){
                mRecyclerView.setVisibility(View.GONE);
                prompt.setVisibility(View.VISIBLE);
            }
            else{
                mRecyclerView.setVisibility(View.VISIBLE);
                prompt.setVisibility(View.GONE);
            }

        }
    }

    /**
     * This function toggles the ImageAdapter that organises the gallery recycler view
     * into a group of all the images.
     */
    private void findImages(){
        int numberOfColumns = 3;

        mRecyclerView.setLayoutManager(new GridLayoutManager(this, numberOfColumns));
        mAdapter= new ImageAdapter(myPictureList);
        mRecyclerView.setAdapter(mAdapter);


    }

    /**
     * Initialises the lists that are used in this application as soon as the activity starts. This is done in the background thread.
     *
     */
    private void initData(){
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
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
                    myPictureList.addAll(ImageFetchService.getImageElements(imageFile, null));
                }


                loaded = true;
            }
        });
    }

    /**
     * This function toggles the SortImageAdapter that organises the gallery recycler view
     * into groups according to the visits they belong to.
     */
    private void findSortedImages(){
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter= new SortedImageAdapter(myFolderList);
        mRecyclerView.setAdapter(mAdapter);
    }
}
