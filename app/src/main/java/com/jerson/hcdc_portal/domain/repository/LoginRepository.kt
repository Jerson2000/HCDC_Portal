package com.jerson.hcdc_portal.domain.repository

import com.jerson.hcdc_portal.util.Resource
import kotlinx.coroutines.flow.Flow

interface LoginRepository {
    suspend fun login(email:String,pass:String): Flow<Resource<String>>
    suspend fun checkSession():Flow<Resource<Boolean>>
}