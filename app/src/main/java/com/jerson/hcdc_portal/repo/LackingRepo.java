package com.jerson.hcdc_portal.repo;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.jerson.hcdc_portal.PortalApp;
import com.jerson.hcdc_portal.listener.OnHttpResponseListener;
import com.jerson.hcdc_portal.network.HttpClient;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class LackingRepo {

    public LiveData<String> getLacking(MutableLiveData<Throwable> err) {
        MutableLiveData<String> data = new MutableLiveData<>();

        HttpClient.getInstance().GET(PortalApp.baseUrl + PortalApp.lackingCredential, new OnHttpResponseListener<Document>() {
            @Override
            public void onResponse(Document response) {
                data.setValue(parseLacking(response));
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

    public String parseLacking(Document response) {
        StringBuilder res = new StringBuilder();
        Elements lacking = response.select("main.app-content > div.row");

        boolean noLack = response.body().select("main.app-content div.row center h2").hasText();

        if (noLack) {
            res.append(response.body().select("main.app-content div.row center h2").text());
        } else {
            for (Element s : lacking.select("div.info")) {
                res.append(s.select("h4").text()).append(":");
            }
        }
        return res.toString();
    }
}
