package com.example.mapper.services.models;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;


/**
 * @author      Ellis Squires <ellis.squires@gmail.com>
 * Room DAO fop the com.example.mapper.services.models.Point model
 */

@Dao
public interface PointDAO {

    @Insert
    void insertAll(Point... points);

    @Insert
    long insert(Point points);

    @Insert
    void delete(Point points);

    @Delete
    void deleteAll(Point... points);

    @Query("SELECT COUNT(*) FROM points")
    int count();

    /**
     * Return point from id
     */
    @Query("SELECT * FROM points WHERE id=:id LIMIT 1")
    LiveData<Point> getPoint(final long id);
}
