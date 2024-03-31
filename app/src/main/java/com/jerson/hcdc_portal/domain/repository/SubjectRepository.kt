package com.jerson.hcdc_portal.domain.repository

import com.jerson.hcdc_portal.domain.model.Subject
import com.jerson.hcdc_portal.util.Resource
import kotlinx.coroutines.flow.Flow

interface SubjectRepository {
    suspend fun fetchSubjects(): Flow<Resource<List<Subject>>>
    // Database
    suspend fun getSubjects():Flow<Resource<List<Subject>>>
}