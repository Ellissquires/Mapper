package com.example.mapper.services;

import android.app.Application;
import android.graphics.Picture;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.mapper.services.database.ApplicationDatabase;
import com.example.mapper.services.models.PicturePoint;
import com.example.mapper.services.models.PicturePointDAO;

import java.util.List;

public class PicturePointRepository extends ViewModel {
    private final PicturePointDAO picturePointDAO;

    public PicturePointRepository(Application application) {
        ApplicationDatabase db = ApplicationDatabase.getDatabase(application);
        picturePointDAO = db.picturePointDAO();
    }

    public void createPicturePoint(PicturePoint picturePoint) {
        new InsertPointAsyncTask(picturePointDAO).execute(picturePoint);
    }

    public void updatePicturePoint(PicturePoint picturePoint){
        new UpdatePointAsyncTask(picturePointDAO).execute(picturePoint);
    }


    public LiveData<PicturePoint> getPicturePoint(long pointId) {
        LiveData<PicturePoint> point = picturePointDAO.getPicturePoint(pointId);
        return point;
    }

    /**
     * it returns the value of the live data
     * @return
     */

    public LiveData<List<PicturePoint>> getAllPoints(long pathId) {
        LiveData<List<PicturePoint>> points = picturePointDAO.findPicturePointsOnPath(pathId);
        return points;
    }

    static class InsertPointAsyncTask extends AsyncTask<PicturePoint, Void, Void> {
        private PicturePointDAO asyncPicturePointDao;

        private InsertPointAsyncTask(PicturePointDAO dao){
            asyncPicturePointDao = dao;
        }

        @Override
        protected Void doInBackground(final PicturePoint... params){
            asyncPicturePointDao.insert(params[0]);

            return null;
        }
    }

    static class UpdatePointAsyncTask extends AsyncTask<PicturePoint, Void, Void> {
        private PicturePointDAO asyncPicturePointDao;

        private UpdatePointAsyncTask(PicturePointDAO dao){
            asyncPicturePointDao = dao;
        }

        @Override
        protected Void doInBackground(final PicturePoint... params){
            asyncPicturePointDao.update(params[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }
    }
}

