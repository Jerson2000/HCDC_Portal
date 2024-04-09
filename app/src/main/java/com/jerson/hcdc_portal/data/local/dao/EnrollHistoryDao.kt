package com.jerson.hcdc_portal.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jerson.hcdc_portal.domain.model.EnrollHistory
import kotlinx.coroutines.flow.Flow
@Dao
interface EnrollHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertHistory(grades: List<EnrollHistory>)

    @Query("delete from enroll_history where termId = :termId")
    suspend fun deleteAllHistory(termId:Int)

    @Query("select * from enroll_history where termId = :termId")
    fun getHistory(termId:Int): Flow<List<EnrollHistory>>
}