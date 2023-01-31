package com.jerson.hcdc_portal.network;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.jerson.hcdc_portal.model.DashboardModel;
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
       /* new Thread() {
            @Override
            public void run() {

                Loaders.login(response, email, pass);
            }
        }.start();*/
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

       /* new Thread(() -> {
            Loaders.dashboard(data, response);
        }).start();*/
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

       /* new Thread(() -> {
            Loaders.gradesLink(data, response);
        }).start();*/
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


}
