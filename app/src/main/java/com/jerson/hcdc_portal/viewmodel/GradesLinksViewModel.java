package com.jerson.hcdc_portal.viewmodel;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.jerson.hcdc_portal.listener.OnHttpResponseListener;
import com.jerson.hcdc_portal.model.GradeLinksModel;
import com.jerson.hcdc_portal.network.Clients;
import com.jerson.hcdc_portal.network.HttpClient;
import com.jerson.hcdc_portal.repo.GradeRepo;
import com.jerson.hcdc_portal.util.AppConstants;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class GradesLinksViewModel extends ViewModel {
    MutableLiveData<String> response = new MutableLiveData<>();
    MutableLiveData<Integer> resCode = new MutableLiveData<>();
    GradeRepo repo;
    public GradesLinksViewModel(){
        repo = new GradeRepo();
    }

    public MutableLiveData<String> getResponse() {
        return response;
    }

    public LiveData<Integer> getResCode() {
        return resCode;
    }

    public LiveData<List<GradeLinksModel>> getLinks(Context context){
        return repo.getLinks(context,response,resCode);
    }

}
