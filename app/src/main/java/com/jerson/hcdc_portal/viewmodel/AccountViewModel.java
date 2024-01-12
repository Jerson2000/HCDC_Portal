package com.jerson.hcdc_portal.viewmodel;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.jerson.hcdc_portal.PortalApp;
import com.jerson.hcdc_portal.database.DatabasePortal;
import com.jerson.hcdc_portal.model.AccountLinksModel;
import com.jerson.hcdc_portal.model.AccountModel;
import com.jerson.hcdc_portal.model.DashboardModel;
import com.jerson.hcdc_portal.repo.AccountRepo;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class AccountViewModel extends ViewModel {
    private static final String TAG = "AccountViewModel";
    private final MutableLiveData<String> response = new MutableLiveData<>();
    private final MutableLiveData<Integer> resCode = new MutableLiveData<>();
    private final MutableLiveData<Throwable> err = new MutableLiveData<>();
    private final AccountRepo repo;
    private final DatabasePortal databasePortal;

    public AccountViewModel() {
        repo = new AccountRepo();
        databasePortal = DatabasePortal.getDatabase(PortalApp.getAppContext());
    }

    public LiveData<String> getResponse() {
        return response;
    }

    public LiveData<Integer> getResCode() {
        return resCode;
    }


    public LiveData<List<AccountModel>> getData(String link) {
        return repo.getData(link, response, resCode);
    }

    public LiveData<Throwable> getErr() {
        return err;
    }

    public LiveData<List<AccountModel>> getData() {
        return repo.getData(resCode,err);
    }

    /* Database */
    public LiveData<Boolean> insertAccount(List<AccountModel> list) {
        MutableLiveData<Boolean> callback = new MutableLiveData<>();
        new CompositeDisposable().add(databasePortal.accountDao().insertAccount(list)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> callback.setValue(true), throwable -> {
                    callback.setValue(false);
                    Log.e(TAG, "insertAccount: " + throwable.getMessage() );
                })

        );

        return callback;
    }

    public LiveData<List<AccountModel>> loadAccounts(){
        MutableLiveData<List<AccountModel>> list = new MutableLiveData<>();
        new CompositeDisposable().add(databasePortal.accountDao().getAccounts()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list::setValue, throwable -> {
                  err.setValue(throwable);
                    Log.e(TAG, "loadAccounts: "+throwable.getMessage());
                }));
        return list;
    }


    public LiveData<Boolean> deleteAccount() {
        MutableLiveData<Boolean> callback = new MutableLiveData<>();
        new CompositeDisposable().add(databasePortal.accountDao().deleteAccount()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> callback.setValue(true), throwable -> {
                    callback.setValue(false);
                    Log.e(TAG, "deleteAccount: " + throwable.getMessage() );
                })
        );
        return callback;
    }

}
