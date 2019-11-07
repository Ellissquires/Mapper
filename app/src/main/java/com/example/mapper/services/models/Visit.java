package com.example.mapper.services.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "visits")
public class Visit {

    @PrimaryKey(autoGenerate = true)

    private int id = 0;


    public Visit(){

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }



}