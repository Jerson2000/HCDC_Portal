package com.jerson.hcdc_portal.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.jerson.hcdc_portal.PortalApp;
import com.jerson.hcdc_portal.listener.OnHttpResponseListener;
import com.jerson.hcdc_portal.model.DashboardModel;
import com.jerson.hcdc_portal.network.HttpClient;
import com.jerson.hcdc_portal.repo.DashboardRepo;

import org.jsoup.nodes.Document;

import java.util.List;

import okhttp3.FormBody;

public class LoginViewModel extends ViewModel {
    MutableLiveData<Integer> resCode = new MutableLiveData<>();
    MutableLiveData<List<DashboardModel>> dashboardData = new MutableLiveData<>();

    public LiveData<String> Login(String email, String password) {
        MutableLiveData<String> res = new MutableLiveData<>();
        getToken().observeForever(resp -> {
            if (resp != null) {
                FormBody formBody = new FormBody.Builder()
                        .add("_token", resp)
                        .add("email", email)
                        .add("password", password)
                        .build();

                HttpClient.getInstance().POST(PortalApp.baseUrl + PortalApp.loginPostUrl, formBody, new OnHttpResponseListener<Document>() {
                    @Override
                    public void onResponse(Document response) {
                        boolean wrongPass = response.body().text().contains("CROSSIAN LOG-IN");

                        dashboardData.postValue(DashboardRepo.parseDashboard(response));

                        if (wrongPass) {
                            res.postValue("Incorrect Credentials!");
                        }
                        if (!wrongPass) {
                            res.postValue("Logged In!");
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        res.postValue(e.getMessage());
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponseCode(int code) {
                        resCode.setValue(code);
                    }

                });

            }
        });

        return res;
    }

    public MutableLiveData<Integer> getResCode() {
        return resCode;
    }

    public LiveData<List<DashboardModel>> getDashboard(){
        return dashboardData;
    }

    MutableLiveData<String> getToken() {
        MutableLiveData<String> s = new MutableLiveData<>();
        HttpClient.getInstance().GET(PortalApp.baseUrl + PortalApp.loginUrl, new OnHttpResponseListener<Document>() {

            @Override
            public void onResponse(Document response) {
                s.postValue(response.select("input[name=_token]").first().attr("value"));
            }

            @Override
            public void onFailure(Exception e) {
                s.postValue(e.getMessage());
                e.printStackTrace();
            }

            @Override
            public void onResponseCode(int code) {
                resCode.setValue(code);
            }


        });
        return s;
    }

}
