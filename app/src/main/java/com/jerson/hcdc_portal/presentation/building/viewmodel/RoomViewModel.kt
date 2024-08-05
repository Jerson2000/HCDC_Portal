package com.jerson.hcdc_portal.presentation.building.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jerson.hcdc_portal.domain.model.Room
import com.jerson.hcdc_portal.domain.repository.RoomRepository
import com.jerson.hcdc_portal.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RoomViewModel @Inject constructor(
    private val roomRepository: RoomRepository
): ViewModel() {

    private val _fetchRoom = MutableStateFlow<Resource<Room>?>(null)
    val fetchRoom:StateFlow<Resource<Room>?> = _fetchRoom

    init {
        fetchRoom()
    }

    private fun fetchRoom(){
        viewModelScope.launch {
            roomRepository.getBuilding().collect{
                when(it){
                    is Resource.Loading->{
                        _fetchRoom.value = Resource.Loading()
                    }
                    is Resource.Success ->{
                        _fetchRoom.value = it
                    }
                    is Resource.Error ->{
                        _fetchRoom.value = Resource.Error(it.message)
                    }

                    else -> Unit
                }

            }
        }
    }

}