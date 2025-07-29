package com.jerson.hcdc_portal.presentation.main.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jerson.hcdc_portal.domain.repository.AppRepository
import com.jerson.hcdc_portal.util.NetworkMonitor
import com.jerson.hcdc_portal.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    private val repo: AppRepository,
    networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _postReport = MutableStateFlow<Resource<Boolean>?>(null)
    val postReport: StateFlow<Resource<Boolean>?> = _postReport

    private val _appUpdate = MutableStateFlow<Resource<String>?>(null)
    val appUpdate: StateFlow<Resource<String>?> = _appUpdate

    private val isConnected = networkMonitor.isConnected
    fun postReport(err: String) {
        viewModelScope.launch {
            _postReport.value = Resource.Loading()
            if (!isConnected.first()){
                _postReport.value = Resource.Error("No internet connection.")
                return@launch
            }
            repo.sendCrashReport(err).collect {
                when (it) {
                    is Resource.Success -> _postReport.value = it
                    is Resource.Error -> _postReport.value = Resource.Error(it.message)
                    else -> Unit
                }
            }
        }
    }

    fun checkForUpdate() {
        viewModelScope.launch {
            _appUpdate.value = Resource.Loading()
            if (!isConnected.first()){
                _appUpdate.value = Resource.Error("No internet connection.")
                return@launch
            }
            repo.checkForUpdate().collect {
                when (it) {
                    is Resource.Success -> _appUpdate.value = it
                    is Resource.Error -> _appUpdate.value = Resource.Error(it.message)
                    else -> Unit
                }
            }
        }
    }

}