package com.example.mapper.services;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.mapper.services.database.ApplicationDatabase;
import com.example.mapper.services.models.Path;
import com.example.mapper.services.models.PathDAO;
import com.example.mapper.services.models.Point;

public class PathRepository extends ViewModel {

    private final PathDAO pathDAO;

    public PathRepository(Application application) {
        ApplicationDatabase db = ApplicationDatabase.getDatabase(application);
        pathDAO = db.pathDao();
    }

    public void createPath(Point... points){
        new InsertPathAsyncTask(pathDAO).execute(new Path());
    }

    static class InsertPathAsyncTask extends AsyncTask<Path, Void, Void> {
        private PathDAO asyncPathDao;

        private InsertPathAsyncTask(PathDAO dao){
            asyncPathDao = dao;
        }

        @Override
        protected Void doInBackground(final Path... params){
            long pathID = asyncPathDao.insert(params[0]);

            return null;
        }
    }

}