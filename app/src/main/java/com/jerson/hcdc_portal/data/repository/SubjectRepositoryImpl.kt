package com.jerson.hcdc_portal.data.repository

import com.jerson.hcdc_portal.data.local.PortalDB
import com.jerson.hcdc_portal.domain.model.Subject
import com.jerson.hcdc_portal.domain.repository.SubjectRepository
import com.jerson.hcdc_portal.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class SubjectRepositoryImpl @Inject constructor(
    private val db:PortalDB
): SubjectRepository {
    override suspend fun fetchSubjects(): Flow<Resource<List<Subject>>> {
        TODO("Not yet implemented")
    }

    override suspend fun getSubjects(): Flow<Resource<List<Subject>>> = flow {
        emit(Resource.Loading())
        db.subjectDao().getSubjects()
            .catch {
                emit(Resource.Error(it.message))
            }
            .collect{
                emit(Resource.Success(it))
            }
    }
}