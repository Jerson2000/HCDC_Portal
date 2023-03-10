package com.jerson.hcdc_portal.viewmodel;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.jerson.hcdc_portal.model.EnrollHistModel;
import com.jerson.hcdc_portal.model.EnrollLinksModel;
import com.jerson.hcdc_portal.network.Clients;
import com.jerson.hcdc_portal.repo.EnrollHistRepo;

import java.util.List;

public class EnrollHistoryViewModel extends ViewModel {
    MutableLiveData<String> response = new MutableLiveData<>();
    MutableLiveData<Integer> resCode = new MutableLiveData<>();
    EnrollHistRepo repo;
    public EnrollHistoryViewModel(){
        repo = new EnrollHistRepo();
    }


    public MutableLiveData<String> getResponse() {
        return response;
    }

    public LiveData<List<EnrollHistModel>> getData(String link, Context context) {
       return repo.getEnrollData(link,context,response,resCode);
    }

    public LiveData<List<EnrollLinksModel>> getLinks(Context context) {
        return repo.getEnrollLinks(context,response,resCode);
    }
}
