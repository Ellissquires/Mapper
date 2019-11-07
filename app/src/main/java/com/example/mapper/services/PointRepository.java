package com.example.mapper.services;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.mapper.services.database.ApplicationDatabase;
import com.example.mapper.services.models.Point;
import com.example.mapper.services.models.PointDAO;

import java.util.List;

public class PointRepository extends ViewModel {
    private final PointDAO pointDAO;

    public PointRepository(Application application) {
        ApplicationDatabase db = ApplicationDatabase.getDatabase(application);
        pointDAO = db.pointDao();
    }


    /**
     * it returns the value of the live data
     * @return
     */

    public LiveData<List<Point>> getAllPoints() {
        LiveData<List<Point>> points = pointDAO.getAllPoints();
        return points;
    }

}

