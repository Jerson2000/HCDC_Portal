package com.jerson.hcdc_portal.presentation.accounts.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jerson.hcdc_portal.data.local.PortalDB
import com.jerson.hcdc_portal.domain.model.Account
import com.jerson.hcdc_portal.domain.repository.AccountsRepository
import com.jerson.hcdc_portal.util.AppPreference
import com.jerson.hcdc_portal.util.Constants
import com.jerson.hcdc_portal.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val db: PortalDB,
    private val repository: AccountsRepository,
    private val pref: AppPreference
) : ViewModel() {

    private val _fetchAccounts = MutableSharedFlow<Resource<List<Account>>>()
    val fetchAccounts = _fetchAccounts.asSharedFlow()


    init {
        val isLoaded = pref.getBooleanPreference(Constants.KEY_IS_ACCOUNT_LOADED)

        if (!isLoaded) {
            fetchAccounts()
            pref.setBooleanPreference(Constants.KEY_IS_ACCOUNT_LOADED, true)
        }

    }

    fun fetchAccounts() {
        viewModelScope.launch {
            repository.fetchAccounts().collect {
                when (it) {
                    is Resource.Loading -> {
                        _fetchAccounts.emit(Resource.Loading())
                    }

                    is Resource.Success -> {
                        _fetchAccounts.emit(it)
                    }

                    is Resource.Error -> {
                        _fetchAccounts.emit(Resource.Error(it.message))
                    }

                    else -> Unit
                }
            }
        }

    }

    fun getAccounts() {
        viewModelScope.launch {
            repository.getAccounts(pref.getIntPreference(Constants.KEY_SELECTED_ACCOUNT_TERM))
                .collect {
                    when (it) {
                        is Resource.Loading -> {
                            _fetchAccounts.emit(Resource.Loading())
                        }

                        is Resource.Success -> {
                            _fetchAccounts.emit(it)
                        }

                        is Resource.Error -> {
                            _fetchAccounts.emit(Resource.Error(it.message))
                        }

                        else -> Unit
                    }
                }
        }
    }

}