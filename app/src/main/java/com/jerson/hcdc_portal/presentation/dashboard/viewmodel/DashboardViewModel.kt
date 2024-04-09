package com.jerson.hcdc_portal.presentation.dashboard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jerson.hcdc_portal.domain.model.Schedule
import com.jerson.hcdc_portal.domain.repository.SchedulesRepository
import com.jerson.hcdc_portal.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val schedulesRepository: SchedulesRepository
) : ViewModel() {

    private val _fetchSchedules = MutableSharedFlow<Resource<List<Schedule>>>()
    val fetchSchedules = _fetchSchedules.asSharedFlow()

    fun fetchSchedules() {
        viewModelScope.launch {
            schedulesRepository.fetchSchedules().collect {
                when (it) {
                    is Resource.Loading -> {
                        _fetchSchedules.emit(Resource.Loading())
                    }

                    is Resource.Success -> {
                        _fetchSchedules.emit(it)
                    }

                    is Resource.Error -> {
                        _fetchSchedules.emit(Resource.Error(it.message))
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
                        _fetchSchedules.emit(Resource.Loading())
                    }

                    is Resource.Success -> {
                        _fetchSchedules.emit(it)
                    }

                    is Resource.Error -> {
                        _fetchSchedules.emit(Resource.Error(it.message))
                    }

                    else -> Unit
                }
            }
        }
    }

}