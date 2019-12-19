package com.example.mapper.services;

// Imports
import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.example.mapper.services.database.ApplicationDatabase;
import com.example.mapper.services.models.Point;
import com.example.mapper.services.models.PointDAO;
import com.example.mapper.services.models.RepoInsertCallback;

public class PointRepository extends ViewModel {
    private final PointDAO pointDAO;

    public PointRepository(Application application) {
        ApplicationDatabase db = ApplicationDatabase.getDatabase(application);
        pointDAO = db.pointDao();
    }

    /**
     * Creates a Point record in the database asynchronously
     * @param point point to be inserted
     */
    public void createPoint(Point point) {
        new InsertPointAsyncTask(pointDAO, null).execute(point);
    }

    /**
     * Creates a Point record in the database asynchronously with a callback
     * @param point point to be inserted
     * @param riCB callback
     */
    public void createPoint(Point point, RepoInsertCallback riCB) {
        new InsertPointAsyncTask(pointDAO, riCB).execute(point);
    }

    public LiveData<Point> getPoint(long id){
        return pointDAO.getPoint(id);
    }

    /**
     * Handles inserting points asynchronously, when an insertion task has finishing running the
     * passed callback is run.
     */
    static class InsertPointAsyncTask extends AsyncTask<Point, Void, Long> {
        private PointDAO asyncPointDao;
        private RepoInsertCallback mCallback;

        private InsertPointAsyncTask(PointDAO dao, RepoInsertCallback riCB){
            asyncPointDao = dao;
            mCallback = riCB;
        }

        @Override
        protected Long doInBackground(final Point... params){
            return asyncPointDao.insert(params[0]);
        }

        protected void onPostExecute(Long result)  {
            // If a callback is available run it
            if (mCallback != null) {
                mCallback.OnFinishInsert(result);
            }
        }
    }
}

