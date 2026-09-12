package com.jerson.hcdc_portal.util

import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class NetworkBoundResourceTest {

    @Test
    fun networkBoundResource_successfulFetch_emitsLoadingThenSuccess() = runBlocking {
        val databaseContent = mutableListOf("initial_cached_item")

        val flow = networkBoundResource(
            query = { flowOf(databaseContent.toList()) },
            fetch = { listOf("fresh_network_item") },
            saveFetchResult = { remoteList ->
                databaseContent.clear()
                databaseContent.addAll(remoteList)
            }
        )

        val emissions = flow.toList()
        assertEquals(2, emissions.size)

        // First emission: Loading with cached data
        assertTrue(emissions[0] is Resource.Loading)
        assertEquals(listOf("initial_cached_item"), emissions[0].data)

        // Second emission: Success with fresh data
        assertTrue(emissions[1] is Resource.Success)
        assertEquals(listOf("fresh_network_item"), emissions[1].data)
    }

    @Test
    fun networkBoundResource_failedFetch_emitsLoadingThenErrorWithCachedData() = runBlocking {
        val databaseContent = mutableListOf("cached_item_available_offline")

        val flow = networkBoundResource(
            query = { flowOf(databaseContent.toList()) },
            fetch = { throw IOException("No network connectivity") },
            saveFetchResult = { /* No-op */ }
        )

        val emissions = flow.toList()
        assertEquals(2, emissions.size)

        // First emission: Loading with cached data
        assertTrue(emissions[0] is Resource.Loading)
        assertEquals(listOf("cached_item_available_offline"), emissions[0].data)

        // Second emission: Error but with cached data preserved
        assertTrue(emissions[1] is Resource.Error)
        assertEquals("No network connectivity", emissions[1].message)
        assertEquals(listOf("cached_item_available_offline"), emissions[1].data)
    }

    @Test
    fun networkBoundResource_shouldFetchFalse_onlyEmitsCache() = runBlocking {
        val databaseContent = listOf("offline_cache_only")

        val flow = networkBoundResource(
            query = { flowOf(databaseContent) },
            fetch = { throw IllegalStateException("Should not be called") },
            saveFetchResult = { },
            shouldFetch = { false }
        )

        val emissions = flow.toList()
        assertEquals(2, emissions.size)
        assertTrue(emissions[0] is Resource.Loading)
        assertTrue(emissions[1] is Resource.Success)
        assertEquals(listOf("offline_cache_only"), emissions[1].data)
    }
}
