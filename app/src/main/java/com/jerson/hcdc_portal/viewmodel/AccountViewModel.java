package com.jerson.hcdc_portal.viewmodel;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.jerson.hcdc_portal.model.AccountLinksModel;
import com.jerson.hcdc_portal.model.AccountModel;
import com.jerson.hcdc_portal.repo.AccountRepo;

import java.util.List;

public class AccountViewModel extends ViewModel {
    MutableLiveData<String> response = new MutableLiveData<>();
    MutableLiveData<Integer> resCode = new MutableLiveData<>();
    AccountRepo repo;
    public AccountViewModel(){
        repo = new AccountRepo();
    }

    public MutableLiveData<String> getResponse() {
        return response;
    }

    public MutableLiveData<Integer> getResCode() {
        return resCode;
    }

    public LiveData<List<AccountLinksModel>> getLinks() {
        return repo.getLinks(response,resCode);
    }

    public LiveData<List<AccountModel>> getData(String link) {
        return repo.getData(link,response,resCode);
    }
}
