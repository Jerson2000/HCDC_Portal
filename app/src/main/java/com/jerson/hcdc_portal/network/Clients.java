package com.jerson.hcdc_portal.network;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.jerson.hcdc_portal.model.AccountLinksModel;
import com.jerson.hcdc_portal.model.AccountModel;
import com.jerson.hcdc_portal.model.DashboardModel;
import com.jerson.hcdc_portal.model.EnrollHistModel;
import com.jerson.hcdc_portal.model.EnrollLinksModel;
import com.jerson.hcdc_portal.model.GradeLinksModel;
import com.jerson.hcdc_portal.model.GradeModel;

import java.io.IOException;
import java.security.KeyStore;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Clients {
    private static final String TAG = "Clients";
    ExecutorService executor;
    Handler handler = new Handler(Looper.getMainLooper());

    public Clients(){
        executor = Executors.newSingleThreadExecutor();
    }

    public void login(MutableLiveData<String> response, String email, String pass) {

        executor.execute(() -> {
            try {
                Loaders.login(response, email, pass);
            } catch (IOException e) {
                Log.d(TAG, "login: "+e.getLocalizedMessage());
                response.postValue(e.getLocalizedMessage());
            }
        });

        /* new Thread(() -> {
            Loaders.login(response, email, pass);
        }).start();*/
    }

    public  void dashboardData(MutableLiveData<List<DashboardModel>> data, MutableLiveData<String> response) {
        executor.execute(()->{
            try {
                Loaders.dashboard(data,response);
            } catch (IOException e) {
                Log.d(TAG, "dashboardData: "+e.getLocalizedMessage());
                response.postValue(e.getLocalizedMessage());
            }
        });

    }

    public void gradesLinkData(MutableLiveData<List<GradeLinksModel>> data, MutableLiveData<String> response) {

        executor.execute(()->{
            try {
                Loaders.gradesLink(data,response);
            } catch (IOException e) {
                Log.d(TAG, "gradesLinkData: "+ e.getLocalizedMessage());
                response.postValue(e.getLocalizedMessage());
            }
        });


    }

    public void grades(MutableLiveData<List<GradeModel>> data,MutableLiveData<String> response,String link){
        executor.execute(()->{
            try {
                Loaders.grades(data,response,link);
            } catch (IOException e) {
                Log.d(TAG, "grades: "+e.getLocalizedMessage());
                response.postValue(e.getLocalizedMessage());
            }
        });
    }

    public void accountLinks(MutableLiveData<List<AccountLinksModel>> data,MutableLiveData<String> response){
        executor.execute(() -> {
            try {
                Loaders.accountLink(data, response);
            } catch (IOException e) {
                Log.d(TAG, "accountLinks: "+e.getLocalizedMessage());
                response.postValue(e.getLocalizedMessage());
            }
        });
    }

    public void account(MutableLiveData<List<AccountModel>> data, MutableLiveData<String> response,String link){
        executor.execute(() -> {
            try {
                Loaders.account(data, response,link);
            } catch (IOException e) {
                Log.d(TAG, "account: "+e.getMessage());
                response.postValue(e.getLocalizedMessage());
            }
        });
    }

    public void enrollHistory(MutableLiveData<List<EnrollHistModel>> data,MutableLiveData<String> response,String link){
        executor.execute(() -> {
            try {
                Loaders.enrollHistory(data, response,link);
            } catch (IOException e) {
                Log.d(TAG, "enrollHistory: "+e.getMessage());
                response.postValue(e.getLocalizedMessage());
            }
        });
    }

    public void enrollLink(MutableLiveData<List<EnrollLinksModel>> data, MutableLiveData<String> response){
        executor.execute(() -> {
            try {
                Loaders.enrollLink(data, response);
            } catch (IOException e) {
                Log.d(TAG, "enrollLink: "+e.getMessage());
                response.postValue(e.getLocalizedMessage());
            }
        });
    }


}
