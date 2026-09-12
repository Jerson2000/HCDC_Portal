package com.jerson.hcdc_portal.domain.repository

import com.jerson.hcdc_portal.domain.model.CanonicalProgram
import com.jerson.hcdc_portal.domain.model.Prospectus
import com.jerson.hcdc_portal.util.Resource
import kotlinx.coroutines.flow.Flow

interface ProspectusRepository {
    fun getProspectus(rawStudentProgram: String?, forceRefresh: Boolean = false): Flow<Resource<Prospectus>>
    fun syncCoursesCatalog(): Flow<Resource<List<CanonicalProgram>>>
    fun getCanonicalPrograms(): List<CanonicalProgram>
}
