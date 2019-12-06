package com.example.mapper.services.models;

//import statements
import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.util.Date;

/**
 * @author      Ellis Squires <ellis.squires@gmail.com>
 * Room Database entity for the com.example.mapper.services.models.Visit model
 * Implements parcelable so it can be passed with an intent
 */

@Entity(tableName = "visits")
public class Visit implements Parcelable {
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Visit createFromParcel(Parcel in) {
            return new Visit(in);
        }

        public Visit[] newArray(int size) {
            return new Visit[size];
        }
    };

    @PrimaryKey(autoGenerate = true)
    private long id;
    private String title;
    private String description;
    private Date visitDate;
    private double distance;
    private long pathId;


    public Visit(String title,String description, Date visitDate, double distance){
        this.title = title;
        this.description = description;
        this.visitDate = visitDate;
        this.distance = distance;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getVisitDate() {
        return visitDate;
    }

    public void setVisitDate(Date visitDate) {
        this.visitDate = visitDate;
    }

    public long getPathId() {
        return pathId;
    }

    public void setPathId(long pathId) {
        this.pathId = pathId;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    // Parcelling part
    public Visit(Parcel in){
        this.id = in.readLong();
        this.title = in.readString();
        this.description = in.readString();
        this.visitDate = new Date(in.readLong());
        this.distance = in.readDouble();
    }

    // Parcable overrides
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.title);
        dest.writeString(this.description);
        dest.writeLong(visitDate.getTime());
        dest.writeDouble(this.distance);
    }

    @Override
    public String toString() {
        return "Visit{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", visitDate='" + visitDate.toString() + '\'' +
                ", distance='" + distance + '\'' +
                '}';
    }
}