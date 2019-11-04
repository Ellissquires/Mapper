package com.example.mapper.services.models;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class Point {
    @PrimaryKey public final int id;

    public final double lat;
    public final double lng;
    public final double pressure;
    public final double temperature;

    public Point(final int id, double lat, double lng, double pressure, double temperature) {
        this.id = id;
        this.lat = lat;
        this.lng = lng;
        this.pressure = pressure;
        this.temperature = temperature;
    }
}

