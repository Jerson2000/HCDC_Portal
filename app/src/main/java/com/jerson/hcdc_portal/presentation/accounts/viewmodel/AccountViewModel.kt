package com.jerson.hcdc_portal.presentation.accounts.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jerson.hcdc_portal.data.local.PortalDB
import com.jerson.hcdc_portal.domain.model.Account
import com.jerson.hcdc_portal.domain.model.Term
import com.jerson.hcdc_portal.domain.repository.AccountsRepository
import com.jerson.hcdc_portal.util.AppPreference
import com.jerson.hcdc_portal.util.Constants
import com.jerson.hcdc_portal.util.NetworkMonitor
import com.jerson.hcdc_portal.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val db: PortalDB,
    private val repository: AccountsRepository,
    private val pref: AppPreference,
    networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _fetchAccounts = MutableStateFlow<Resource<List<Account>>?>(null)
    val fetchAccounts: StateFlow<Resource<List<Account>>?> = _fetchAccounts

    private val _fetchTerms = MutableStateFlow<Resource<List<Term>>?>(null)
    val fetchTerms: StateFlow<Resource<List<Term>>?> = _fetchTerms
    private val isConnected= networkMonitor.isConnected

    init {
        val isLoaded = pref.getBooleanPreference(Constants.KEY_IS_ACCOUNT_LOADED)

        if (!isLoaded) {
            fetchAccounts()
            pref.setBooleanPreference(Constants.KEY_IS_ACCOUNT_LOADED, true)
        }

    }

    fun fetchAccounts(termId: Term) {
        viewModelScope.launch {
            _fetchAccounts.value = Resource.Loading()

            if (!isConnected.first()) {
                _fetchAccounts.value = Resource.Error("No internet connection.")
                return@launch
            }
            repository.fetchAccounts(termId).collect {
                when (it) {
                    is Resource.Success -> _fetchAccounts.value = it
                    is Resource.Error -> _fetchAccounts.value = Resource.Error(it.message)
                    else -> Unit
                }
            }
        }
    }

    fun fetchAccounts() {
        viewModelScope.launch {
            _fetchAccounts.value = Resource.Loading()

            if (!isConnected.first()) {
                _fetchAccounts.value = Resource.Error("No internet connection.")
                return@launch
            }
            repository.fetchAccounts().collect {
                when (it) {
                    is Resource.Success -> _fetchAccounts.value = it
                    is Resource.Error -> _fetchAccounts.value = Resource.Error(it.message)
                    else -> Unit
                }
            }
        }
    }

    fun getAccounts(termId: Int) {
        viewModelScope.launch {
            repository.getAccounts(termId)
                .collect {
                    when (it) {
                        is Resource.Loading -> {
                            _fetchAccounts.value = Resource.Loading()
                        }

                        is Resource.Success -> {
                            _fetchAccounts.value = it
                        }

                        is Resource.Error -> {
                            _fetchAccounts.value = Resource.Error(it.message)
                        }

                        else -> Unit
                    }
                }
        }
    }

    fun getAccountTerm() {
        viewModelScope.launch {
            repository.getAccountTerm()
                .collect {
                    when (it) {
                        is Resource.Loading -> {
                            _fetchTerms.value = Resource.Loading()
                        }

                        is Resource.Success -> {
                            _fetchTerms.value = it
                        }

                        is Resource.Error -> {
                            _fetchTerms.value = Resource.Error(it.message)
                        }

                        else -> Unit
                    }
                }
        }
    }

    fun hasData(term: Term, hasData: (Boolean) -> Unit) {
        viewModelScope.launch {
            repository.hasData(term, hasData)
        }
    }

}