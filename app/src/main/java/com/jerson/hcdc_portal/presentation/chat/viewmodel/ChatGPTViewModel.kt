package com.jerson.hcdc_portal.presentation.chat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jerson.hcdc_portal.domain.model.ChatGPT
import com.jerson.hcdc_portal.domain.repository.ChatGPTRepository
import com.jerson.hcdc_portal.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.FormBody
import javax.inject.Inject

@HiltViewModel
class ChatGPTViewModel @Inject constructor(
    private val repo:ChatGPTRepository
):ViewModel() {

    private val _fetchChatGPT = MutableStateFlow<Resource<ChatGPT>?>(null)
    val fetchChatGPT:StateFlow<Resource<ChatGPT>?> = _fetchChatGPT

    private val _fetchChatDataValue = MutableStateFlow<Resource<FormBody.Builder>?>(null)
    val fetchChatDataValue:StateFlow<Resource<FormBody.Builder>?> = _fetchChatDataValue

    init {
        fetchChatDataValue()
    }

    private fun fetchChatDataValue(){
        viewModelScope.launch {
            repo.chatDataValue().collect{
                when(it){
                    is Resource.Loading->{
                        _fetchChatDataValue.value = Resource.Loading()
                    }
                    is  Resource.Success->{
                        _fetchChatDataValue.value = it
                    }
                    is Resource.Error->{
                        _fetchChatDataValue.value = Resource.Error(it.message)
                    }
                    else -> Unit
                }
            }
        }
    }

    fun chat(formBody: FormBody){
        viewModelScope.launch {
            repo.chat(formBody).collect{
                when(it){
                    is Resource.Loading->{
                        _fetchChatGPT.value = Resource.Loading()
                    }
                    is Resource.Success->{
                        _fetchChatGPT.value = it
                    }
                    is Resource.Error->{
                        _fetchChatGPT.value = Resource.Error(it.message)
                    }
                    else->Unit
                }
            }
        }
    }
}