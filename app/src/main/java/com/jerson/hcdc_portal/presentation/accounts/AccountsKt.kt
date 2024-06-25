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
import com.jerson.hcdc_portal.util.LoadingDialog
import com.jerson.hcdc_portal.util.Resource
import com.jerson.hcdc_portal.util.SnackBarKt
import com.jerson.hcdc_portal.util.TermSelectionDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AccountsKt : Fragment() {
    private lateinit var binding: FragmentAccountKtBinding
    private val accountViewModel: AccountViewModel by viewModels()
    private var loadingDialog: LoadingDialog? = null
    private var termDialog: TermSelectionDialog? = null

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
        loadingDialog = context?.let { LoadingDialog(it) }
        termDialog = context?.let { TermSelectionDialog(it) }

        val isLoaded = pref.getBooleanPreference(Constants.KEY_IS_ACCOUNT_LOADED)

        if (isLoaded) accountViewModel.getAccounts(pref.getIntPreference(Constants.KEY_SELECTED_ACCOUNT_TERM))
        getAccounts()
        accountViewModel.getAccountTerm()
        getTerms()

        binding.cardTerm.setOnClickListener {
            termDialog?.showDialog {term ->
                accountViewModel.hasData(term) {
                    pref.setIntPreference(Constants.KEY_SELECTED_ACCOUNT_TERM,term.id)
                    if (!it) {
                        accountViewModel.fetchAccounts(term)
                    }else{
                        accountViewModel.getAccounts(term.id)
                    }
                }
            }
        }
    }


    fun getAccounts() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                accountViewModel.fetchAccounts.collect {
                    when (it) {
                        is Resource.Loading -> {
                            loadingDialog?.show()
                        }

                        is Resource.Success -> {
                            loadingDialog?.dismiss()
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
                            loadingDialog?.dismiss()
                            it.message?.let { msg -> SnackBarKt.snackBarLong(binding.root, msg) }
                        }

                        else -> Unit

                    }
                }
            }
        }
    }

    private fun getTerms() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                accountViewModel.fetchTerms.collect {
                    when (it) {
                        is Resource.Loading -> {
                            loadingDialog?.show()
                        }

                        is Resource.Success -> {
                            loadingDialog?.dismiss()
                            it.data?.let { it1 -> termDialog?.setTerms(it1) }

                        }

                        is Resource.Error -> {
                            loadingDialog?.dismiss()
                            it.message?.let { msg -> SnackBarKt.snackBarLong(binding.root, msg) }
                        }

                        else -> Unit
                    }
                }
            }
        }
    }

}