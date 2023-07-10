package com.jerson.hcdc_portal.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.jerson.hcdc_portal.repo.EvaluationRepo;

public class EvaluationViewModel extends ViewModel {
    private MutableLiveData<Throwable> err = new MutableLiveData<>();
    private EvaluationRepo repo;

    public EvaluationViewModel() {
        repo = new EvaluationRepo();
    }

    public MutableLiveData<Throwable> getErr() {
        return err;
    }

    public LiveData<String> getEvaluation(){
        return repo.getEvaluation(err);
    }

}
