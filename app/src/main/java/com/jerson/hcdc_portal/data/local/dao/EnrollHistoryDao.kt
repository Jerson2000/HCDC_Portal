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

    @androidx.room.Transaction
    suspend fun replaceHistory(termId: Int, history: List<EnrollHistory>) {
        deleteAllHistory(termId)
        upsertHistory(history)
    }

    @Query("delete from enroll_history")
    suspend fun deleteAllHistory()

    @Query("select * from enroll_history where termId = :termId")
    fun getHistory(termId:Int): Flow<List<EnrollHistory>>

    @Query("select * from enroll_history where term = :term")
    fun getHistoryByTermName(term: String): Flow<List<EnrollHistory>>

    @Query("update enroll_history set termId = :termId where term = :term and (termId = 0 or termId is null)")
    suspend fun updateTermId(termId: Int, term: String)
}