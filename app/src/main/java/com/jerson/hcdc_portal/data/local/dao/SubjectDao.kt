package com.jerson.hcdc_portal.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.jerson.hcdc_portal.domain.model.Subject
import com.jerson.hcdc_portal.util.Resource
import kotlinx.coroutines.flow.Flow

@Dao
interface SubjectDao {
    @Upsert
    suspend fun upsertSubject(subject: Subject)

    @Query("delete from subjects")
    suspend fun deleteAllSubject()

    @Query("select * from subjects")
    fun getSubjects(): Flow<List<Subject>>

}