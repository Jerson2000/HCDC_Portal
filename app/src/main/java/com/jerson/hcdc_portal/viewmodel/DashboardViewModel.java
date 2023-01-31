package com.jerson.hcdc_portal.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.jerson.hcdc_portal.model.DashboardModel;
import com.jerson.hcdc_portal.network.Clients;


import java.util.List;

public class DashboardViewModel extends ViewModel {
    Clients clients;
    MutableLiveData<String> response = new MutableLiveData<>();
    MutableLiveData<List<DashboardModel>> dashboardData = new MutableLiveData<>();

    public MutableLiveData<List<DashboardModel>> getDashboardData(){
        clients = new Clients();
        clients.dashboardData(dashboardData,response);
        return dashboardData;
    }

    public MutableLiveData<String> getDashboardResponse(){
        return response;
    }

}
