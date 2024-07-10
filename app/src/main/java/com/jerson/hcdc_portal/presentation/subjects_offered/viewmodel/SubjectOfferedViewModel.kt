package com.jerson.hcdc_portal.presentation.subjects_offered.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jerson.hcdc_portal.domain.model.SubjectOffered
import com.jerson.hcdc_portal.domain.repository.SubjectOfferedRepository
import com.jerson.hcdc_portal.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubjectOfferedViewModel @Inject constructor(
    private val repo:SubjectOfferedRepository
): ViewModel() {

    private val _fetchSubjectOffered = MutableSharedFlow<Resource<List<SubjectOffered>>>()
    val fetchSubjectOffered = _fetchSubjectOffered.asSharedFlow()


    fun huhu(){
        viewModelScope.launch {
            repo.fetchSubjectOffered(1).collect{
                when(it){
                    is Resource.Loading ->{
                        _fetchSubjectOffered.emit(Resource.Loading())
                    }
                    is Resource.Success ->{
                        _fetchSubjectOffered.emit(it)
                    }
                    is Resource.Error ->{
                        _fetchSubjectOffered.emit(Resource.Error(it.message))
                    }
                    else -> Unit
                }
            }
        }
    }


}