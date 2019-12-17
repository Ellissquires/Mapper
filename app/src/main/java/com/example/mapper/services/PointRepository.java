package com.example.mapper.services;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.mapper.services.database.ApplicationDatabase;
import com.example.mapper.services.models.Point;
import com.example.mapper.services.models.PointDAO;
import com.example.mapper.services.models.RepoInsertCallback;
import com.example.mapper.services.models.Visit;
import com.example.mapper.services.models.VisitDAO;

import java.util.List;

public class PointRepository extends ViewModel {
    private final PointDAO pointDAO;

    public PointRepository(Application application) {
        ApplicationDatabase db = ApplicationDatabase.getDatabase(application);
        pointDAO = db.pointDao();
    }

    public void createPoint(Point point) {
        new InsertPointAsyncTask(pointDAO, null).execute(point);
    }

    public void createPoint(Point point, RepoInsertCallback riCB) {
        new InsertPointAsyncTask(pointDAO, riCB).execute(point);
    }



    /**
     * it returns the value of the live data
     * @return
     */

//    public LiveData<List<Point>> getAllPoints() {
//        LiveData<List<Point>> points = pointDAO.getAllPoints();
//        return points;
//    }

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
            if (mCallback != null) {
                mCallback.OnFinishInsert(result);
            }
        }
    }
}

