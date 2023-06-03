package com.jerson.hcdc_portal.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.jerson.hcdc_portal.model.AccountModel;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;

@Dao
public interface AccountDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertAccount(List<AccountModel> list);

    @Query("select * from account")
    Flowable<List<AccountModel>> getAccounts();


    @Query("delete from account")
    Completable deleteAccount();
}
