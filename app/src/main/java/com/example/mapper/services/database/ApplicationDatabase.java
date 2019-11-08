package com.example.mapper.services.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.mapper.services.models.Path;
import com.example.mapper.services.models.PathDAO;
import com.example.mapper.services.models.Point;
import com.example.mapper.services.models.PointDAO;
import com.example.mapper.services.models.Visit;
import com.example.mapper.services.models.VisitDAO;

@Database(entities = {Point.class, Path.class, Visit.class}, version = 1, exportSchema = false)
@TypeConverters({Converters.class})

public abstract class ApplicationDatabase extends RoomDatabase {

    public abstract PointDAO pointDao();
    public abstract PathDAO pathDao();
    public abstract VisitDAO visitDao();

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