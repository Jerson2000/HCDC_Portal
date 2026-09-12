package com.jerson.hcdc_portal.presentation.accounts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import coil.load
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jerson.hcdc_portal.R
import com.jerson.hcdc_portal.databinding.FragmentAccountKtBinding
import com.jerson.hcdc_portal.domain.model.Account
import com.jerson.hcdc_portal.domain.model.Term
import com.jerson.hcdc_portal.presentation.accounts.viewmodel.AccountViewModel
import com.jerson.hcdc_portal.presentation.login.viewmodel.LoginViewModel
import com.jerson.hcdc_portal.util.AppPreference
import com.jerson.hcdc_portal.util.Constants
import com.jerson.hcdc_portal.util.LoadingDialog
import com.jerson.hcdc_portal.util.Resource
import com.jerson.hcdc_portal.util.SnackBarKt
import com.jerson.hcdc_portal.util.TermSelectionDialog
import com.jerson.hcdc_portal.util.convertToReadableTerm
import com.jerson.hcdc_portal.util.setupCustomTheme
import com.jerson.hcdc_portal.util.stopRefreshing
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AccountsKt : Fragment() {
    private var _binding: FragmentAccountKtBinding? = null
    private val binding get() = _binding!!
    private val accountViewModel: AccountViewModel by viewModels()
    private val loginViewModel: LoginViewModel by viewModels()
    private var loadingDialog: LoadingDialog? = null
    private var termDialog: TermSelectionDialog? = null
    private var selectedTerm: Term? = null
    private val list = mutableListOf<Account>()
    private var isRefresh = false

    @Inject
    lateinit var pref: AppPreference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountKtBinding.inflate(inflater, container, false)
        loadingDialog = context?.let { LoadingDialog(it) }
        termDialog = context?.let { TermSelectionDialog(it) }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.swipeRefresh.setupCustomTheme(requireContext())
        binding.swipeRefresh.setOnRefreshListener {
            isRefresh = true
            accountViewModel.fetchAccounts()
        }

        val isLoaded = pref.getBooleanPreference(Constants.KEY_IS_ACCOUNT_LOADED)
        if (isLoaded) accountViewModel.getAccounts(pref.getIntPreference(Constants.KEY_SELECTED_ACCOUNT_TERM))

        getAccounts { isDone->
            if (isDone) {
                accountViewModel.getAccountTerm()
            }
        }
        getTerms()
        reLogonResponse()
        binding.cardTerm.setOnClickListener {
            termDialog?.showDialog { term ->
                selectedTerm = term
                accountViewModel.hasData(term) {hasData->
                    pref.setIntPreference(Constants.KEY_SELECTED_ACCOUNT_TERM, term.id)
                    if (!hasData) {
                        accountViewModel.fetchAccounts(term)
                    } else {
                        accountViewModel.getAccounts(term.id)
                    }
                }
            }
        }

        binding.btnViewDetails.setOnClickListener {
            if (list.isNotEmpty() && !list[0].description.isNullOrBlank()) {
                val bundle = bundleOf("objectList" to list)
                setFragmentResult("requestKey", bundle)
                findNavController().navigate(R.id.action_accounts_to_accountDetails, bundle)
            }
        }

        binding.btnRefresh.setOnClickListener {
            binding.swipeRefresh.isRefreshing = true
            isRefresh = true
            accountViewModel.fetchAccounts()
        }

    }

    fun getAccounts(isDone: (Boolean) -> Unit) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                accountViewModel.fetchAccounts.collect {
                    when (it) {
                        is Resource.Loading -> {
                            if (it.data.isNullOrEmpty() && list.isEmpty()) {
                                _binding?.swipeRefresh?.isRefreshing = true
                            } else {
                                updateAccountsUi(it.data.orEmpty(), isDone)
                            }
                        }

                        is Resource.Success -> {
                            _binding?.swipeRefresh?.stopRefreshing()
                            loadingDialog?.dismiss()
                            updateAccountsUi(it.data.orEmpty(), isDone)
                        }

                        is Resource.Error -> {
                            isDone(true)
                            _binding?.swipeRefresh?.stopRefreshing()
                            loadingDialog?.dismiss()
                            if (!it.data.isNullOrEmpty()) {
                                updateAccountsUi(it.data, isDone)
                            }
                            it.message?.let {msg->
                                if (msg.contains("session end", true))
                                    loginViewModel.reLogon()
                                else {
                                    _binding?.let { b -> SnackBarKt.snackBarLong(b.root, msg) }
                                }
                            }
                        }

                        else -> Unit

                    }
                }
            }
        }
    }

    private fun updateAccountsUi(data: List<Account>, isDone: (Boolean) -> Unit) {
        if (data.isNotEmpty()) {
            _binding?.apply {
                tvDue.text = data[0].dueAmount
                tvDueText.text = data[0].dueTextPeriod
                tvTerm.text = convertToReadableTerm(data[0].term)

                list.clear()
                list.addAll(data)
            }
        } else {
            _binding?.apply {
                tvDue.text = "Php 0.0"
                tvDueText.text = "Due Amount"
                tvTerm.text = "Select term"
            }
        }
        isDone(true)
    }

    private fun getTerms() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                accountViewModel.fetchTerms.collect {
                    when (it) {
                        is Resource.Loading -> {
                        }

                        is Resource.Success -> {
                            it.data?.let { it1 ->
                                termDialog?.setTerms(it1)
                            }

                        }

                        is Resource.Error -> {
                            it.message?.let { msg ->
                                _binding?.let { b -> SnackBarKt.snackBarLong(b.root, msg) }
                            }
                        }

                        else -> Unit
                    }
                }
            }
        }
    }

    private fun reLogonResponse() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                loginViewModel.login.collect {
                    when (it) {
                        is Resource.Loading -> {
                            loadingDialog?.show()
                        }

                        is Resource.Success -> {
                            selectedTerm?.let { term -> accountViewModel.fetchAccounts(term) }
                            if(isRefresh){
                                accountViewModel.fetchAccounts()
                            }
                        }

                        is Resource.Error -> {
                            it.message?.let{msg->
                                if(!msg.contains("null")){
                                    loadingDialog?.dismiss()
                                    _binding?.let { b -> SnackBarKt.snackBarLong(b.root, msg) }
                                }
                            }
                        }

                        else -> Unit
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        loadingDialog?.dismiss()
        loadingDialog = null
        termDialog = null
        _binding = null
    }

}