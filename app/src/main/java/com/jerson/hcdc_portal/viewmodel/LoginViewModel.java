package com.jerson.hcdc_portal.viewmodel;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import com.jerson.hcdc_portal.listener.OnHttpResponseListener;
import com.jerson.hcdc_portal.network.Clients;
import com.jerson.hcdc_portal.network.HttpClient;
import com.jerson.hcdc_portal.network.Loaders;
import com.jerson.hcdc_portal.util.AppConstants;

import org.jsoup.nodes.Document;

import java.util.Locale;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.FormBody;

public class LoginViewModel extends ViewModel {
    /*Clients clients;
    MutableLiveData<String> response = new MutableLiveData<>();

    public MutableLiveData<String> login(String email,String pass){
        clients = new Clients();
        clients.login(response,email,pass);
        return response;
    }*/

    private MutableLiveData<String> res = new MutableLiveData<>();
    public MutableLiveData<String> getRes() {
        return res;
    }
    public MutableLiveData<String> Login(String email, String password, Context context){
        getToken(context).observeForever(resp->{
            if(resp!=null){
                FormBody formBody = new FormBody.Builder()
                        .add("_token",resp)
                        .add("email", email)
                        .add("password", password)
                        .build();

                HttpClient.getInstance(context).POST(AppConstants.baseUrl+AppConstants.loginPostUrl, formBody, new OnHttpResponseListener<Document>() {
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
                        res.postValue(e.getStackTrace().toString());
                    }

                });

            }
        });

        return res;
    }

    MutableLiveData<String> getToken(Context context){
        MutableLiveData<String> s = new MutableLiveData<>();
        HttpClient.getInstance(context).GET(AppConstants.baseUrl+AppConstants.loginUrl, new OnHttpResponseListener<Document>() {

            @Override
            public void onResponse(Document response) {
                 s.postValue(response.select("input[name=_token]").first().attr("value"));
            }

            @Override
            public void onFailure(Exception e) {
                s.postValue(e.getMessage());
            }


        });

        return s;
    }
}
