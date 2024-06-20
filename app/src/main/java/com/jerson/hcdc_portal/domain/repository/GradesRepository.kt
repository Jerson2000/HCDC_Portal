package com.jerson.hcdc_portal.domain.repository

import com.jerson.hcdc_portal.domain.model.Grade
import com.jerson.hcdc_portal.domain.model.Term
import com.jerson.hcdc_portal.util.Resource
import kotlinx.coroutines.flow.Flow

interface GradesRepository {
    suspend fun fetchGrades(): Flow<Resource<List<Grade>>>
    suspend fun fetchGrades(term: Term): Flow<Resource<List<Grade>>>
    suspend fun fetchGradesTerm():Flow<Resource<List<Term>>>

    // Database
    suspend fun getGrades(termId:Int): Flow<Resource<List<Grade>>>
    suspend fun  getGradeTerms():Flow<Resource<List<Term>>>
}