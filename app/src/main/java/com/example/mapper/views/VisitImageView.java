package com.example.mapper.views;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mapper.ImageHandler.CacheHandler;
import com.example.mapper.ImageHandler.ImageAdapter;
import com.example.mapper.ImageHandler.ImageObj;
import com.example.mapper.R;
import com.example.mapper.services.ImageFetchService;

import java.io.File;
import java.util.List;

public class VisitImageView extends AppCompatActivity {

    CacheHandler cache = CacheHandler.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);

        Bundle bundle = getIntent().getExtras();
        File element = (File) bundle.get("image");
        String tag = (String) bundle.get("tag");
        if(element != null){
            ImageView imageView = findViewById(R.id.image);
            Bitmap myBitmap = BitmapFactory.decodeFile(element.getAbsolutePath());
            imageView.setImageBitmap(myBitmap);
        }
    }

}