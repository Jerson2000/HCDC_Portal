package com.jerson.hcdc_portal.presentation.finance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.jerson.hcdc_portal.R
import com.jerson.hcdc_portal.databinding.FragmentFinanceKtBinding
import com.jerson.hcdc_portal.domain.model.Account
import com.jerson.hcdc_portal.domain.model.Term
import com.jerson.hcdc_portal.presentation.accounts.viewmodel.AccountViewModel
import com.jerson.hcdc_portal.presentation.evaluation.viewmodel.EvaluationViewModel
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
class FinanceKt : Fragment() {

    private var _binding: FragmentFinanceKtBinding? = null
    private val binding get() = _binding!!
    private val accountViewModel: AccountViewModel by viewModels()
    private val evaluationViewModel: EvaluationViewModel by viewModels()
    private val loginViewModel: LoginViewModel by viewModels()
    private var loadingDialog: LoadingDialog? = null
    private var termDialog: TermSelectionDialog? = null
    private var selectedTerm: Term? = null
    private val list = mutableListOf<Account>()
    private var isRefresh = false

    @Inject
    lateinit var pref: AppPreference

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFinanceKtBinding.inflate(inflater, container, false)
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
            evaluationViewModel.fetchLacking()
        }

        val isLoaded = pref.getBooleanPreference(Constants.KEY_IS_ACCOUNT_LOADED)
        if (isLoaded) {
            accountViewModel.getAccounts(pref.getIntPreference(Constants.KEY_SELECTED_ACCOUNT_TERM))
        }

        observeAccounts { isDone ->
            if (isDone) {
                accountViewModel.getAccountTerm()
            }
        }
        observeTerms()
        observeRelogin()
        observeCompliance()

        evaluationViewModel.fetchLacking()

        binding.cardTerm.setOnClickListener {
            termDialog?.showDialog { term ->
                selectedTerm = term
                binding.tvTerm.text = convertToReadableTerm(term.term)
                pref.setIntPreference(Constants.KEY_SELECTED_ACCOUNT_TERM, term.id)
                accountViewModel.hasData(term) { hasData ->
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
                val bundle = bundleOf("objectList" to ArrayList(list))
                setFragmentResult("requestKey", bundle)
                try {
                    findNavController().navigate(R.id.accountDetails, bundle)
                } catch (_: Exception) {}
            } else {
                context?.let { ctx ->
                    SnackBarKt.snackBarLong(binding.root, "No statement records found for this term.")
                }
            }
        }

        binding.btnRefresh.setOnClickListener {
            binding.swipeRefresh.isRefreshing = true
            isRefresh = true
            accountViewModel.fetchAccounts()
            evaluationViewModel.fetchLacking()
        }
    }

    private fun observeAccounts(isDone: (Boolean) -> Unit) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                accountViewModel.fetchAccounts.collect { res ->
                    when (res) {
                        is Resource.Loading -> {
                            if (res.data.isNullOrEmpty() && list.isEmpty()) {
                                _binding?.swipeRefresh?.isRefreshing = true
                            } else {
                                updateAccountsUi(res.data.orEmpty(), isDone)
                            }
                        }
                        is Resource.Success -> {
                            _binding?.swipeRefresh?.stopRefreshing()
                            loadingDialog?.dismiss()
                            updateAccountsUi(res.data.orEmpty(), isDone)
                        }
                        is Resource.Error -> {
                            isDone(true)
                            _binding?.swipeRefresh?.stopRefreshing()
                            loadingDialog?.dismiss()
                            if (!res.data.isNullOrEmpty()) {
                                updateAccountsUi(res.data, isDone)
                            } else {
                                _binding?.apply {
                                    selectedTerm?.let { t -> tvTerm.text = convertToReadableTerm(t.term) }
                                    tvDue.text = "Php 0.00"
                                    tvDueText.text = "Outstanding Balance"
                                    list.clear()
                                }
                            }
                            res.message?.let { msg ->
                                if (msg.contains("session end", true)) {
                                    loginViewModel.reLogon()
                                } else {
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
                tvDue.text = data[0].dueAmount ?: "Php 0.00"
                tvDueText.text = data[0].dueTextPeriod ?: "Due Amount"
                tvTerm.text = convertToReadableTerm(data[0].term)

                list.clear()
                list.addAll(data)
            }
        } else {
            _binding?.apply {
                tvDue.text = "Php 0.00"
                tvDueText.text = "Outstanding Balance"
                tvTerm.text = "Select term"
            }
        }
        isDone(true)
    }

    private fun observeCompliance() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                evaluationViewModel.fetchLacking.collect { res ->
                    when (res) {
                        is Resource.Success -> {
                            val data = res.data.orEmpty()
                            val ctx = context ?: return@collect
                            _binding?.apply {
                                layoutLackingItems.removeAllViews()
                                if (data.lowercase().contains("complied")) {
                                    tvComplianceBadge.text = "Complied"
                                    tvComplianceBadge.setTextColor(ContextCompat.getColor(ctx, R.color.badge_passed_text))
                                    tvComplianceDesc.text = "All university credentials and registrar requirements are fully satisfied."
                                    layoutLackingItems.visibility = View.GONE
                                } else if (data.isNotBlank()) {
                                    tvComplianceBadge.text = "Pending"
                                    tvComplianceBadge.setTextColor(ContextCompat.getColor(ctx, R.color.badge_pending_text))
                                    tvComplianceDesc.text = "The following credentials require compliance with the Registrar:"
                                    layoutLackingItems.visibility = View.VISIBLE

                                    val items = data.split(":").filter { it.isNotBlank() }
                                    for (item in items) {
                                        val tv = TextView(ctx)
                                        tv.text = "• ${item.trim()}"
                                        tv.setPadding(0, 6, 0, 6)
                                        androidx.core.widget.TextViewCompat.setTextAppearance(tv, com.google.android.material.R.style.TextAppearance_Material3_BodyMedium)
                                        layoutLackingItems.addView(tv)
                                    }
                                }
                            }
                        }
                        else -> Unit
                    }
                }
            }
        }
    }

    private fun observeTerms() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                accountViewModel.fetchTerms.collect { res ->
                    if (res is Resource.Success && res.data != null) {
                        termDialog?.setTerms(res.data)
                    }
                }
            }
        }
    }

    private fun observeRelogin() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                loginViewModel.login.collect { res ->
                    if (res is Resource.Success) {
                        selectedTerm?.let { term -> accountViewModel.fetchAccounts(term) }
                        if (isRefresh) {
                            accountViewModel.fetchAccounts()
                        }
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
