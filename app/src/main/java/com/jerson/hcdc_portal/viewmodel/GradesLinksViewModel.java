package com.jerson.hcdc_portal.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.jerson.hcdc_portal.model.GradeLinksModel;
import com.jerson.hcdc_portal.network.Clients;

import java.util.List;

public class GradesLinksViewModel extends ViewModel {
    Clients clients;
    MutableLiveData<List<GradeLinksModel>> gradesLink = new MutableLiveData<>();
    MutableLiveData<String> response = new MutableLiveData<>();

    public MutableLiveData<String> getResponse() {
        return response;
    }

    public MutableLiveData<List<GradeLinksModel>> getGradesLink() {
        clients = new Clients();
        clients.gradesLinkData(gradesLink,response);
        return gradesLink;
    }
}
