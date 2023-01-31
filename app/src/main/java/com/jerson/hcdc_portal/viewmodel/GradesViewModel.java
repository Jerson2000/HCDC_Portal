package com.jerson.hcdc_portal.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.jerson.hcdc_portal.model.GradeModel;
import com.jerson.hcdc_portal.network.Clients;

import java.util.List;

public class GradesViewModel extends ViewModel {
    MutableLiveData<List<GradeModel>> data = new MutableLiveData<>();
    MutableLiveData<String> response = new MutableLiveData<>();

    public MutableLiveData<String> getResponse() {
        return response;
    }

    public MutableLiveData<List<GradeModel>> getData(String link) {
        new Clients().grades(data,response,link);
        return data;
    }
}
