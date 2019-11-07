package com.example.mapper.services.models;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;
@Dao
public interface PathDAO {

    @Insert
    void insertAll(Path... paths);

    @Insert
    long insert(Path paths);

    @Insert
    void delete(Path paths);

    @Query("SELECT * FROM points WHERE pathId=:pathId")
    List<Point> findPointsOnPath(final int pathId);

}