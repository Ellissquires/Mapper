package com.example.mapper.services;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

    public static List<ImageObj> getImageElements(List<File> returnedPhotos) {
        List<ImageObj> imageElementList= new ArrayList<>();
        for (File file: returnedPhotos){
            ImageObj element= new ImageObj(file);
            imageElementList.add(element);
        }
        return imageElementList;
    }

    public static void cacheImages(final CacheHandler cache, final Context context){
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
                        Bitmap bitmap = decodeSampledBitmapFromResource(file, 150, 150);
                        cache.addToCache(path, bitmap);
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

    public static List<String> saveImage(final List<File> mFile, Context context, String title, CacheHandler cache) {
        File storageDir = new File((context.getExternalFilesDir(null).getAbsolutePath()) + "/Mapper/" + title + "/");

        List<String> filenames = new ArrayList<>();
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

                filenames.add(newFile.toURI().toString());
            }
            catch (Exception e) {
                e.printStackTrace();
            }

        }
        return filenames;

    }//saveImage

}
