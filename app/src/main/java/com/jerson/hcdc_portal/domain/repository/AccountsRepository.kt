package com.jerson.hcdc_portal.domain.repository

import com.jerson.hcdc_portal.domain.model.Account
import com.jerson.hcdc_portal.util.Resource
import kotlinx.coroutines.flow.Flow

interface AccountsRepository {
    suspend fun fetchAccounts(): Flow<Resource<List<Account>>>
    // Database
    suspend fun getAccounts(): Flow<Resource<List<Account>>>
}