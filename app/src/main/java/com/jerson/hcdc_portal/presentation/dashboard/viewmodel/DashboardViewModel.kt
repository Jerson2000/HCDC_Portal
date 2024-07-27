package com.jerson.hcdc_portal.presentation.dashboard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jerson.hcdc_portal.domain.model.Schedule
import com.jerson.hcdc_portal.domain.repository.SchedulesRepository
import com.jerson.hcdc_portal.util.AppPreference
import com.jerson.hcdc_portal.util.Constants
import com.jerson.hcdc_portal.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val schedulesRepository: SchedulesRepository,
    private val pref: AppPreference
) : ViewModel() {

    private val _fetchSchedules = MutableStateFlow<Resource<List<Schedule>>?>(null)
    val fetchSchedules: StateFlow<Resource<List<Schedule>>?> = _fetchSchedules

    init {
        val isLoaded = pref.getBooleanPreference(Constants.KEY_IS_SCHEDULE_LOADED)
        if (!isLoaded) {
            fetchSchedules()
            pref.setBooleanPreference(Constants.KEY_IS_SCHEDULE_LOADED, true)
        }
    }

    fun fetchSchedules() {
        viewModelScope.launch {
            schedulesRepository.fetchSchedules().collect {
                when (it) {
                    is Resource.Loading -> {
                        _fetchSchedules.value = Resource.Loading()
                    }

                    is Resource.Success -> {
                        _fetchSchedules.value = it
                    }

                    is Resource.Error -> {
                        _fetchSchedules.value = Resource.Error(it.message)
                    }

                    else -> Unit
                }
            }
        }
    }

    // Get from database
    fun getSchedules() {
        viewModelScope.launch {
            schedulesRepository.getSchedules().collect {
                when (it) {
                    is Resource.Loading -> {
                        _fetchSchedules.value = Resource.Loading()
                    }

                    is Resource.Success -> {
                        _fetchSchedules.value = it
                    }

                    is Resource.Error -> {
                        _fetchSchedules.value = Resource.Error(it.message)
                    }

                    else -> Unit
                }
            }
        }
    }

}