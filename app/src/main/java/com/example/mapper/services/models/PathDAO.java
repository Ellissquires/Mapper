package com.example.mapper.services.models;
// import statements
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

/**
 * @author      Ellis Squires <ellis.squires@gmail.com>
 * Room DAO fop the com.example.mapper.services.models.Path model
 */

@Dao
public interface PathDAO {

    @Insert
    void insertAll(Path... paths);

    @Insert
    long insert(Path paths);

    @Insert
    void delete(Path paths);

    /**
     * Returns the list of points associated with the given pathID
     *
     * @param pathId a com.example.mapper.services.models.Path id
     */
    @Query("SELECT * FROM points WHERE pathId=:pathId")
    LiveData<List<Point>> findPointsOnPath(final long pathId);

}