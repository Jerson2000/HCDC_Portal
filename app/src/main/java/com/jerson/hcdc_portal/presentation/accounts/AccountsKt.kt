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
import com.jerson.hcdc_portal.presentation.dashboard.viewmodel.DashboardViewModel
import com.jerson.hcdc_portal.util.Resource
import com.jerson.hcdc_portal.util.SnackBarKt
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AccountsKt : Fragment() {
    private lateinit var binding: FragmentAccountKtBinding
    private val accountViewModel: AccountViewModel by viewModels()
    private val dashboardViewModel: DashboardViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentAccountKtBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        accountViewModel.getAccounts()
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
                           binding.apply{
                               tvDue.text = it.data?.get(0)?.dueAmount ?: "00"
                               tvDueText.text = it.data?.get(0)?.dueTextPeriod ?: "Due Amount"
                               tvTerm.text = it.data?.get(0)?.term ?: "Select Term"

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

    private fun listenerFetch() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                dashboardViewModel.fetchSchedules.collect {
                    when (it) {
                        is Resource.Loading -> {

                        }

                        is Resource.Success -> {
                            for(x in it.data!!)
                                Log.e("HUHU", "listenerFetch: ${x.subjectCode}", )
                        }

                        is Resource.Error -> {

                        }

                        else -> Unit
                    }
                }
            }
        }
    }

}