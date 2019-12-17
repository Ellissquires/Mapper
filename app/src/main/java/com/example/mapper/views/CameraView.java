package com.example.mapper.views;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
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
    CacheHandler cache = CacheHandler.getInstance();//Singleton instance handled in ImagesCache class.

    private Context context;
    private Activity activity;


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
        mRecyclerView = (RecyclerView) findViewById(R.id.visit_gallery);
        // set up the RecyclerView
        int numberOfColumns = 3;
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, numberOfColumns));
        mAdapter= new ImageAdapter(myPictureList);
        mRecyclerView.setAdapter(mAdapter);

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                initData();
            }
        });

        mAdapter.notifyDataSetChanged();

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
                if (android.os.Build.VERSION.SDK_INT >= 23) {
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
     * Finishes the activity and bundles the filename into the data.
     * @param filePath the file path to the picture taken.
     */
    private void finishActivityAndReturn (String filePath) {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("filename", filePath);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
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

    public void saveImage(List<File> mFile) {
        File storageDir = new File((getApplicationContext().getExternalFilesDir(null).getAbsolutePath()) + "/Mapper/" + title + "/");

        for(File file: mFile){
            //         Create an image file name
            Timestamp date = new Timestamp(System.currentTimeMillis());

            String timestamp = title + date.toString();

            if (!storageDir.exists())
                storageDir.mkdirs();
            File newFile = new File(storageDir, (timestamp + ".jpeg"));

            try {
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                cache.addToCache(file.getAbsolutePath(), bitmap);

                FileOutputStream output = new FileOutputStream(newFile);

                // Compress into png format image from 0% - 100%
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, output);
                output.flush();
                output.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            // Finish and return once saved.
            finishActivityAndReturn(newFile.toURI().toString());
        }
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
                final List<File> imageList = new ArrayList<>();
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        imageList.addAll(imageFiles);
                        List<String>imgs = ImageFetchService.saveImage(imageList, context , title, cache);
                        if (imgs.size() > 0) {
                            finishActivityAndReturn(imgs.get(0));
                        }
                    }
                });
            }

            @Override
            public void onCanceled(EasyImage.ImageSource source, int type) {

            }
        });
    }

    private void onPhotosReturned(List<File> returnedPhotos) {
        myPictureList.addAll(ImageFetchService.getImageElements(returnedPhotos));
        // we tell the adapter that the data is changed and hence the grid needs
        // refreshing
        mAdapter.notifyDataSetChanged();
        mRecyclerView.scrollToPosition(returnedPhotos.size() - 1);
    }

    public Activity getActivity() {
        return activity;
    }
}
