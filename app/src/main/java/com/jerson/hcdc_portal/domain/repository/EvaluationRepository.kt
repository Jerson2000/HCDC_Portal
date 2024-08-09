package com.jerson.hcdc_portal.domain.repository

import com.jerson.hcdc_portal.util.Resource
import kotlinx.coroutines.flow.Flow

interface EvaluationRepository {
    suspend fun fetchEvaluation(): Flow<Resource<String>>

    // Lacking Credential -> I just insert this here to minimize file
    suspend fun fetchLacking():Flow<Resource<String>>
}