package com.jerson.hcdc_portal.presentation.grade.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jerson.hcdc_portal.data.local.PortalDB
import com.jerson.hcdc_portal.domain.model.Grade
import com.jerson.hcdc_portal.domain.model.Term
import com.jerson.hcdc_portal.domain.repository.GradesRepository
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
class GradeViewModel @Inject constructor(
    private val repository: GradesRepository,
    private val db: PortalDB,
    private val pref: AppPreference,
    networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _fetchGrades = MutableStateFlow<Resource<List<Grade>>?>(null)
    val fetchGrades: StateFlow<Resource<List<Grade>>?> = _fetchGrades

    private val _fetchTerms = MutableStateFlow<Resource<List<Term>>?>(null)
    val fetchTerms: StateFlow<Resource<List<Term>>?> = _fetchTerms

    private val isConnected = networkMonitor.isConnected
    fun fetchGrades(term:Term) {

        viewModelScope.launch {
            _fetchGrades.value = Resource.Loading()

            if(!isConnected.first()){
                _fetchGrades.value = Resource.Error("No internet connection.")
                return@launch
            }

            repository.fetchGrades(term).collect {
                when (it) {
                    is Resource.Success -> {
                        _fetchGrades.value = it
                        pref.setBooleanPreference(Constants.KEY_IS_GRADE_LOADED, true)
                    }
                    is Resource.Error -> _fetchGrades.value = Resource.Error(it.message)
                    else -> Unit
                }
            }
        }
    }

     fun fetchGrades() {
        viewModelScope.launch {
            _fetchGrades.value = Resource.Loading()

            if(!isConnected.first()){
                _fetchGrades.value = Resource.Error("No internet connection.")
                return@launch
            }
            repository.fetchGrades().collect {
                when (it) {
                    is Resource.Success -> _fetchGrades.value = it
                    is Resource.Error -> _fetchGrades.value = Resource.Error(it.message)

                    else -> Unit
                }
            }
        }
    }

    fun getGrades(termId: Int) {
        viewModelScope.launch {
            repository.getGrades(termId).collect {
                when (it) {
                    is Resource.Loading -> {
                        _fetchGrades.value = Resource.Loading()
                    }

                    is Resource.Success -> {
                        _fetchGrades.value = it
                    }

                    is Resource.Error -> {
                        _fetchGrades.value = Resource.Error(it.message)
                    }

                    else -> Unit
                }
            }
        }
    }

    fun getGradeTerm() {
        viewModelScope.launch {
            repository.getGradeTerms()
                .collect {
                    when (it) {
                        is Resource.Loading -> {
                            _fetchTerms.value = Resource.Loading()
                        }

                        is Resource.Success -> {
                            _fetchTerms.value = it
                        }

                        is Resource.Error -> {
                            _fetchTerms.value = Resource.Error(it.message)
                        }

                        else -> Unit
                    }
                }
        }
    }

    fun hasData(term: Term, hasData: (Boolean) -> Unit) {
        viewModelScope.launch {
            repository.hasData(term, hasData)
        }
    }

}
