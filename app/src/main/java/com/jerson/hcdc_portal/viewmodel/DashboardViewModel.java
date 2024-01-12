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
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class DashboardViewModel extends AndroidViewModel {
    private MutableLiveData<Integer> resCode = new MutableLiveData<>();
    private MutableLiveData<Throwable> err = new MutableLiveData<>();
    private DatabasePortal databasePortal;
    private DashboardRepo repo;

    public DashboardViewModel(@NonNull Application application) {
        super(application);
        databasePortal =  DatabasePortal.getDatabase(application);
        repo = new DashboardRepo();
    }

    public MutableLiveData<Integer> getResCode() {
        return resCode;
    }

    public LiveData<List<DashboardModel>> getData(){
        return repo.getDashData(resCode,err);
    }

    public MutableLiveData<Throwable> getErr() {
        return err;
    }

    public LiveData<Boolean> insertDashboard(List<DashboardModel> list){
        MutableLiveData<Boolean> callback = new MutableLiveData<>();
        new CompositeDisposable().add(databasePortal.dashboardDao().insertDashboard(list)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> callback.setValue(true), throwable -> callback.setValue(false))

        );
        return callback;
    }

    public LiveData<Boolean> deleteDashboardData(){
        MutableLiveData<Boolean> callback = new MutableLiveData<>();
        new CompositeDisposable().add(databasePortal.dashboardDao().deleteDashboardData()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(()->callback.setValue(true), throwable ->callback.setValue(false)));
        return callback;
    }

    public LiveData<List<DashboardModel>> loadSubjects(){
        MutableLiveData<List<DashboardModel>> list = new MutableLiveData<>();
        new CompositeDisposable().add(databasePortal.dashboardDao().getDashboard()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list::setValue, err::setValue));
        return list;
    }

}
