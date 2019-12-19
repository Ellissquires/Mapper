package com.example.mapper.services;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.mapper.ImageHandler.CacheHandler;
import com.example.mapper.ImageHandler.ImageObj;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pl.aprilapps.easyphotopicker.EasyImage;

/**
 * @author Neville Kitala
 * @version 1.0
 * @since 1.0
 */

public class ImageFetchService {

    private static final int REQUEST_READ_EXTERNAL_STORAGE = 2987;
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 7829;

    public static void imagePermissions(final Context context, Activity activity){
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    androidx.appcompat.app.AlertDialog.Builder alertBuilder = new androidx.appcompat.app.AlertDialog.Builder(context);
                    alertBuilder.setCancelable(true);
                    alertBuilder.setTitle("Permission necessary");
                    alertBuilder.setMessage("External storage permission is necessary");
                    alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_EXTERNAL_STORAGE);
                        }
                    });
                    androidx.appcompat.app.AlertDialog alert = alertBuilder.create();
                    alert.show();

                } else {
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_EXTERNAL_STORAGE);
                }

            }
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    androidx.appcompat.app.AlertDialog.Builder alertBuilder = new androidx.appcompat.app.AlertDialog.Builder(context);
                    alertBuilder.setCancelable(true);
                    alertBuilder.setTitle("Permission necessary");
                    alertBuilder.setMessage("Writing external storage permission is necessary");
                    alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE);
                        }
                    });
                    androidx.appcompat.app.AlertDialog alert = alertBuilder.create();
                    alert.show();

                } else {
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE);
                }
            }

        }
    }

    /**
     * Gets all file elements provided as inputs and converts them into a list of image objects.
     * @param returnedPhotos
     * @param source
     * @return
     */
    public static List<ImageObj> getImageElements(List<File> returnedPhotos, EasyImage.ImageSource source, Boolean bool) {
        String rotateTag = "rotate";
        String uprightTag = "upright";

//        iterate over al files and convert them to objects
        List<ImageObj> imageElementList= new ArrayList<>();
        for (File file: returnedPhotos){
            ImageObj element= new ImageObj(file, bool);

//            handle the image tags to determine their orientation
            if(source == EasyImage.ImageSource.CAMERA){
                element.setTag(rotateTag);
            }
            else{
                element.setTag(uprightTag);
            }
            imageElementList.add(element);
        }
        return imageElementList;
    }

    /**
     * Function to cache images at initialisation of the app to ensure that the app loads quickle
     * @param cache
     * @param context
     */
    public static void cacheImages(final CacheHandler cache, final Context context, final CacheHandler myCache){
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                File storageDir = new File((context.getExternalFilesDir(null).getAbsolutePath()) + "/Mapper/");
                if (storageDir.exists()){
                    File[] files = storageDir.listFiles();
                    List<File> folders = (Arrays.asList(files));
                    List<File> imageFile = new ArrayList<>();
                    for(File file : folders){
                        File[] visitFolder = file.listFiles();
                        imageFile.addAll(Arrays.asList(visitFolder));
                    }

                    for(File file: imageFile){
                        String path = file.getAbsolutePath();
                        Bitmap bitmap = cache.getFromCache(path);
                        if(bitmap == null)
                            bitmap = decodeSampledBitmapFromResource(file, 150, 150);
                        myCache.addToCache(path, bitmap);
                    }
                }
            }
        });
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromResource(File file, int reqWidth, int reqHeight) {

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), options);

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(file.getAbsolutePath(), options);
    }

    public static void deleteImageFolder (final String title, final Context context){
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                File storageDir = new File((context.getExternalFilesDir(null).getAbsolutePath()) + "/Mapper/" + title);
                if (storageDir.isDirectory())
                    for (File child : storageDir.listFiles())
                        (child).delete();

                storageDir.delete();
            }
        });
    }

    /**
     * This folder alows you to deld
     * @param originalTitle
     * @param newTitle
     * @param context
     * @param cache
     */
    public static void editImageFolder (final String originalTitle, final String newTitle, final Context context, final CacheHandler cache){

        File storageDir = new File((context.getExternalFilesDir(null).getAbsolutePath()) + "/Mapper/" + originalTitle);
        if (storageDir.isDirectory()) {
            List<File> fileList = Arrays.asList(storageDir.listFiles());
            saveImage(fileList, context, newTitle, cache, null, new SaveImagesAsyncTask.AsyncResponse() {
                @Override
                public void processFinish(){
                }
            });
        }
        deleteImageFolder(originalTitle, context);
    }

    /**
     * Image fetch service is used to save images to the app content folder asynchronously.
     */
    public static class SaveImagesAsyncTask extends AsyncTask<Void, Void, Void> {

        final List<File> mFile;
        final Context context;
        final String title;
        final CacheHandler cache;
        final EasyImage.ImageSource source;

        public interface AsyncResponse {
            void processFinish();
        }

        public AsyncResponse delegate = null;

        public SaveImagesAsyncTask(final List<File> mFile, final Context context, final String title, final CacheHandler cache, final EasyImage.ImageSource source, AsyncResponse delegate){
            this.mFile = mFile;
            this.context = context;
            this.title = title;
            this.cache = cache;
            this.source = source;
            this.delegate = delegate;
        }

        @Override
        protected Void doInBackground(Void... params){

            File storageDir = new File((context.getExternalFilesDir(null).getAbsolutePath()) + "/Mapper/" + title + "/");

            for(File file: mFile) {
                //         Create an image file name
                Timestamp date = new Timestamp(System.currentTimeMillis());

                String timestamp = title + date.toString();

                if (!storageDir.exists())
                    storageDir.mkdirs();
                File newFile = new File(storageDir, (timestamp + ".jpeg"));

                try {
                    Bitmap unrotatedBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                    Bitmap bitmap;
                    if (source.equals(EasyImage.ImageSource.CAMERA)) {
                        bitmap = rotateBitmap(unrotatedBitmap, 90);
                    } else {
                        bitmap = unrotatedBitmap;
                    }
                    cache.addToCache(file.getAbsolutePath(), bitmap);

                    FileOutputStream output = new FileOutputStream(newFile);

                    // Compress into png format image from 0% - 100%
                    bitmap.compress(Bitmap.CompressFormat.PNG, 10, output);
                    output.flush();
                    output.close();

                    cache.addToCache(newFile.toURI().toString(), bitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            delegate.processFinish();
        }
    }

    /**
     * This calls the async method and provides all the nessecary parameters
     * @param mFile
     * @param context
     * @param title
     * @param cache
     * @param source
     * @param res
     */
    public static void saveImage(final List<File> mFile, final Context context, final String title, final CacheHandler cache, final EasyImage.ImageSource source, SaveImagesAsyncTask.AsyncResponse res) {

        new SaveImagesAsyncTask(mFile, context, title, cache, source, res).execute();

    }//saveImage

    /**
     * Gets an image from resources and converts it into an icon
     * @param bm
     * @param w
     * @return
     */
    public static Bitmap getIcon(Bitmap bm, int w){
        int width = bm.getWidth();
        float scaleWidth = ((float) w) / width;
        // create a matrix for the manipulation
        Matrix matrix = new Matrix();
        // resize the bit map
        matrix.postScale(scaleWidth, scaleWidth);
        // recreate the new Bitmap
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, width, matrix, true);
        return resizedBitmap;
    }

    /**
     * Rotates an image by the angle defined in the parameters.
     * @param source
     * @param angle
     * @return
     */
    public static Bitmap rotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

}
