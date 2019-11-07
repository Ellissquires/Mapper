package com.example.mapper.services.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.mapper.services.models.Point;
import com.example.mapper.services.models.PointDAO;

@Database(entities = {Point.class}, version = 1, exportSchema = false)
public abstract class ApplicationDatabase extends RoomDatabase {

    public abstract PointDAO pointDao();

    private static volatile ApplicationDatabase INSTANCE;

    public static ApplicationDatabase getDatabase(final Context context){
        if (INSTANCE == null){
            synchronized (ApplicationDatabase.class) {
                if(INSTANCE == null){
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            ApplicationDatabase.class, "application_database")
                            .fallbackToDestructiveMigration()
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }

        }
        return INSTANCE;
    }

    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);

            // do any init operation about any initialisation here
        }
    };

}