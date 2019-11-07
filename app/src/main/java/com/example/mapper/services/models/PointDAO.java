package com.example.mapper.services.models;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;
@Dao
public interface PointDAO {

    @Insert
    void insertAll(Point... points);

    @Insert
    void insert(Point points);

    @Insert
    void delete(Point points);

    @Query("SELECT * FROM points")
    LiveData<List<Point>> getAllPoints();

    @Delete
    void deleteAll(Point... points);

    @Query("SELECT COUNT(*) FROM points")
    int count();
}
