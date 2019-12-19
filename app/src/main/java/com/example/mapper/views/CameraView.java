package com.example.mapper.views;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mapper.ImageHandler.CacheHandler;
import com.example.mapper.ImageHandler.ImageAdapter;
import com.example.mapper.ImageHandler.ImageObj;
import com.example.mapper.R;
import com.example.mapper.services.PicturePointRepository;
import com.example.mapper.services.models.PicturePoint;
import com.example.mapper.services.models.Point;
import com.example.mapper.services.models.Visit;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.example.mapper.services.ImageFetchService;

import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;


public class CameraView extends AppCompatActivity {
    public static final String EXTRA_VISIT = "com.example.mapper.VISIT";

    private String title;
    private static final int REQUEST_CAMERA = 7500;

    private List<ImageObj> myPictureList = new ArrayList<>();
    private RecyclerView.Adapter  mAdapter;
    private RecyclerView mRecyclerView;
    private TextView prompt;

    private CacheHandler cache = CacheHandler.getInstance();

    private Context context;
    private Activity activity;

    private Handler handler = new Handler();

    private ArrayList<String> filePath = new ArrayList<>();
    private CardView menubar;
    private ProgressBar progressBar;

    /**
     * Easy image is a library that allows th user to upload images from the inbuilt gallery, as well as to make use of their camera
     * Here we configure it to allow images from the gallery and from the camera.
     */
    private void initEasyImage(){
       EasyImage.configuration(this)
               .setImagesFolderName("Mapper")
               .setCopyTakenPhotosToPublicGalleryAppFolder(true)
               .setCopyPickedImagesToPublicGalleryAppFolder(true)
               .setAllowMultiplePickInGallery(true);
   }

    /**
     * A default method for every class that extends AppCompatActivity that allows it to define
     * which layout it will use, as well as how define and to manipulate its contents
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        Bundle extras = getIntent().getExtras();
        Visit mVisit = null;
        if (extras != null) {
            mVisit = (Visit) extras.getParcelable(EXTRA_VISIT);
        }

        context = this;

        if (mVisit != null) {
            title = mVisit.getTitle();
        }
        menubar = (CardView)findViewById(R.id.menubar);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
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
                    menubar.animate().translationY(500);
                else if(height<0)
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
        initData();

        mAdapter.notifyDataSetChanged();

        if(mAdapter.getItemCount() < 1){
            mRecyclerView.setVisibility(View.GONE);
            prompt.setVisibility(View.VISIBLE);
        }
        else{
            mRecyclerView.setVisibility(View.VISIBLE);
            prompt.setVisibility(View.GONE);
        }

        checkPermissions(getApplicationContext());

        initEasyImage();
        activity= this;

        ImageButton fab_gallery = findViewById(R.id.fab_gallery);
        fab_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                EasyImage.openGallery(getActivity(), 0);
            }
        });

        ImageButton fab_camera = findViewById(R.id.camera);
        fab_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int cameraPermission = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA);
                if (Build.VERSION.SDK_INT >= 23) {
                    if (cameraPermission != PackageManager.PERMISSION_GRANTED  ) {

                        ActivityCompat.requestPermissions( getActivity(),
                                new String[]{Manifest.permission.CAMERA },
                                REQUEST_CAMERA
                        );
                    }
                    else{
                        EasyImage.openCamera(getActivity(), 0);
                    }
                }
            }
        });
    }

    /**
     * returns a string of image paths that have been collected from all the new images added in this activity
     * to the previous activity. This overrides the method onBackPressed(), that typically just returns to the
     * previous activity
     */
    public void finishAndReturn()
    {
        Intent returnIntent = new Intent();
        File[] storageDir = new File((getApplicationContext().getExternalFilesDir(null).getAbsolutePath()) + "/Mapper/" + title + "/").listFiles();

        filePath.add(storageDir[storageDir.length - 1].toURI().toString());
        returnIntent.putExtra("filename", filePath);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    /**
     * artificial init of the data so to upload some images as a starting point
     */
    private void initData() {
        File storageDir = new File((getApplicationContext().getExternalFilesDir(null).getAbsolutePath()) + "/Mapper/" + title + "/");

        if (storageDir.exists()){

            List<File> imageFiles = (Arrays.asList(storageDir.listFiles()));
            myPictureList.addAll(ImageFetchService.getImageElements(imageFiles, null));

        }
    }

    /**
     * check permissions are necessary starting from Android 6
     * if you do not set the permissions, the activity will simply not work and you will be probably baffled for some hours
     * until you find a note on StackOverflow
     * @param context the calling context
     */
    private void checkPermissions(final Context context) {
       ImageFetchService.imagePermissions(context,this);
    }

    /**
     * A callback function that handles the images returned by easy image. Handles
     * Errors with the pictures retured and cancelation of the easyImage methods.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        EasyImage.handleActivityResult(requestCode, resultCode, data, this, new DefaultCallback() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {
                //Some error handling
                e.printStackTrace();
            }

            @Override
            public void onImagesPicked(@NotNull final List<File> imageFiles, EasyImage.ImageSource source, int type) {
                onPhotosReturned(imageFiles, source);

            }

            @Override
            public void onCanceled(EasyImage.ImageSource source, int type) {

            }
        });
    }

    /**
     * This function updates the recycler view with the new images and saves them to the external
     * drive. It contains an Async task for the
     * @param returnedPhotos
     */
    private void onPhotosReturned(final List<File> returnedPhotos, final EasyImage.ImageSource source) {

        menubar.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        ImageFetchService.saveImage(returnedPhotos, context , title, cache, source, new ImageFetchService.SaveImagesAsyncTask.AsyncResponse() {
            @Override
            public void processFinish(){
                finishAndReturn();
            }
        });



        myPictureList.addAll(ImageFetchService.getImageElements(returnedPhotos, source));
        mRecyclerView.setVisibility(View.VISIBLE);
        prompt.setVisibility(View.GONE);

        // we tell the adapter that the data is changed and hence the grid needs
        // refreshing
        mAdapter.notifyDataSetChanged();
    }

    /**
     * Gets the current activity
     * @return
     */
    public Activity getActivity() {
        return activity;
    }
}
