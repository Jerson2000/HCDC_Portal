package com.jerson.hcdc_portal.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.jerson.hcdc_portal.model.GradeModel;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;

@Dao
public interface GradeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertGrade(List<GradeModel> grade);

    @Query("select * from grade where link_id=:link_id")
    Flowable<List<GradeModel>> getGrade(int link_id);

    @Query("delete from grade where link_id=:link_id")
    Completable deleteGradeData(int link_id);

    // link
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertGradeLink(List<GradeModel.Link> gradeLink);

    @Query("select * from gradelink")
    Flowable<List<GradeModel.Link>> getGradeLink();

    @Query("delete from gradelink")
    Completable deleteGradeLink();
}
