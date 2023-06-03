package com.jerson.hcdc_portal.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.jerson.hcdc_portal.database.DatabasePortal;
import com.jerson.hcdc_portal.model.DashboardModel;
import com.jerson.hcdc_portal.repo.DashboardRepo;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;

/* NOTE: THIS A SUBJECTS VIEWMODEL - In the student portal the subject's schedule is in the dashboard so I just copy the name*/
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

    public LiveData<List<DashboardModel>> getData(){
        return repo.getDashData(resCode,response);
    }

    public Completable insertDashboard(List<DashboardModel> dashboardModel){
        return databasePortal.dashboardDao().insertDashboard(dashboardModel);
    }

    public Flowable<List<DashboardModel>> loadDashboard(){
        return databasePortal.dashboardDao().getDashboard();
    }

    public Completable deleteDashboardData(){
        return databasePortal.dashboardDao().deleteDashboardData();
    }

}
