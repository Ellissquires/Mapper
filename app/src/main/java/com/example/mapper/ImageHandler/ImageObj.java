package com.example.mapper.ImageHandler;

import android.graphics.Bitmap;

import java.io.File;

public class ImageObj {
    public int image=-1;
    public File file=null;
    public Bitmap bitmap = null;

    public ImageObj(int image) {
        this.image = image;

    }

    public ImageObj(File fileX) {
        file= fileX;
    }

    public ImageObj(Bitmap bitmap) {
        this.bitmap = bitmap;

    }
}