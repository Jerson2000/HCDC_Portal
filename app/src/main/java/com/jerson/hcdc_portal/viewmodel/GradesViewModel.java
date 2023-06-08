package com.jerson.hcdc_portal.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.jerson.hcdc_portal.PortalApp;
import com.jerson.hcdc_portal.database.DatabasePortal;
import com.jerson.hcdc_portal.model.GradeModel;
import com.jerson.hcdc_portal.repo.GradeRepo;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;

public class GradesViewModel extends ViewModel {

    MutableLiveData<String> response = new MutableLiveData<>();
    MutableLiveData<Integer> resCode = new MutableLiveData<>();
    MutableLiveData<Throwable> err = new MutableLiveData<>();
    GradeRepo repo;
    DatabasePortal databasePortal;

    public GradesViewModel() {
        databasePortal = DatabasePortal.getDatabase(PortalApp.getAppContext());
        repo = new GradeRepo();
    }

    public LiveData<String> getResponse() {
        return response;
    }

    public LiveData<Integer> getResCode() {
        return resCode;
    }

    public LiveData<Throwable> getErr() {
        return err;
    }

    public LiveData<List<GradeModel.Link>> getLinks() {
        return repo.getLinks(err, resCode);
    }

    public LiveData<List<GradeModel>> gradeData(String link) {
        return repo.gradeData(link, err, resCode);
    }


    /* database */

    public Completable insertGrade(List<GradeModel> grade) {
        return databasePortal.gradeDao().insertGrade(grade);
    }

    public Flowable<List<GradeModel>> loadGrade(int link_id) {
        return databasePortal.gradeDao().getGrade(link_id);
    }
    public Completable deleteGrade(int link_id){
        return databasePortal.gradeDao().deleteGradeData(link_id);
    }

    public Completable insertGradeLink(List<GradeModel.Link> grade) {
        return databasePortal.gradeDao().insertGradeLink(grade);
    }

    public Flowable<List<GradeModel.Link>> loadGradeLink() {
        return databasePortal.gradeDao().getGradeLink();
    }

    public Completable deleteGradeLink(){
        return databasePortal.gradeDao().deleteGradeLink();
    }

    public Completable deleteAllGradeData(){
        return databasePortal.gradeDao().deleteAllGradeData();
    }

}
