package com.example.mapper.services.models;

// import statements
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * @author      Ellis Squires <ellis.squires@gmail.com>
 * Room DAO for the com.example.mapper.services.models.Visit model
 */

@Dao
public interface VisitDAO {

    @Insert
    void insertAll(Visit... visits);

    @Insert
    long insert(Visit visits);

    @Delete
    void delete(Visit visits);

    @Query("DELETE FROM visits")
    void deleteAll();

    @Update
    void update(Visit... visits);

    /**
     * Return all the visits
     */
    @Query("SELECT * FROM visits ORDER BY visitDate DESC")
    LiveData<List<Visit>> getAllVisits();
}
