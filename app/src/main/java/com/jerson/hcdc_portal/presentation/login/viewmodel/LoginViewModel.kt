package com.jerson.hcdc_portal.presentation.login.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jerson.hcdc_portal.data.remote.HttpClients
import com.jerson.hcdc_portal.domain.repository.LoginRepository
import com.jerson.hcdc_portal.util.Constants
import com.jerson.hcdc_portal.util.Resource
import com.jerson.hcdc_portal.util.await
import com.jerson.hcdc_portal.util.copy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import java.net.HttpURLConnection
import javax.inject.Inject
@HiltViewModel
class LoginViewModel@Inject constructor(
    private val loginRepository: LoginRepository,
    private val client:OkHttpClient
) :ViewModel(){

    private val _login = MutableSharedFlow<Resource<String>>()
    val login = _login.asSharedFlow()

    private val _session = MutableSharedFlow<Resource<Boolean>>()
    val session = _session.asSharedFlow()


    fun login(email:String,pass:String){
        viewModelScope.launch {
            loginRepository.login(email, pass).collect{
                when(it){
                    is Resource.Loading->{
                        _login.emit(Resource.Loading())
                    }
                    is Resource.Success ->{
                        _login.emit(it)
                    }

                    is Resource.Error->{
                        _login.emit(Resource.Error(it.message))
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
                        _session.emit(Resource.Loading())
                    }
                    is Resource.Success ->{
                        _session.emit(it)
                    }

                    is Resource.Error->{
                        _session.emit(Resource.Error(it.message))
                    }
                    else -> Unit
                }
            }
        }
    }



}