package com.example.mapper.views;

import android.hardware.Camera;

import androidx.appcompat.app.AppCompatActivity;

public class CameraView extends AppCompatActivity {

    /** A safe way to get an instance of the Camera object. */
    @SuppressWarnings("deprecation")
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

}
