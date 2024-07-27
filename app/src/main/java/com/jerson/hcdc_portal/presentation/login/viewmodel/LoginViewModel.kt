package com.jerson.hcdc_portal.presentation.login.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jerson.hcdc_portal.domain.repository.LoginRepository
import com.jerson.hcdc_portal.util.AppPreference
import com.jerson.hcdc_portal.util.Constants.KEY_EMAIL
import com.jerson.hcdc_portal.util.Constants.KEY_PASSWORD
import com.jerson.hcdc_portal.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel@Inject constructor(
    private val loginRepository: LoginRepository,
    private val pref:AppPreference
) :ViewModel(){

    private val _login = MutableStateFlow<Resource<String>?>(null)
    val login: StateFlow<Resource<String>?> = _login

    private val _session = MutableStateFlow<Resource<Boolean>?>(null)
    val session: StateFlow<Resource<Boolean>?> = _session


    fun login(email:String,pass:String){
        viewModelScope.launch {
            loginRepository.login(email, pass).collect{
                when(it){
                    is Resource.Loading->{
                        _login.value = Resource.Loading()
                    }
                    is Resource.Success ->{
                        _login.value  = it
                    }

                    is Resource.Error->{
                        _login.value = Resource.Error(it.message)
                    }
                    else -> Unit
                }
            }
        }

    }
    fun checkSession(){
        viewModelScope.launch {
            loginRepository.checkSession().collect{
                when(it){
                    is Resource.Loading->{
                        _session.value = Resource.Loading()
                    }
                    is Resource.Success ->{
                        _session.value = it
                    }

                    is Resource.Error->{
                        _session.value = Resource.Error(it.message)
                    }
                    else -> Unit
                }
            }
        }
    }
    fun reLogon(){
        viewModelScope.launch {
            loginRepository.login(pref.getStringPreference(KEY_EMAIL),pref.getStringPreference(KEY_PASSWORD)).collect{
                when(it){
                    is Resource.Loading->{
                        _login.value = Resource.Loading()
                    }
                    is Resource.Success->{
                        _login.value = it
                    }
                    is Resource.Error ->{
                        _login.value = Resource.Error(it.message)
                    }
                    else -> Unit
                }
            }
        }

    }



}