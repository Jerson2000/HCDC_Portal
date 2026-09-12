package com.jerson.hcdc_portal.presentation.prospectus.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jerson.hcdc_portal.domain.model.CanonicalProgram
import com.jerson.hcdc_portal.domain.model.Prospectus
import com.jerson.hcdc_portal.domain.repository.ProspectusRepository
import com.jerson.hcdc_portal.domain.resolver.ProgramResolver
import com.jerson.hcdc_portal.util.AppPreference
import com.jerson.hcdc_portal.util.Constants
import com.jerson.hcdc_portal.util.NetworkMonitor
import com.jerson.hcdc_portal.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SyncStatus {
    object Idle : SyncStatus()
    object Syncing : SyncStatus()
    data class Synced(val timestamp: Long) : SyncStatus()
    data class Offline(val message: String) : SyncStatus()
}

@HiltViewModel
class ProspectusViewModel @Inject constructor(
    private val repository: ProspectusRepository,
    private val programResolver: ProgramResolver,
    private val pref: AppPreference,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _prospectusState = MutableStateFlow<Resource<Prospectus>?>(null)
    val prospectusState: StateFlow<Resource<Prospectus>?> = _prospectusState.asStateFlow()

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val _resolvedProgram = MutableStateFlow<CanonicalProgram?>(null)
    val resolvedProgram: StateFlow<CanonicalProgram?> = _resolvedProgram.asStateFlow()

    init {
        resolveAndLoadProspectus(forceRefresh = false)
    }

    fun resolveAndLoadProspectus(forceRefresh: Boolean = false) {
        val studentCourse = pref.getStringPreference(Constants.KEY_STUDENT_COURSE)
        val resolved = programResolver.resolve(studentCourse)
        _resolvedProgram.value = resolved

        viewModelScope.launch {
            _syncStatus.value = SyncStatus.Syncing

            val isConnected = networkMonitor.isConnected.first()
            if (!isConnected && forceRefresh) {
                _syncStatus.value = SyncStatus.Offline("No internet connection. Displaying cached curriculum.")
            }

            repository.getProspectus(studentCourse, forceRefresh).collect { resource ->
                _prospectusState.value = resource
                when (resource) {
                    is Resource.Success -> {
                        val isCached = resource.data?.metadata?.isOfflineCached ?: false
                        if (isCached && !isConnected) {
                            _syncStatus.value = SyncStatus.Offline("Offline / Cached Curriculum")
                        } else {
                            _syncStatus.value = SyncStatus.Synced(
                                resource.data?.metadata?.lastSyncedTimestamp ?: System.currentTimeMillis()
                            )
                        }
                    }
                    is Resource.Error -> {
                        _syncStatus.value = SyncStatus.Offline(resource.message ?: "Failed to sync")
                    }
                    is Resource.Loading -> {
                        _syncStatus.value = SyncStatus.Syncing
                    }
                    else -> Unit
                }

            }
        }
    }

    fun syncCoursesCatalog() {
        viewModelScope.launch {
            repository.syncCoursesCatalog().collect {
                if (it is Resource.Success) {
                    // Re-resolve in case catalog updated
                    val studentCourse = pref.getStringPreference(Constants.KEY_STUDENT_COURSE)
                    _resolvedProgram.value = programResolver.resolve(studentCourse)
                }
            }
        }
    }
}
