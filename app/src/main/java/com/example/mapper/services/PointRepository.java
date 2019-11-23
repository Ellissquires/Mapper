package com.example.mapper.services;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.mapper.services.database.ApplicationDatabase;
import com.example.mapper.services.models.Point;
import com.example.mapper.services.models.PointDAO;
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
        new InsertPointAsyncTask(pointDAO).execute(point);
    }


    /**
     * it returns the value of the live data
     * @return
     */

    public LiveData<List<Point>> getAllPoints() {
        LiveData<List<Point>> points = pointDAO.getAllPoints();
        return points;
    }

    static class InsertPointAsyncTask extends AsyncTask<Point, Void, Void> {
        private PointDAO asyncPointDao;

        private InsertPointAsyncTask(PointDAO dao){
            asyncPointDao = dao;
        }

        @Override
        protected Void doInBackground(final Point... params){
            asyncPointDao.insert(params[0]);

            return null;
        }
    }

}

