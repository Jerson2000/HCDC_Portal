package com.jerson.hcdc_portal.viewmodel;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.jerson.hcdc_portal.PortalApp;
import com.jerson.hcdc_portal.database.DatabasePortal;
import com.jerson.hcdc_portal.model.EnrollHistModel;
import com.jerson.hcdc_portal.repo.EnrollHistRepo;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;

public class EnrollHistoryViewModel extends ViewModel {
    MutableLiveData<String> response = new MutableLiveData<>();
    MutableLiveData<Integer> resCode = new MutableLiveData<>();
    EnrollHistRepo repo;
    DatabasePortal databasePortal;
    public EnrollHistoryViewModel(){
        databasePortal = DatabasePortal.getDatabase(PortalApp.getAppContext());
        repo = new EnrollHistRepo();
    }


    public MutableLiveData<String> getResponse() {
        return response;
    }

    public LiveData<List<EnrollHistModel>> getData(String link) {
       return repo.getEnrollData(link,response,resCode);
    }

    public LiveData<List<EnrollHistModel.Link>> getLinks( ) {
        return repo.getEnrollLinks(response,resCode);
    }

    public Completable insertEnrollHistory(List<EnrollHistModel> enrollHistory){
        return databasePortal.databaseDao().insertEnrollHistory(enrollHistory);
    }

    public Flowable<List<EnrollHistModel>> loadEnrollHistory(){
        return databasePortal.databaseDao().getEnrollHistory();
    }

    public Flowable<List<EnrollHistModel>> loadEnrollHistory(int link_id){
        return databasePortal.databaseDao().getEnrollHistory(link_id);
    }

    public Completable insertEnrollHistoryLink(List<EnrollHistModel.Link> enrollHistoryLink){
        return databasePortal.databaseDao().insertEnrollHistoryLink(enrollHistoryLink);
    }


    public Flowable<List<EnrollHistModel.Link>> loadEnrollHistoryLink(){
        return databasePortal.databaseDao().getEnrollHistoryLinks();
    }

    public Completable deleteEnrollHistoryData(int link_id){
        return databasePortal.databaseDao().deleteEnrollHistoryData(link_id);
    }

    public Completable deleteEnrollHistoryLinkData(){
        return databasePortal.databaseDao().deleteEnrollHistoryLinkData();
    }



}
