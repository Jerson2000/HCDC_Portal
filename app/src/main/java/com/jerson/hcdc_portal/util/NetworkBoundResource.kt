package com.jerson.hcdc_portal.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

/**
 * Executes an offline-first data synchronization strategy:
 * 1. Immediately emits local cached data as Resource.Loading(cachedData).
 * 2. If shouldFetch(cachedData) is true, attempts remote fetch.
 * 3. On success, persists fetched data to local database via saveFetchResult.
 * 4. Re-emits from local database as Resource.Success(freshData).
 * 5. On failure, emits Resource.Error(errorMessage, cachedData), ensuring the user still sees cached data.
 */
inline fun <ResultType, RequestType> networkBoundResource(
    crossinline query: () -> Flow<ResultType>,
    crossinline fetch: suspend () -> RequestType,
    crossinline saveFetchResult: suspend (RequestType) -> Unit,
    crossinline shouldFetch: (ResultType) -> Boolean = { true },
    crossinline onFetchFailed: (Throwable) -> Unit = {}
): Flow<Resource<ResultType>> = flow {
    val localData = query().first()
    emit(Resource.Loading(localData))

    if (shouldFetch(localData)) {
        try {
            val remoteData = fetch()
            saveFetchResult(remoteData)
            emitAll(query().map { Resource.Success(it) })
        } catch (throwable: Throwable) {
            onFetchFailed(throwable)
            val currentCachedData = query().first()
            val rawMessage = throwable.localizedMessage ?: "An unexpected error occurred"
            val formattedMessage = ErrorMessageHelper.format(rawMessage)
            emit(Resource.Error(formattedMessage, currentCachedData))
        }
    } else {
        emitAll(query().map { Resource.Success(it) })
    }
}
