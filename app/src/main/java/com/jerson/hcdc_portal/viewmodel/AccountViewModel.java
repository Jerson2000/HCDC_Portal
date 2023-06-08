package com.jerson.hcdc_portal.viewmodel;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.jerson.hcdc_portal.PortalApp;
import com.jerson.hcdc_portal.database.DatabasePortal;
import com.jerson.hcdc_portal.model.AccountLinksModel;
import com.jerson.hcdc_portal.model.AccountModel;
import com.jerson.hcdc_portal.repo.AccountRepo;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;

public class AccountViewModel extends ViewModel {
    MutableLiveData<String> response = new MutableLiveData<>();
    MutableLiveData<Integer> resCode = new MutableLiveData<>();
    MutableLiveData<Throwable> err = new MutableLiveData<>();
    AccountRepo repo;
    DatabasePortal databasePortal;

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
    public Completable insertAccount(List<AccountModel> list) {
        return databasePortal.accountDao().insertAccount(list);
    }

    public Flowable<List<AccountModel>> getAccounts() {
        return databasePortal.accountDao().getAccounts();
    }


    public Completable deleteAccount() {
        return databasePortal.accountDao().deleteAccount();
    }

}
