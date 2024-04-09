package com.jerson.hcdc_portal.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jerson.hcdc_portal.domain.model.Schedule
import kotlinx.coroutines.flow.Flow
@Dao
interface ScheduleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSchedules(schedules: List<Schedule>)

    @Query("delete from schedules")
    suspend fun deleteAllSchedules()

    @Query("select * from schedules")
    fun getSchedules(): Flow<List<Schedule>>
}