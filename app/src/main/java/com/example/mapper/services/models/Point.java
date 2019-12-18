package com.example.mapper.services.models;

// import statements
import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

/**
 * @author      Ellis Squires <ellis.squires@gmail.com>
 * Room Database entity for the com.example.mapper.services.models.Point model
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

public class Point implements Parcelable {
    @PrimaryKey(autoGenerate = true)
    public int id;
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

    public void setPressure(double p) { this.pressure = p; }

    public double getPressure(){return this.pressure; }

    public void setTemperature(double t) { this.temperature = t; }

    public double getTemperature(){ return this.temperature; }

    // Below are functions for creating a parcelable extra. used for passing data from PathRecorderService to the Activity
    public static final Creator<Point> CREATOR = new Creator<Point>() {
        @Override
        public Point createFromParcel(Parcel in) {
            return new Point(in);
        }

        @Override
        public Point[] newArray(int size) {
            return new Point[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    public Point(Parcel p) {
        this.id = p.readInt();
        this.pathId = p.readInt();
        this.lat = p.readDouble();
        this.lng = p.readDouble();
        this.pressure = p.readDouble();
        this.temperature = p.readDouble();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeInt(this.pathId);
        dest.writeDouble(this.lat);
        dest.writeDouble(this.lng);
        dest.writeDouble(this.pressure);
        dest.writeDouble(this.temperature);
    }
}
