package com.jerson.hcdc_portal.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.jerson.hcdc_portal.network.Clients;
import com.jerson.hcdc_portal.network.Loaders;

import java.util.Locale;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class LoginViewModel extends ViewModel {
    Clients clients;
    MutableLiveData<String> response = new MutableLiveData<>();

    public MutableLiveData<String> login(String email,String pass){
        clients = new Clients();
        clients.login(response,email,pass);
        return response;
    }
}
