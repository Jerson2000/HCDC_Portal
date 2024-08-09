package com.jerson.hcdc_portal.domain.repository

import com.jerson.hcdc_portal.domain.model.Account
import com.jerson.hcdc_portal.domain.model.Term
import com.jerson.hcdc_portal.util.Resource
import kotlinx.coroutines.flow.Flow

interface AccountsRepository {
    suspend fun fetchAccounts(): Flow<Resource<List<Account>>>
    suspend fun fetchAccounts(term: Term): Flow<Resource<List<Account>>>
    // Database
    suspend fun getAccounts(): Flow<Resource<List<Account>>>
    suspend fun getAccounts(termId: Int): Flow<Resource<List<Account>>>
    suspend fun  getAccountTerm(): Flow<Resource<List<Term>>>
    suspend fun hasData(term: Term,hasData:(Boolean)-> Unit)
}