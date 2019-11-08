package com.example.mapper.services.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "visits")
public class Visit {

    @PrimaryKey(autoGenerate = true)

    private int id = 0;
    private String title;
    private int pathId;
    private Date visitDate;


    public Visit(){

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }



}