package com.example.mapper.services;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.mapper.services.database.ApplicationDatabase;
import com.example.mapper.services.models.Visit;
import com.example.mapper.services.models.VisitDAO;

import java.util.List;

public class VisitRepository extends ViewModel {

    private final VisitDAO visitDAO;

    public VisitRepository(Application application) {
        ApplicationDatabase db = ApplicationDatabase.getDatabase(application);
        visitDAO = db.visitDao();
    }

    public void createVisit(Visit visit){
        new InsertVisitAsyncTask(visitDAO).execute(visit);
    }

    static class InsertVisitAsyncTask extends AsyncTask<Visit, Void, Void> {
        private VisitDAO asyncVisitDao;

        private InsertVisitAsyncTask(VisitDAO dao){
            asyncVisitDao = dao;
        }

        @Override
        protected Void doInBackground(final Visit... params){
            asyncVisitDao.insert(params[0]);

            return null;
        }
    }


    public LiveData<List<Visit>> getAllVisits() {
        return visitDAO.getAllVisits();
    }

}