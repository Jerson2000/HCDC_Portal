package com.jerson.hcdc_portal.repo;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.jerson.hcdc_portal.PortalApp;
import com.jerson.hcdc_portal.listener.OnHttpResponseListener;
import com.jerson.hcdc_portal.network.HttpClient;

import org.jsoup.nodes.Document;

public class EvaluationRepo {

    public LiveData<String> getEvaluation(MutableLiveData<Throwable> err){
        MutableLiveData<String> data = new MutableLiveData<>();

        HttpClient.getInstance().GET(PortalApp.baseUrl + PortalApp.evaluationsUrl, new OnHttpResponseListener<Document>() {
            @Override
            public void onResponse(Document response) {
                String dat=response.body().select("div.col-md-12 > div.tile").toString();
                data.setValue(!dat.equals("")?dat:"wala");
            }

            @Override
            public void onFailure(Exception e) {
                err.setValue(e);
            }

            @Override
            public void onResponseCode(int code) {

            }
        });


        return data;
    }
}
