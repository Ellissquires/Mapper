package com.example.mapper.services.models;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

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

    @Query("SELECT * FROM visits")
    LiveData<List<Visit>> getAllVisits();
}
