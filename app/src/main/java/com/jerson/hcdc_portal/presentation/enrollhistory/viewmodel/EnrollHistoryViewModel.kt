package com.jerson.hcdc_portal.presentation.enrollhistory.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jerson.hcdc_portal.data.local.PortalDB
import com.jerson.hcdc_portal.domain.model.EnrollHistory
import com.jerson.hcdc_portal.domain.repository.EnrollHistoryRepository
import com.jerson.hcdc_portal.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EnrollHistoryViewModel @Inject constructor(
    private val enrollHistoryRepository: EnrollHistoryRepository,
    private val db:PortalDB
):ViewModel() {
    private val _fetchEnrollHistory = MutableSharedFlow<Resource<List<EnrollHistory>>>()
    val fetchEnrollHistory = _fetchEnrollHistory.asSharedFlow()

    fun fetchEnrollHistory(){
        viewModelScope.launch {
            enrollHistoryRepository.fetchEnrollHistory().collect{
                when(it){
                    is Resource.Loading->{
                        _fetchEnrollHistory.emit(Resource.Loading())
                    }
                    is Resource.Success ->{
                        _fetchEnrollHistory.emit(it)
                    }
                    is Resource.Error->{
                        _fetchEnrollHistory.emit(Resource.Error(it.message))
                    }
                    else -> Unit
                }
            }
        }
    }
}