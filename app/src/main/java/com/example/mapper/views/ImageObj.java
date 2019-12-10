package com.example.mapper.views;

import java.io.File;

class ImageObj {
    int image=-1;
    File file=null;


    public ImageObj(int image) {
        this.image = image;

    }

    public ImageObj(File fileX) {
        file= fileX;
    }
}