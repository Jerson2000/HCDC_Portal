package com.jerson.hcdc_portal.domain.repository

import com.jerson.hcdc_portal.domain.model.Room
import com.jerson.hcdc_portal.util.Resource
import kotlinx.coroutines.flow.Flow

interface RoomRepository {
    suspend fun getBuilding(): Flow<Resource<Room>>
}