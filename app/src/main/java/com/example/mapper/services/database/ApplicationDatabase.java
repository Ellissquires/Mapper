package com.example.mapper.services.database;

import android.content.Context;
import android.os.AsyncTask;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.mapper.services.models.Path;
import com.example.mapper.services.models.PathDAO;
import com.example.mapper.services.models.PicturePoint;
import com.example.mapper.services.models.PicturePointDAO;
import com.example.mapper.services.models.Point;
import com.example.mapper.services.models.PointDAO;
import com.example.mapper.services.models.Visit;
import com.example.mapper.services.models.VisitDAO;


@Database(entities = {Point.class, Path.class, Visit.class, PicturePoint.class}, version = 6, exportSchema = false)
@TypeConverters({Converters.class})

public abstract class ApplicationDatabase extends RoomDatabase {

    // Define Model DAOs
    public abstract PointDAO pointDao();
    public abstract PathDAO pathDao();
    public abstract VisitDAO visitDao();
    public abstract PicturePointDAO picturePointDAO();

    private static volatile ApplicationDatabase INSTANCE;


    /**
     * Creates (if not previously created) or fetches the application database
     * @param context The context of the application
     * @return The ApplicationDatabase object
     */
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

    /**
     * Called when the database is opened.
     */
    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);

            new PopulateWithTestVisitsAsync(INSTANCE).execute();

        }
    };

    /**
     * populates the database with test data.
     */
    private static class PopulateWithTestVisitsAsync extends AsyncTask<Void, Void, Void> {

        private final VisitDAO mDao;

        // Change this to false when the database can be persistent between sessions.
        private boolean mDestroyDatabase = false;

        PopulateWithTestVisitsAsync(ApplicationDatabase db) {
            mDao = db.visitDao();
        }

        @Override
        protected Void doInBackground(final Void... params) {
            if (mDestroyDatabase) {
                mDao.deleteAll();

            }
            return null;
        }
    }



}