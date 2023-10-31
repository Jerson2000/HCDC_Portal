package com.jerson.hcdc_portal.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.jerson.hcdc_portal.model.SubjectOfferedModel;
import com.jerson.hcdc_portal.repo.SubjectOfferedRepo;

public class SubjectOfferedViewModel extends ViewModel {
    private SubjectOfferedRepo repo;
    MutableLiveData<Throwable> err = new MutableLiveData<>();

    public SubjectOfferedViewModel(){
        repo = new SubjectOfferedRepo();
    }

    public LiveData<SubjectOfferedModel> getSubjectOffered(int page){
        return repo.getSubjectOffered(page,err);
    }

    public LiveData<SubjectOfferedModel> searchSubjectOffered(String query){
        return repo.searchSubjectOffered(query,err);
    }

    public MutableLiveData<Throwable> getErr() {
        return err;
    }
}
