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

    public VisitRepository(Application application) {
        ApplicationDatabase db = ApplicationDatabase.getDatabase(application);
        visitDAO = db.visitDao();
        pathDAO = db.pathDao();
    }

    /**
     * Creates a Visit record asynchronously
     * @param visit Visit to be inserted
     */
    public void createVisit(Visit visit){
        new InsertVisitAsyncTask(visitDAO, pathDAO).execute(visit);
    }

    /**
     * Deletes a Visit record asynchronously
     * @param visit Visit to be deleted
     */
    public void deleteVisit(Visit visit){
        new DeleteVisitAsyncTask(visitDAO).execute(visit);
    }

    /**
     * Updates a Visit record asynchronously
     * @param visit Visit to be updated
     */
    public void updateVisit(Visit visit){
        new UpdateVisitAsyncTask(visitDAO).execute(visit);
    }

    /**
     * Handles updating a Visit record in the database asynchronously using a DAO
     */
    private static class UpdateVisitAsyncTask extends AsyncTask<Visit, Void, Void> {
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

    /**
     * Handles inserting a Visit record in the database asynchronously using a DAO, requires both the
     * path and visit DAO's because when a visit is created its path is created along with it; and
     * relationship is generated.
     */
    private static class InsertVisitAsyncTask extends AsyncTask<Visit, Void, Void> {
        private VisitDAO asyncVisitDao;
        private PathDAO asyncPathDao;


        private InsertVisitAsyncTask(VisitDAO vDao, PathDAO pDao){
            asyncPathDao = pDao;
            asyncVisitDao = vDao;
        }

        @Override
        protected Void doInBackground(final Visit... params){
            // insert the visit path
            long pathID = asyncPathDao.insert(new Path());
            Visit visit = params[0];
            // Updats the visit with the generated path ID
            visit.setPathId(pathID);
            long id = asyncVisitDao.insert(visit);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }
    }

    /**
     * Handles deleting a Visit record in the database asynchronously using a DAO
     */
    private static class DeleteVisitAsyncTask extends AsyncTask<Visit, Void, Void> {
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

    /**
     * Returns all the visits in the database
     * @return LiveData<List<Visit>> List of all visits
     */
    public LiveData<List<Visit>> getAllVisits() {
        return visitDAO.getAllVisits();
    }

    /**
     * Returns the visit with the given title
     * @return
     */
    public LiveData<Visit> getVisit(String title) {
        return visitDAO.getVisitFromTitle(title);
    }


}