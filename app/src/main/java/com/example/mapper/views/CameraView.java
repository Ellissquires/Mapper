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

    private Visit mVisit;
    private String title;
    private static final int REQUEST_CAMERA = 7500;

    private List<ImageObj> myPictureList = new ArrayList<>();
    private RecyclerView.Adapter  mAdapter;
    private RecyclerView mRecyclerView;
    private TextView prompt;

    CacheHandler cache = CacheHandler.getInstance();

    private Context context;
    private Activity activity;

    Handler handler = new Handler();

    ArrayList<String> filePath = new ArrayList<>();

    private void initEasyImage(){
       EasyImage.configuration(this)
               .setImagesFolderName("Mapper")
               .setCopyTakenPhotosToPublicGalleryAppFolder(true)
               .setCopyPickedImagesToPublicGalleryAppFolder(true)
               .setAllowMultiplePickInGallery(true);
   }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        Bundle extras = getIntent().getExtras();
        mVisit = (Visit) extras.getParcelable(EXTRA_VISIT);

        context = this;

        title = mVisit.getTitle();
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

        // set up the RecyclerView
        int numberOfColumns = 3;
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

        ImageButton fab_gallery = (ImageButton) findViewById(R.id.fab_gallery);
        fab_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                EasyImage.openGallery(getActivity(), 0);
            }
        });

        ImageButton fab_camera = (ImageButton) findViewById(R.id.camera);
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

    @Override
    public void onBackPressed()
    {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("filename", filePath);
        setResult(Activity.RESULT_OK, returnIntent);
        super.onBackPressed();
    }

    /**
     * Cancels this activity and returns as such.
     */
    private void cancelActivity () {
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, returnIntent);
        finish();
    }

    /**
     * artificial init of the data so to upload some images as a starting point
     */
    private void initData() {
        File storageDir = new File((getApplicationContext().getExternalFilesDir(null).getAbsolutePath()) + "/Mapper/" + title + "/");

        if (storageDir.exists()){

            List<File> imageFiles = (Arrays.asList(storageDir.listFiles()));
            myPictureList.addAll(ImageFetchService.getImageElements(imageFiles));

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
                onPhotosReturned(imageFiles);

            }

            @Override
            public void onCanceled(EasyImage.ImageSource source, int type) {

            }
        });
    }

    private void onPhotosReturned(final List<File> returnedPhotos) {

        myPictureList.addAll(ImageFetchService.getImageElements(returnedPhotos));
        for(File file: returnedPhotos){
            filePath.add(file.getAbsolutePath());
        }
        mRecyclerView.setVisibility(View.VISIBLE);
        prompt.setVisibility(View.GONE);

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                ImageFetchService.saveImage(returnedPhotos, context , title, cache);
            }
        });


        // we tell the adapter that the data is changed and hence the grid needs
        // refreshing
        mAdapter.notifyDataSetChanged();
        mRecyclerView.scrollToPosition(returnedPhotos.size() - 1);
    }

    public Activity getActivity() {
        return activity;
    }
}
