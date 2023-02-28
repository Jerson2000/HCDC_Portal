package com.jerson.hcdc_portal.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.jerson.hcdc_portal.database.DatabasePortal;
import com.jerson.hcdc_portal.model.DashboardModel;
import com.jerson.hcdc_portal.network.Clients;


import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import io.reactivex.Completable;
import io.reactivex.Flowable;

public class DashboardViewModel extends AndroidViewModel {
    Clients clients;
    MutableLiveData<String> response = new MutableLiveData<>();
    MutableLiveData<List<DashboardModel>> dashboardData = new MutableLiveData<>();
    DatabasePortal databasePortal;

    public DashboardViewModel(@NonNull Application application) {
        super(application);
        databasePortal =  DatabasePortal.getDatabase(application);

    }

    public MutableLiveData<List<DashboardModel>> getDashboardData(){
        clients = new Clients();
        clients.dashboardData(dashboardData,response);
        return dashboardData;
    }

    public MutableLiveData<String> getDashboardResponse(){
        return response;
    }

    public Completable insertDashboard(List<DashboardModel> dashboardModel){
        return databasePortal.databaseDao().insertDashboard(dashboardModel);
    }

    public Flowable<List<DashboardModel>> loadDashboard(){
        return databasePortal.databaseDao().getDashboard();
    }

}
