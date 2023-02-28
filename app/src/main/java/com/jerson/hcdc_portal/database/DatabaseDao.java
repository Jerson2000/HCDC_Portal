package com.jerson.hcdc_portal.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.jerson.hcdc_portal.model.DashboardModel;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
@Dao
public interface DatabaseDao {
    @Query("select * from dashboard")
    Flowable<List<DashboardModel>> getDashboard();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertDashboard(List<DashboardModel> dashboard);
}
