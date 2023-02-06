package com.jerson.hcdc_portal.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.jerson.hcdc_portal.model.AccountLinksModel;
import com.jerson.hcdc_portal.model.AccountModel;
import com.jerson.hcdc_portal.network.Clients;

import java.util.List;

public class AccountViewModel extends ViewModel {
    // for links
    MutableLiveData<List<AccountLinksModel>> dataLinks = new MutableLiveData<>();

    //for data
    MutableLiveData<List<AccountModel>> data = new MutableLiveData<>();

    // response
    MutableLiveData<String> response = new MutableLiveData<>();

    public MutableLiveData<String> getResponse() {
        return response;
    }

    public MutableLiveData<List<AccountLinksModel>> getDataLinks() {
        new Clients().accountLinks(dataLinks,response);
        return dataLinks;
    }

    public MutableLiveData<List<AccountModel>> getData(String link) {
        new Clients().account(data,response,link);
        return data;
    }
}
