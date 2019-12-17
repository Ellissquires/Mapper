package com.example.mapper.services.models;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;


/**
 * @author      Thomas Croasdale <tecroasdale@gmail.com>
 * Room DAO for the com.example.mapper.services.models.PicturePoint model
 */

@Dao
public interface PicturePointDAO {

    @Insert
    void insertAll(PicturePoint... points);

    @Insert
    void insert(PicturePoint points);

    @Insert
    void delete(PicturePoint points);

    @Delete
    void deleteAll(PicturePoint... points);

    @Query("SELECT COUNT(*) FROM picturePoints")
    int count();

    /**
     * Returns the list of points associated with the given pathID
     *
     * @param pathId a com.example.mapper.services.models.Path id
     */
    @Query("SELECT picturePoints.id, picturePoints.pointId, picturePoints.pictureURI FROM picturePoints INNER JOIN points on points.id=picturePoints.pointId WHERE pathId=:pathId")
    LiveData<List<PicturePoint>> findPicturePointsOnPath(final long pathId);

    @Query("SELECT * FROM picturePoints WHERE pointId=:pointId")
    LiveData<PicturePoint> getPicturePoint(final long pointId);
}
