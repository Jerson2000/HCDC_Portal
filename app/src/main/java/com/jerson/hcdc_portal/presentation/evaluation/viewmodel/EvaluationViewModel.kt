package com.jerson.hcdc_portal.presentation.evaluation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jerson.hcdc_portal.domain.repository.EvaluationRepository
import com.jerson.hcdc_portal.util.AppPreference
import com.jerson.hcdc_portal.util.Constants
import com.jerson.hcdc_portal.util.NetworkMonitor
import com.jerson.hcdc_portal.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EvaluationViewModel @Inject constructor(
    private val repo:EvaluationRepository,
    private val pref:AppPreference,
    networkMonitor: NetworkMonitor
) :ViewModel(){
    private val _fetchEvaluation = MutableStateFlow<Resource<String>?>(null)
    val fetchEvaluation: StateFlow<Resource<String>?> = _fetchEvaluation
    val fetchLacking : StateFlow<Resource<String>?> = _fetchEvaluation

    private val isConnected = networkMonitor.isConnected
    fun fetchEvaluation(){
        viewModelScope.launch {
            _fetchEvaluation.value = Resource.Loading()
            if(!isConnected.first()){
                _fetchEvaluation.value = Resource.Error("No internet connection.")
                return@launch
            }
            repo.fetchEvaluation().collect{
                when(it){
                    is Resource.Success->{
                        pref.setBooleanPreference(Constants.KEY_HTML_EVALUATION_LOADED,true)
                        _fetchEvaluation.value = it
                    }
                    is Resource.Error ->_fetchEvaluation.value = Resource.Error(it.message)
                    else -> Unit
                }
            }
        }
    }

    // In this case I just reuse the stateFlow as for the lacking because they're the same response data
    fun fetchLacking(){
        viewModelScope.launch {
            _fetchEvaluation.value = Resource.Loading()
            if(!isConnected.first()){
                _fetchEvaluation.value = Resource.Error("No internet connection.")
                return@launch
            }
            repo.fetchLacking().collect{
                when(it){
                    is Resource.Success->_fetchEvaluation.value = it
                    is Resource.Error -> _fetchEvaluation.value = Resource.Error(it.message)
                    else -> Unit
                }
            }
        }
    }


}