package com.jerson.hcdc_portal.viewmodel;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.jerson.hcdc_portal.database.DatabasePortal;
import com.jerson.hcdc_portal.listener.OnHttpResponseListener;
import com.jerson.hcdc_portal.model.DashboardModel;
import com.jerson.hcdc_portal.network.Clients;
import com.jerson.hcdc_portal.network.HttpClient;
import com.jerson.hcdc_portal.repo.DashboardRepo;
import com.jerson.hcdc_portal.util.AppConstants;


import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import io.reactivex.Completable;
import io.reactivex.Flowable;

public class DashboardViewModel extends AndroidViewModel {
    MutableLiveData<String> response = new MutableLiveData<>();
    MutableLiveData<Integer> resCode = new MutableLiveData<>();

    DatabasePortal databasePortal;

    DashboardRepo repo;

    public DashboardViewModel(@NonNull Application application) {
        super(application);
        databasePortal =  DatabasePortal.getDatabase(application);
        repo = new DashboardRepo();
    }

    public MutableLiveData<Integer> getResCode() {
        return resCode;
    }

    public LiveData<List<DashboardModel>> getData(Context context){
        return repo.getDashData(context,resCode,response);
    }

    public LiveData<String> getDashboardResponse(){
        return response;
    }


    public Completable insertDashboard(List<DashboardModel> dashboardModel){
        return databasePortal.databaseDao().insertDashboard(dashboardModel);
    }

    public Flowable<List<DashboardModel>> loadDashboard(){
        return databasePortal.databaseDao().getDashboard();
    }

    public Completable deleteAll(){
        return databasePortal.databaseDao().deleteAll();
    }

}
