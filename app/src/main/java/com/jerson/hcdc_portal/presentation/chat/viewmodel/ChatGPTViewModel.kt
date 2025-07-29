package com.jerson.hcdc_portal.presentation.chat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jerson.hcdc_portal.domain.model.Chat
import com.jerson.hcdc_portal.domain.repository.ChatGPTRepository
import com.jerson.hcdc_portal.util.NetworkMonitor
import com.jerson.hcdc_portal.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.FormBody
import javax.inject.Inject

@HiltViewModel
class ChatGPTViewModel @Inject constructor(
    private val repo:ChatGPTRepository,
    networkMonitor: NetworkMonitor
):ViewModel() {

    private val _fetchChatGPT = MutableStateFlow<Resource<String>?>(null)
    val fetchChatGPT:StateFlow<Resource<String>?> = _fetchChatGPT

    private val isConnected = networkMonitor.isConnected

    fun chat(chatList:List<Chat>){
        viewModelScope.launch {
            _fetchChatGPT.value = Resource.Loading()
            if(!isConnected.first()){
                _fetchChatGPT.value = Resource.Error("No internet connection.")
                return@launch
            }
            repo.chat(chatList).collect{
                when(it){
                    is Resource.Success->_fetchChatGPT.value = it
                    is Resource.Error->_fetchChatGPT.value = Resource.Error(it.message)
                    else->Unit
                }
            }
        }
    }
}