package com.example.mapper.views;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mapper.R;

import java.io.File;

public class ImagesView extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        Bundle bundle = getIntent().getExtras();
        File element = (File) bundle.get("image");

        if(element != null){
            ImageView imageView = (ImageView) findViewById(R.id.image);
            if (element!=null) {
                Bitmap myBitmap = BitmapFactory.decodeFile(element.getAbsolutePath());
                imageView.setImageBitmap(myBitmap);
            }
        }
    }

}