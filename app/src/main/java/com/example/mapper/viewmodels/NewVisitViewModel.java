package com.example.mapper.viewmodels;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;

import com.example.mapper.services.PathRepository;
import com.example.mapper.services.VisitRepository;
import com.example.mapper.services.models.Path;
import com.example.mapper.services.models.Visit;

public class NewVisitViewModel extends AndroidViewModel {
    private final VisitRepository visitRepository;

    public NewVisitViewModel(Application application){
        super(application);
        visitRepository = new VisitRepository(application);
    }


    public void createVisit(Visit visit) { visitRepository.createVisit(visit);}
}
