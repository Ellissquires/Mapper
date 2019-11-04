package com.example.mapper.services.database;


import com.example.mapper.services.models.Point;

@android.arch.persistence.room.Database(entities = {Point.class}, version = 1, exportSchema = false)

public abstract class RoomDatabase {


}
