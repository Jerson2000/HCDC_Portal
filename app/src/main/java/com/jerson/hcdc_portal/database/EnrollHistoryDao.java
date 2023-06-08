package com.jerson.hcdc_portal.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.jerson.hcdc_portal.model.EnrollHistModel;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;

@Dao
public interface EnrollHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertEnrollHistory(List<EnrollHistModel> enrollHistory);

    @Query("select * from enrollhist")
    Flowable<List<EnrollHistModel>> getEnrollHistory();

    @Query("select * from enrollhist where link_id=:link_id")
    Flowable<List<EnrollHistModel>> getEnrollHistory(int link_id);

    @Query("delete from enrollhist where link_id=:link_id")
    Completable deleteEnrollHistoryData(int link_id);

    @Query("delete from enrollhist")
    Completable deleteAllEnrollHistoryData();

    // link
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertEnrollHistoryLink(List<EnrollHistModel.Link> enrollHistoryLink);

    @Query("select * from enrollhistlink")
    Flowable<List<EnrollHistModel.Link>> getEnrollHistoryLinks();

    @Query("delete from enrollhistlink")
    Completable deleteEnrollHistoryLinkData();
}
