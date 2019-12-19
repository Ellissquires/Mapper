package com.example.mapper.services;

import android.app.Application;
import android.graphics.Picture;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.mapper.services.database.ApplicationDatabase;
import com.example.mapper.services.models.PicturePoint;
import com.example.mapper.services.models.PicturePointDAO;

import java.net.URI;
import java.util.List;

public class PicturePointRepository extends ViewModel {
    private final PicturePointDAO picturePointDAO;

    public PicturePointRepository(Application application) {
        ApplicationDatabase db = ApplicationDatabase.getDatabase(application);
        picturePointDAO = db.picturePointDAO();
    }

    /**
     * Creates a PicturePoint record in the database asynchronously
     * @param picturePoint
     */
    public void createPicturePoint(PicturePoint picturePoint) {
        new InsertPointAsyncTask(picturePointDAO).execute(picturePoint);
    }

    /**
     * Updates a PicturePoint record in the database asynchronously
     * @param picturePoint
     */
    public void updatePicturePoint(PicturePoint picturePoint){
        new UpdatePointAsyncTask(picturePointDAO).execute(picturePoint);
    }

    /**
     * Returns the PicturePoint record associated with the given point
     * @param pointId
     * @return Point point
     */
    public LiveData<PicturePoint> getPicturePoint(long pointId) {
        LiveData<PicturePoint> point = picturePointDAO.getPicturePoint(pointId);
        return point;
    }

    public LiveData<PicturePoint> getPicturePointFromURI(String uri) {
        LiveData<PicturePoint> point = picturePointDAO.getPicturePointFromURI(uri);
        return point;
    }

    /**
     * Returns all the PicturePoints associated with a path
     * @param pathId
     * @return LiveData<List<PicturePoint>> points
     */
    public LiveData<List<PicturePoint>> getAllPoints(long pathId) {
        LiveData<List<PicturePoint>> points = picturePointDAO.findPicturePointsOnPath(pathId);
        return points;
    }

    /**
     * Handles inserting PicturePoints asynchronously (on a background thread)
     */
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

    /**
     * Handles updating PicturePoints asynchronously (on a background thread)
     */
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

