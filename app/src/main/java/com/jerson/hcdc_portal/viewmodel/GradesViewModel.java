package com.jerson.hcdc_portal.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.jerson.hcdc_portal.PortalApp;
import com.jerson.hcdc_portal.database.DatabasePortal;
import com.jerson.hcdc_portal.model.EnrollHistModel;
import com.jerson.hcdc_portal.model.GradeLinksModel;
import com.jerson.hcdc_portal.model.GradeModel;
import com.jerson.hcdc_portal.repo.GradeRepo;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;

public class GradesViewModel extends ViewModel {

    MutableLiveData<String> response = new MutableLiveData<>();
    MutableLiveData<Integer> resCode = new MutableLiveData<>();
    GradeRepo repo;
    DatabasePortal databasePortal;

    public GradesViewModel() {
        databasePortal = DatabasePortal.getDatabase(PortalApp.getAppContext());
        repo = new GradeRepo();
    }

    public MutableLiveData<String> getResponse() {
        return response;
    }


    public LiveData<List<GradeModel.Link>> getLinks() {
        return repo.getLinks(response, resCode);
    }

    public LiveData<List<GradeModel>> gradeData(String link) {
        return repo.gradeData(link, response, resCode);
    }

    public MutableLiveData<Integer> getResCode() {
        return resCode;
    }

    /* database */

    public Completable insertGrade(List<GradeModel> grade) {
        return databasePortal.databaseDao().insertGrade(grade);
    }

    public Flowable<List<GradeModel>> loadGrade(int link_id) {
        return databasePortal.databaseDao().getGrade(link_id);
    }
    public Completable deleteGrade(int link_id){
        return databasePortal.databaseDao().deleteGradeData(link_id);
    }

    public Completable insertGradeLink(List<GradeModel.Link> grade) {
        return databasePortal.databaseDao().insertGradeLink(grade);
    }

    public Flowable<List<GradeModel.Link>> loadGradeLink() {
        return databasePortal.databaseDao().getGradeLink();
    }

    public Completable deleteGradeLink(){
        return databasePortal.databaseDao().deleteGradeLink();
    }

}
