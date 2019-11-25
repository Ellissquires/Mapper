package com.example.mapper.services.models;

// import statements
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

/**
 * @author      Ellis Squires <ellis.squires@gmail.com>
 * Room Database entity for the com.example.mapper.models.Point model
 */

/**
 * Setup entity one-to-many relationship between Path and Point models
 */
@Entity(foreignKeys = @ForeignKey(entity = Path.class,
        parentColumns = "id",
        childColumns = "pathId",
        onDelete = CASCADE),
        tableName = "points",
        indices = {@Index("pathId")})

public class Point {

    @PrimaryKey public int id;
    private int pathId;
    private double lat;
    private double lng;
    private double pressure;
    private double temperature;

    public Point(double lat, double lng, double pressure, double temperature, int pathId){
        this.lat = lat;
        this.lng = lng;
        this.pressure = pressure;
        this.temperature = temperature;
        this.pathId = pathId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPathId() {
        return pathId;
    }

    public void setPathId(int pathId) {
        this.pathId = pathId;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public void setPressure(double p) {
        this.pressure = p;
    }

    public double getPressure(){return this.pressure; }

    public void setTemperature(double t) {
        this.temperature = t;
    }

    public double getTemperature(){ return this.temperature; }


}
