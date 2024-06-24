package com.jerson.hcdc_portal.presentation.accounts

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.jerson.hcdc_portal.databinding.FragmentAccountKtBinding
import com.jerson.hcdc_portal.presentation.accounts.viewmodel.AccountViewModel
import com.jerson.hcdc_portal.util.AppPreference
import com.jerson.hcdc_portal.util.Constants
import com.jerson.hcdc_portal.util.Resource
import com.jerson.hcdc_portal.util.SnackBarKt
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AccountsKt : Fragment() {
    private lateinit var binding: FragmentAccountKtBinding
    private val accountViewModel: AccountViewModel by viewModels()
    @Inject
    lateinit var pref: AppPreference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentAccountKtBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val isLoaded = pref.getBooleanPreference(Constants.KEY_IS_ACCOUNT_LOADED)
        if (isLoaded) accountViewModel.getAccounts()
        getAccounts()
    }


    fun getAccounts() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                accountViewModel.fetchAccounts.collect {
                    when (it) {
                        is Resource.Loading -> {
                            Log.e("HUHU", "getAccounts: Loading...")
                        }

                        is Resource.Success -> {
                            if (it.data!!.isNotEmpty()) {
                                binding.apply {
                                    tvDue.text = it.data[0].dueAmount
                                    tvDueText.text = it.data[0].dueTextPeriod
                                    tvTerm.text = it.data[0].term
                                }
                            } else {
                                binding.apply {
                                    tvDue.text = "Php 0.0"
                                    tvDueText.text = "Due Amount"
                                    tvTerm.text = "Select term"
                                }
                            }
                        }

                        is Resource.Error -> {
                            it.message?.let { msg -> SnackBarKt.snackBarLong(binding.root, msg) }
                        }

                        else -> Unit

                    }
                }
            }
        }
    }

}