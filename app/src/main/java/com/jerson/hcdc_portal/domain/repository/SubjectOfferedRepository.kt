package com.jerson.hcdc_portal.domain.repository

import com.jerson.hcdc_portal.domain.model.SubjectOffered
import com.jerson.hcdc_portal.util.Resource
import kotlinx.coroutines.flow.Flow

interface SubjectOfferedRepository {
    suspend fun fetchSubjectOffered(page:Int): Flow<Resource<List<SubjectOffered>>>
    suspend fun fetchSubjectOffered(query:String?): Flow<Resource<List<SubjectOffered>>>
}