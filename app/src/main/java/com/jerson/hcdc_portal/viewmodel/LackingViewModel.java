package com.jerson.hcdc_portal.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.jerson.hcdc_portal.repo.LackingRepo;

public class LackingViewModel extends ViewModel {
    private MutableLiveData<Throwable> err = new MutableLiveData<>();
    private LackingRepo repo;

    public LackingViewModel(){
        repo = new LackingRepo();
    }

    public LiveData<String> getLacking(){
        return repo.getLacking(err);
    }

    public MutableLiveData<Throwable> getErr() {
        return err;
    }
}
