package com.jerson.hcdc_portal.viewmodel;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.jerson.hcdc_portal.listener.OnHttpResponseListener;
import com.jerson.hcdc_portal.model.GradeLinksModel;
import com.jerson.hcdc_portal.network.Clients;
import com.jerson.hcdc_portal.network.HttpClient;
import com.jerson.hcdc_portal.util.AppConstants;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class GradesLinksViewModel extends ViewModel {
    Clients clients;
    MutableLiveData<List<GradeLinksModel>> gradesLink = new MutableLiveData<>();
    MutableLiveData<String> response = new MutableLiveData<>();
    MutableLiveData<Integer> resCode = new MutableLiveData<>();

    public MutableLiveData<String> getResponse() {
        return response;
    }

    public MutableLiveData<List<GradeLinksModel>> getGradesLink() {
        clients = new Clients();
        clients.gradesLinkData(gradesLink,response);
        return gradesLink;
    }

    public LiveData<Integer> getResCode() {
        return resCode;
    }

    public LiveData<List<GradeLinksModel>> getLinks(Context context){
        MutableLiveData<List<GradeLinksModel>> data = new MutableLiveData<>();

        HttpClient.getInstance(context).GET(AppConstants.baseUrl + AppConstants.gradesUrl, new OnHttpResponseListener<Document>() {
            @Override
            public void onResponse(Document response) {
                List<GradeLinksModel> gradesLinks = new ArrayList<>();
                Elements semList = response.select("main.app-content ul li.nav-item");

                for (Element list : semList) {
                    String link = list.select("a.nav-link").attr("href");
                    String text = list.select("a.nav-link").text();
                    GradeLinksModel model = new GradeLinksModel(link, text);
                    gradesLinks.add(model);
                }

                data.setValue(gradesLinks);
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
                response.setValue(e.getMessage());
            }

            @Override
            public void onResponseCode(int code) {
                resCode.setValue(code);
            }
        });

        return data;
    }
}
