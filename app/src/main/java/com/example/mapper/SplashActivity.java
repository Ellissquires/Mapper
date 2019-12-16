package com.example.mapper;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mapper.ImageHandler.CacheHandler;
import com.example.mapper.views.VisitListView;
import com.example.mapper.views.VisitView;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        CacheHandler cache = CacheHandler.getInstance();
        cache.initializeCache();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, VisitListView.class);
                startActivity(intent);
                finish();
            }
        }, 500);
    }
}
