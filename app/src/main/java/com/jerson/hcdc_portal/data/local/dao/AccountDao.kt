package com.jerson.hcdc_portal.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jerson.hcdc_portal.domain.model.Account
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAccount(accounts: List<Account>)

    @Query("delete from accounts")
    suspend fun deleteAllAccounts()

    @Query("select * from accounts")
    fun getAccounts(): Flow<List<Account>>
}