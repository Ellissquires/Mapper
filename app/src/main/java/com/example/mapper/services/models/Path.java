package com.example.mapper.services.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "paths")
public class Path {

    @PrimaryKey(autoGenerate = true)

    private int id = 0;


    public Path(){
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }



}