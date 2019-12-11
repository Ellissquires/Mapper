package com.example.mapper.services;

import android.app.Application;
import android.os.AsyncTask;
import android.os.Handler;

import androidx.lifecycle.ViewModel;

import com.example.mapper.services.database.ApplicationDatabase;
import com.example.mapper.services.models.Path;
import com.example.mapper.services.models.PathDAO;
import com.example.mapper.services.models.Point;
import com.example.mapper.services.models.RepoInsertCallback;

import java.util.List;

public class PathRepository extends ViewModel {

    private final PathDAO pathDAO;

    public PathRepository(Application application) {
        ApplicationDatabase db = ApplicationDatabase.getDatabase(application);
        pathDAO = db.pathDao();
    }

    public void createPath(RepoInsertCallback riCB){
        new InsertPathAsyncTask(pathDAO, riCB).execute(new Path());
    }

    public List<Point> getPointsOnPath(long pathId) {
        return pathDAO.findPointsOnPath(pathId);
    }

    static class InsertPathAsyncTask extends AsyncTask<Path, Void, Long> {
        private PathDAO asyncPathDao;
        private RepoInsertCallback mCallback;

        private InsertPathAsyncTask(PathDAO dao, RepoInsertCallback riCB){
            asyncPathDao = dao;
            mCallback = riCB;
        }

        @Override
        protected Long doInBackground(final Path... params){
            return asyncPathDao.insert(params[0]);
        }

        protected void onPostExecute(Long result) {
            mCallback.OnFinishInsert(result);
        }
    }

}
