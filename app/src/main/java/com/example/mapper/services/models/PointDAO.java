package com.example.mapper.services.models;

import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Update;

public interface PointDAO {

    @Insert
    void insert(Point repo);

    @Update
    void update(Point... repos);

    @Delete
    void delete(Point... repos);
}
