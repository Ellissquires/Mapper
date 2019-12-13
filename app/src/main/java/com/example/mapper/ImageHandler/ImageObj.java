package com.example.mapper.ImageHandler;

import java.io.File;

public class ImageObj {
    public int image=-1;
    public File file=null;


    public ImageObj(int image) {
        this.image = image;

    }

    public ImageObj(File fileX) {
        file= fileX;
    }
}