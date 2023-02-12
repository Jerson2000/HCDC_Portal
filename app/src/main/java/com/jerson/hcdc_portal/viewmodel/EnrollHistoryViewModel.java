package com.jerson.hcdc_portal.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.jerson.hcdc_portal.model.EnrollHistModel;
import com.jerson.hcdc_portal.model.EnrollLinksModel;
import com.jerson.hcdc_portal.network.Clients;

import java.util.List;

public class EnrollHistoryViewModel extends ViewModel {
    MutableLiveData<List<EnrollHistModel>> data = new MutableLiveData<>();
    MutableLiveData<List<EnrollLinksModel>> links = new MutableLiveData<>();
    MutableLiveData<String> response = new MutableLiveData<>();

    public MutableLiveData<String> getResponse() {
        return response;
    }

    public MutableLiveData<List<EnrollHistModel>> getData(String link) {
        new Clients().enrollHistory(data,response,link);
        return data;
    }

    public MutableLiveData<List<EnrollLinksModel>> getLinks() {
        new Clients().enrollLink(links,response);
        return links;
    }
}
