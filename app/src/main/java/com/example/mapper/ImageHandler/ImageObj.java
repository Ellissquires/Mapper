package com.example.mapper.ImageHandler;

import java.io.File;

public class ImageObj {

    public File file;
    public String tag;

    /**
     * constructor method for the ImageObj class
     * @param fileX
     */
    public ImageObj(File fileX) {
        file= fileX;
    }

    public String getTag(){
        return tag;
    }

    public void setTag(String tag){
        this.tag = tag;
    }
}