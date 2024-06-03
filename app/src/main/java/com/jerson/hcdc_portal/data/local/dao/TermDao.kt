package com.jerson.hcdc_portal.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jerson.hcdc_portal.domain.model.Term
import kotlinx.coroutines.flow.Flow
@Dao
interface TermDao{
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTerm(grades: List<Term>)

    /**
     * @param isGrade is either 1 or 0 - it indicates that true ( 1 ) or false ( 0 )
     */
    @Query("delete from terms where isGrade = :isGrade")
    suspend fun deleteAllTerm(isGrade:Int)

    @Query("select * from terms where isGrade = :isGrade")
    fun getTerms(isGrade:Int): Flow<List<Term>>
}
