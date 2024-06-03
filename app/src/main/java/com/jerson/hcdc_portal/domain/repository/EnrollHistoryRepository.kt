package com.jerson.hcdc_portal.domain.repository

import com.jerson.hcdc_portal.domain.model.EnrollHistory
import com.jerson.hcdc_portal.domain.model.Term
import com.jerson.hcdc_portal.util.Resource
import kotlinx.coroutines.flow.Flow

interface EnrollHistoryRepository {
    suspend fun fetchEnrollHistory(): Flow<Resource<List<EnrollHistory>>>
    suspend fun fetchEnrollHistory(term: Term): Flow<Resource<List<EnrollHistory>>>
    suspend fun fetchEnrollHistoryTerm(): Flow<Resource<List<Term>>>

    // Database
    suspend fun getEnrollHistory(term: Term): Flow<Resource<List<EnrollHistory>>>
    suspend fun  getEnrollHistoryTerms(): Flow<Resource<List<Term>>>
}