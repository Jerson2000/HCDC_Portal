package com.jerson.hcdc_portal.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jerson.hcdc_portal.domain.model.Grade
import kotlinx.coroutines.flow.Flow
@Dao
interface GradeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertGrade(grades: List<Grade>)

    @Query("delete from grades where termId = :termId")
    suspend fun deleteAllGrades(termId:Int)
    @Query("delete from grades")
    suspend fun deleteAllGrades()

    @Query("select * from grades where termId = :termId")
    fun getGrades(termId:Int): Flow<List<Grade>>
}