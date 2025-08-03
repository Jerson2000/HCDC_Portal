package com.jerson.hcdc_portal.presentation.enrollhistory.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jerson.hcdc_portal.data.local.PortalDB
import com.jerson.hcdc_portal.domain.model.EnrollHistory
import com.jerson.hcdc_portal.domain.model.Term
import com.jerson.hcdc_portal.domain.repository.EnrollHistoryRepository
import com.jerson.hcdc_portal.util.AppPreference
import com.jerson.hcdc_portal.util.Constants
import com.jerson.hcdc_portal.util.NetworkMonitor
import com.jerson.hcdc_portal.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EnrollHistoryViewModel @Inject constructor(
    private val repository: EnrollHistoryRepository,
    private val pref:AppPreference,
    networkMonitor: NetworkMonitor
):ViewModel() {

    private val _fetchEnrollHistory = MutableStateFlow<Resource<List<EnrollHistory>>?>(null)
    val fetchEnrollHistory:StateFlow<Resource<List<EnrollHistory>>?> = _fetchEnrollHistory

    private val _fetchTerms = MutableStateFlow<Resource<List<Term>>?>(null)
    val fetchTerms:StateFlow<Resource<List<Term>>?> = _fetchTerms

    private val isConnected = networkMonitor.isConnected

    init {
        val isLoaded = pref.getBooleanPreference(Constants.KEY_IS_ENROLL_HISTORY_LOADED)
        if(!isLoaded){
            fetchEnrollHistory()
        }

    }
    fun fetchEnrollHistory(term:Term){
        viewModelScope.launch {
            _fetchEnrollHistory.value = Resource.Loading()

            if(!isConnected.first()){
                _fetchEnrollHistory.value = Resource.Error("No internet connection.")
                return@launch
            }

            repository.fetchEnrollHistory(term).collect{
                when(it){
                    is Resource.Success ->{
                        _fetchEnrollHistory.value = it
                        pref.setBooleanPreference(Constants.KEY_IS_ENROLL_HISTORY_LOADED,true)
                    }
                    is Resource.Error->_fetchEnrollHistory.value = Resource.Error(it.message)
                    else -> Unit
                }
            }
        }
    }

    fun fetchEnrollHistory(){
        viewModelScope.launch {
            _fetchEnrollHistory.value = Resource.Loading()

            if(!isConnected.first()){
                _fetchEnrollHistory.value = Resource.Error("No internet connection.")
                return@launch
            }
            repository.fetchEnrollHistory().collect{
                when(it){
                    is Resource.Success ->{
                        _fetchEnrollHistory.value = it
                        pref.setBooleanPreference(Constants.KEY_IS_ENROLL_HISTORY_LOADED,true)
                    }
                    is Resource.Error->_fetchEnrollHistory.value = Resource.Error(it.message)
                    else -> Unit
                }
            }
        }
    }

    fun getEnrollHistory(termId:Int){
        viewModelScope.launch {
            repository.getEnrollHistory(termId)
                .collect {
                    when(it){
                        is Resource.Loading->{
                            _fetchEnrollHistory.value = Resource.Loading()
                        }
                        is Resource.Success ->{
                            _fetchEnrollHistory.value = it
                        }
                        is Resource.Error->{
                            _fetchEnrollHistory.value = Resource.Error(it.message)
                        }
                        else -> Unit
                    }
                }
        }
    }
    fun getEnrollHistoryTerm(){
        viewModelScope.launch {
            repository.getEnrollHistoryTerms()
                .collect {
                    when(it){
                        is Resource.Loading->{
                            _fetchTerms.value = Resource.Loading()
                        }
                        is Resource.Success ->{
                            _fetchTerms.value = it
                        }
                        is Resource.Error->{
                            _fetchTerms.value = Resource.Error(it.message)
                        }
                        else -> Unit
                    }
                }
        }
    }

    fun hasData(term:Term,hasData:(Boolean)-> Unit){
        viewModelScope.launch {
            repository.hasData(term,hasData)
        }
    }

}