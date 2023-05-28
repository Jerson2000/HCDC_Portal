package com.jerson.hcdc_portal.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.jerson.hcdc_portal.model.GradeLinksModel;
import com.jerson.hcdc_portal.model.GradeModel;
import com.jerson.hcdc_portal.repo.GradeRepo;

import java.util.List;

public class GradesViewModel extends ViewModel {

    MutableLiveData<String> response = new MutableLiveData<>();
    MutableLiveData<Integer> resCode = new MutableLiveData<>();
    GradeRepo repo;

    public GradesViewModel(){
        repo  = new GradeRepo();
    }

    public MutableLiveData<String> getResponse() {
        return response;
    }



    public LiveData<List<GradeLinksModel>> getLinks(){
        return repo.getLinks(response,resCode);
    }

    public LiveData<List<GradeModel>> gradeData (String link){
        return repo.gradeData(link,response,resCode);
    }

    public MutableLiveData<Integer> getResCode() {
        return resCode;
    }
}
