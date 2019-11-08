package com.example.mapper.viewmodels;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.mapper.services.VisitRepository;
import com.example.mapper.services.models.Visit;

import java.util.List;

public class VisitViewModel extends AndroidViewModel {
    private final VisitRepository visitRepository;
    private LiveData<List<Visit>> visits;

    public VisitViewModel(Application application){
        super(application);
        visitRepository = new VisitRepository(application);
        visits = visitRepository.getAllVisits();
    }

    public LiveData<List<Visit>> getAllVisits() { return visits; }

    public void createVisit(Visit visit) { visitRepository.createVisit(visit);}



}
