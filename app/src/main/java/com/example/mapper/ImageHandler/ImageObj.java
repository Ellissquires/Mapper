package com.example.mapper.ImageHandler;

import android.graphics.Bitmap;

import java.io.File;

public class ImageObj {

    public File file=null;
    public Bitmap bitmap = null;

    public ImageObj(File fileX) {
        file= fileX;
    }
}