package com.example.mapper.services.models;

// import statements
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

/**
 * @author      Ellis Squires <ellis.squires@gmail.com>
 * Room DAO fop the com.example.mapper.services.models.Visit model
 */

@Dao
public interface VisitDAO {

    @Insert
    void insertAll(Visit... visits);

    @Insert
    long insert(Visit visits);

    @Insert
    void delete(Visit visits);

    @Query("DELETE FROM visits")
    void deleteAll();

    /**
     * Return all the visits
     */
    @Query("SELECT * FROM visits")
    LiveData<List<Visit>> getAllVisits();
}
