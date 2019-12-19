package com.example.mapper.ImageHandler;

import java.io.File;

/**
 * represents an ImageObj
 * @author Neville Kitala
 * @version 1.0
 * @since 1.0
 */
public class ImageObj {

    public File file;
    public String tag;
    public Boolean clickable = true;

    /**
     * constructor method for the ImageObj class
     * @param fileX
     */
    public ImageObj(File fileX, Boolean bool) {
        file= fileX;
        clickable = bool;
    }

    public String getTag(){
        return tag;
    }

    public Boolean getClickable() { return clickable;}

    public void setTag(String tag){
        this.tag = tag;
    }
}