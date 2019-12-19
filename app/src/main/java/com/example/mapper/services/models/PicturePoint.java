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
 * Room Database entity for the com.example.mapper.services.models.PicturePoint model
 */

/**
 * Setup entity one-to-many relationship between Path and Point models
 */
@Entity(tableName = "picturePoints",
        indices = {@Index("pointId")})
public class PicturePoint implements Parcelable {

    @PrimaryKey(autoGenerate = true)
    public int id;
    private int pointId;
    private String pictureURI;

    public PicturePoint(int pointId, String pictureURI){
        this.pointId = pointId;
        this.pictureURI = pictureURI;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPointId() { return pointId; }

    public void setPointId(int pointId) { this.pointId = pointId; }

    public String getPictureURI () { return pictureURI; }

    public void setPictureURI (String pictureURI) { this.pictureURI = pictureURI; }

    // Below are functions for creating a parcelable extra. used for passing data from PathRecorderService to the Activity

    public static final Creator<PicturePoint> CREATOR = new Creator<PicturePoint>() {
        @Override
        public PicturePoint createFromParcel(Parcel in) {
            return new PicturePoint(in);
        }

        @Override
        public PicturePoint[] newArray(int size) {
            return new PicturePoint[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    public PicturePoint(Parcel p) {
        this.id = p.readInt();
        this.pointId = p.readInt();
        this.pictureURI = p.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeInt(this.pointId);
        dest.writeString(this.pictureURI);
    }
}
