package com.jerson.hcdc_portal.presentation.subjects_offered.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jerson.hcdc_portal.domain.model.SubjectOffered
import com.jerson.hcdc_portal.domain.repository.SubjectOfferedRepository
import com.jerson.hcdc_portal.util.AppPreference
import com.jerson.hcdc_portal.util.Constants
import com.jerson.hcdc_portal.util.Constants.baseUrl
import com.jerson.hcdc_portal.util.Constants.subjectOffered
import com.jerson.hcdc_portal.util.NetworkMonitor
import com.jerson.hcdc_portal.util.Resource
import com.jerson.hcdc_portal.util.await
import com.jerson.hcdc_portal.util.postRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.executeAsync
import javax.inject.Inject

@HiltViewModel
class SubjectOfferedViewModel @Inject constructor(
    private val repo: SubjectOfferedRepository,
    networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _fetchSubjectOffered = MutableStateFlow<Resource<List<SubjectOffered>>?>(null)
    val fetchSubjectOffered: StateFlow<Resource<List<SubjectOffered>>?> = _fetchSubjectOffered
    val isConnected = networkMonitor.isConnected


    fun fetchSubjectOffered(page: Int) {
        viewModelScope.launch {
            _fetchSubjectOffered.value = Resource.Loading()

            if (!isConnected.first()) {
                _fetchSubjectOffered.value = Resource.Error("No internet connection.")
                return@launch
            }

            repo.fetchSubjectOffered(page).collect { result ->
                when (result) {
                    is Resource.Error -> _fetchSubjectOffered.value = Resource.Error(result.message)

                    is Resource.Success -> _fetchSubjectOffered.value = result

                    else -> Unit
                }
            }
        }
    }

    fun searchSubjectOffered(query: String?) {
        viewModelScope.launch {
            _fetchSubjectOffered.value = Resource.Loading()

            if (!isConnected.first()) {
                _fetchSubjectOffered.value = Resource.Error("No internet connection.")
                return@launch
            }
            repo.fetchSubjectOffered(query).collect {
                when (it) {
                    is Resource.Success -> _fetchSubjectOffered.value = it

                    is Resource.Error -> _fetchSubjectOffered.value = Resource.Error(it.message)

                    else -> Unit
                }
            }
        }
    }


}