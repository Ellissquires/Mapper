package com.example.mapper.viewmodels;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;

import com.example.mapper.services.PathRepository;
import com.example.mapper.services.models.Point;

import java.util.List;

public class MapViewModel extends AndroidViewModel {
    private final PathRepository pathRepository;
    private List<Point> points;

    public MapViewModel(Application application){
        super(application);
        pathRepository = new PathRepository(application);
    }

    public List<Point> getPoints(int pathId) { return pathRepository.getPointsOnPath(pathId); }


}