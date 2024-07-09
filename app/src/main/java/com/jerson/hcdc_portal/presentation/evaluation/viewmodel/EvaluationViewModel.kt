package com.jerson.hcdc_portal.presentation.evaluation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jerson.hcdc_portal.domain.repository.EvaluationRepository
import com.jerson.hcdc_portal.util.AppPreference
import com.jerson.hcdc_portal.util.Constants
import com.jerson.hcdc_portal.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EvaluationViewModel @Inject constructor(
    private val repo:EvaluationRepository,
    private val pref:AppPreference
) :ViewModel(){
    private val _fetchEvaluation = MutableSharedFlow<Resource<String>>()
    val fetchEvaluation = _fetchEvaluation.asSharedFlow()

    init {
        val isLoaded = pref.getBooleanPreference(Constants.KEY_HTML_EVALUATION_LOADED)
        if(!isLoaded){
            fetchEvaluation()
            pref.setBooleanPreference(Constants.KEY_HTML_EVALUATION_LOADED,true)
        }
    }

    fun fetchEvaluation(){
        viewModelScope.launch {
            repo.fetchEvaluation().collect{
                when(it){
                    is Resource.Loading->{
                        _fetchEvaluation.emit(Resource.Loading())
                    }
                    is Resource.Success->{
                        Log.e("HUHU", "fetchEvaluation: $it", )
                        _fetchEvaluation.emit(it)
                    }
                    is Resource.Error ->{
                        _fetchEvaluation.emit(Resource.Error(it.message))
                    }
                    else -> Unit
                }
            }
        }
    }
    fun getEvaluation(){
        viewModelScope.launch {
            val eval = pref.getStringPreference(Constants.KEY_HTML_EVALUATION)
            _fetchEvaluation.emit(Resource.Success(eval))
            /*Log.e("HUHU", "getEvaluation: $eval", )*/
        }
    }


}