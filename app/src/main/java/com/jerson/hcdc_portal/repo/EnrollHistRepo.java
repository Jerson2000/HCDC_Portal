package com.jerson.hcdc_portal.repo;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.jerson.hcdc_portal.PortalApp;
import com.jerson.hcdc_portal.listener.OnHttpResponseListener;
import com.jerson.hcdc_portal.model.EnrollHistModel;
import com.jerson.hcdc_portal.network.HttpClient;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class EnrollHistRepo {

    public LiveData<List<EnrollHistModel.Link>> getEnrollLinks(MutableLiveData<String> response, MutableLiveData<Integer> resCode){
        MutableLiveData<List<EnrollHistModel.Link>> data = new MutableLiveData<>();
        HttpClient.getInstance().GET(PortalApp.baseUrl + PortalApp.enrollHistory, new OnHttpResponseListener<Document>() {
            @Override
            public void onResponse(Document response) {
                List<EnrollHistModel.Link> links = new ArrayList<>();
                Elements semList = response.select("main.app-content ul li.nav-item:gt(0)");

                for (Element list : semList) {
                    String link = list.select("a.nav-link").attr("href");
                    String text = list.select("a.nav-link").text();
                    EnrollHistModel.Link model = new EnrollHistModel.Link(link, text);
                    links.add(model);

                }

                data.setValue(links);
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


    public LiveData<List<EnrollHistModel>> getEnrollData(String link, MutableLiveData<String> response, MutableLiveData<Integer> resCode){
        MutableLiveData<List<EnrollHistModel>> data = new MutableLiveData<>();

        HttpClient.getInstance().GET(PortalApp.baseUrl + link, new OnHttpResponseListener<Document>() {
            @Override
            public void onResponse(Document response) {
                List<EnrollHistModel> history = new ArrayList<>();
                Elements table = response.select("div.col-md-9 table > tbody");


                for (Element e : table.select("tr")) {

                    EnrollHistModel model = new EnrollHistModel
                            (
                                    e.select("td:eq(0)").text(),
                                    e.select("td:eq(1)").text(),
                                    e.select("td:eq(2)").text(),
                                    e.select("td:eq(3)").text()
                            );

                    history.add(model);
                }

                data.setValue(history);
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
