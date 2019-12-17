package com.example.mapper.services;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.mapper.services.database.ApplicationDatabase;
import com.example.mapper.services.models.Path;
import com.example.mapper.services.models.PathDAO;
import com.example.mapper.services.models.Visit;
import com.example.mapper.services.models.VisitDAO;

import java.util.List;

public class VisitRepository extends ViewModel {

    private final VisitDAO visitDAO;
    private final PathDAO pathDAO;
    private long visitID;

    public VisitRepository(Application application) {
        ApplicationDatabase db = ApplicationDatabase.getDatabase(application);
        visitDAO = db.visitDao();
        pathDAO = db.pathDao();
    }

    public void createVisit(Visit visit){
        new InsertVisitAsyncTask(visitDAO, pathDAO).execute(visit);
    }

    public void deleteVisit(Visit visit){
        new DeleteVisitAsyncTask(visitDAO).execute(visit);
    }

    public void updateVisit(Visit visit){
        new UpdateVisitAsyncTask(visitDAO).execute(visit);
    }

    private class UpdateVisitAsyncTask extends AsyncTask<Visit, Void, Void> {
        private VisitDAO asyncVisitDao;

        private UpdateVisitAsyncTask(VisitDAO vDao){
            asyncVisitDao = vDao;
        }

        @Override
        protected Void doInBackground(final Visit... params){
            asyncVisitDao.update(params[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }
    }

    private class InsertVisitAsyncTask extends AsyncTask<Visit, Void, Void> {
        private VisitDAO asyncVisitDao;
        private PathDAO asyncPathDao;


        private InsertVisitAsyncTask(VisitDAO vDao, PathDAO pDao){
            asyncPathDao = pDao;
            asyncVisitDao = vDao;
        }

        @Override
        protected Void doInBackground(final Visit... params){
            long pathID = asyncPathDao.insert(new Path());
            Visit visit = params[0];
            visit.setPathId(pathID);
            long id = asyncVisitDao.insert(visit);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }
    }

    private class DeleteVisitAsyncTask extends AsyncTask<Visit, Void, Void> {
        private VisitDAO asyncVisitDao;

        private DeleteVisitAsyncTask(VisitDAO vDao){
            asyncVisitDao = vDao;
        }

        @Override
        protected Void doInBackground(final Visit... params){
            asyncVisitDao.delete(params[0]);
            return null;
        }
    }

    public LiveData<List<Visit>> getAllVisits() {
        return visitDAO.getAllVisits();
    }


}