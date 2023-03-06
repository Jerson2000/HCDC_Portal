package com.jerson.hcdc_portal.viewmodel;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.jerson.hcdc_portal.listener.OnHttpResponseListener;
import com.jerson.hcdc_portal.network.HttpClient;
import com.jerson.hcdc_portal.util.AppConstants;

import org.jsoup.nodes.Document;

import okhttp3.FormBody;

public class LoginViewModel extends ViewModel {
    MutableLiveData<Integer> resCode = new MutableLiveData<>();

    public LiveData<String> Login(String email, String password, Context context) {
        MutableLiveData<String> res = new MutableLiveData<>();
        getToken(context).observeForever(resp -> {
            if (resp != null) {
                FormBody formBody = new FormBody.Builder()
                        .add("_token", resp)
                        .add("email", email)
                        .add("password", password)
                        .build();

                HttpClient.getInstance(context).POST(AppConstants.baseUrl + AppConstants.loginPostUrl, formBody, new OnHttpResponseListener<Document>() {
                    @Override
                    public void onResponse(Document response) {
                        boolean wrongPass = response.body().text().contains("CROSSIAN LOG-IN");

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

    MutableLiveData<String> getToken(Context context) {
        MutableLiveData<String> s = new MutableLiveData<>();
        HttpClient.getInstance(context).GET(AppConstants.baseUrl + AppConstants.loginUrl, new OnHttpResponseListener<Document>() {

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
