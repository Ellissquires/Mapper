package com.example.mapper.services.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "points")
public class Point {

    @PrimaryKey(autoGenerate = true)

    private int id = 0;
    private double lat;
    private double lng;

    public Point(double lat, double lng){
        this.lat = lat;
        this.lng = lng;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getLat() {
        return lat;
    }

    public void setLatg(int lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(int lat) {
        this.lng = lng;
    }
}
