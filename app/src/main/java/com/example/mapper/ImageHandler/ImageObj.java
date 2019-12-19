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

    /**
     * constructor method for the ImageObj class
     * @param fileX
     */
    public ImageObj(File fileX) {
        file= fileX;
    }
}