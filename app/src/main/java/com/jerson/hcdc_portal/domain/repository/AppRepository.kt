package com.jerson.hcdc_portal.domain.repository

import com.jerson.hcdc_portal.util.Resource
import kotlinx.coroutines.flow.Flow

interface AppRepository {
    suspend fun sendCrashReport(report:String): Flow<Resource<Boolean>>
    suspend fun checkForUpdate():Flow<Resource<String>>
}