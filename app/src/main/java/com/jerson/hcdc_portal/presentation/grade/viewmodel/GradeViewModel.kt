package com.jerson.hcdc_portal.presentation.grade.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jerson.hcdc_portal.data.local.PortalDB
import com.jerson.hcdc_portal.domain.model.Grade
import com.jerson.hcdc_portal.domain.repository.GradesRepository
import com.jerson.hcdc_portal.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GradeViewModel @Inject constructor(
    private val gradesRepository: GradesRepository,
    private val db: PortalDB
) : ViewModel() {
    private val _fetchGrades = MutableSharedFlow<Resource<List<Grade>>>()
    val fetchGrades = _fetchGrades.asSharedFlow()


    fun fetchGrades(){
        viewModelScope.launch {
            gradesRepository.fetchGrades().collect{
                when(it){
                    is Resource.Loading->{

                    }
                    is Resource.Success ->{
                        _fetchGrades.emit(it)
                    }
                    is Resource.Error->{
                        _fetchGrades.emit(Resource.Error(it.message))
                    }
                    else -> Unit
                }
            }
        }
    }

}
