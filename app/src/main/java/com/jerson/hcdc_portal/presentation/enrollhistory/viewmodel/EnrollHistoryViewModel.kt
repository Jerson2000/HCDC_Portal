package com.jerson.hcdc_portal.presentation.enrollhistory.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jerson.hcdc_portal.data.local.PortalDB
import com.jerson.hcdc_portal.domain.model.EnrollHistory
import com.jerson.hcdc_portal.domain.model.Term
import com.jerson.hcdc_portal.domain.repository.EnrollHistoryRepository
import com.jerson.hcdc_portal.util.AppPreference
import com.jerson.hcdc_portal.util.Constants
import com.jerson.hcdc_portal.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EnrollHistoryViewModel @Inject constructor(
    private val repository: EnrollHistoryRepository,
    private val db:PortalDB,
    private val pref:AppPreference
):ViewModel() {
    private val _fetchEnrollHistory = MutableSharedFlow<Resource<List<EnrollHistory>>>()
    val fetchEnrollHistory = _fetchEnrollHistory.asSharedFlow()

    private val _fetchTerms = MutableSharedFlow<Resource<List<Term>>>()
    val fetchTerms = _fetchTerms.asSharedFlow()

    init {
        val isLoaded = pref.getBooleanPreference(Constants.KEY_IS_ENROLL_HISTORY_LOADED)
        if(!isLoaded){
            fetchEnrollHistory()
            pref.setBooleanPreference(Constants.KEY_IS_ENROLL_HISTORY_LOADED,true)
        }

    }
    fun fetchEnrollHistory(term:Term){
        viewModelScope.launch {
            repository.fetchEnrollHistory(term).collect{
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

    fun fetchEnrollHistory(){
        viewModelScope.launch {
            repository.fetchEnrollHistory().collect{
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

    fun getEnrollHistory(termId:Int){
        viewModelScope.launch {
            repository.getEnrollHistory(termId)
                .collect {
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
    fun getEnrollHistoryTerm(){
        viewModelScope.launch {
            repository.getEnrollHistoryTerms()
                .collect {
                    when(it){
                        is Resource.Loading->{
                            _fetchTerms.emit(Resource.Loading())
                        }
                        is Resource.Success ->{
                            _fetchTerms.emit(it)
                        }
                        is Resource.Error->{
                            _fetchTerms.emit(Resource.Error(it.message))
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