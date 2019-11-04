package com.example.mapper.services;


import android.app.Application;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.example.mapper.services.database.ApplicationDatabase;
import com.example.mapper.services.models.PointDAO;

public class PointRepository extends ViewModel {
    private final PointDAO pointDao;

    PointRepository(Application application){
        ApplicationDatabase db = ApplicationDatabase.getDatabase(application);
        pointDao = db.pointDAO();
    }
}
