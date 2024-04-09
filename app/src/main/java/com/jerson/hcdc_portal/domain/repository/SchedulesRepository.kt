package com.jerson.hcdc_portal.domain.repository

import com.jerson.hcdc_portal.domain.model.Schedule
import com.jerson.hcdc_portal.domain.model.Subject
import com.jerson.hcdc_portal.util.Resource
import kotlinx.coroutines.flow.Flow

interface SchedulesRepository {
    suspend fun fetchSchedules(): Flow<Resource<List<Schedule>>>
    // Database
    suspend fun getSchedules():Flow<Resource<List<Schedule>>>
}