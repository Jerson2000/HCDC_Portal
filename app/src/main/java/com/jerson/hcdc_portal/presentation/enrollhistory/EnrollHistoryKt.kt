package com.jerson.hcdc_portal.presentation.enrollhistory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jerson.hcdc_portal.R
import com.jerson.hcdc_portal.databinding.FragmentEnrollmentHistoryKtBinding
import com.jerson.hcdc_portal.domain.model.EnrollHistory
import com.jerson.hcdc_portal.domain.model.Term
import com.jerson.hcdc_portal.presentation.enrollhistory.adapter.EnrollHistoryAdapter
import com.jerson.hcdc_portal.presentation.enrollhistory.viewmodel.EnrollHistoryViewModel
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
class EnrollHistoryKt : Fragment() {
    private var _binding: FragmentEnrollmentHistoryKtBinding? = null
    private val binding get() = _binding!!
    private val enrollHistoryViewModel: EnrollHistoryViewModel by viewModels()
    private val loginViewModel: LoginViewModel by viewModels()
    private lateinit var adapter: EnrollHistoryAdapter
    private val list = mutableListOf<EnrollHistory>()
    private var termDialog: TermSelectionDialog? = null
    private var loadingDialog: LoadingDialog? = null
    private var selectedTerm: Term?=null
    private var isRefresh = false

    @Inject
    lateinit var pref: AppPreference
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEnrollmentHistoryKtBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingDialog = context?.let { LoadingDialog(it) }
        termDialog = context?.let { TermSelectionDialog(it) }

        adapter = EnrollHistoryAdapter(list)
        binding.apply {
            recyclerView.adapter = adapter
            recyclerView.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

            swipeRefresh.setupCustomTheme(requireContext())
            swipeRefresh.setOnRefreshListener {
                isRefresh = true
                enrollHistoryViewModel.fetchEnrollHistory()
            }
        }

        val isLoaded = pref.getBooleanPreference(Constants.KEY_IS_ENROLL_HISTORY_LOADED)
        if (isLoaded)
            enrollHistoryViewModel.getEnrollHistory(pref.getIntPreference(Constants.KEY_SELECTED_ENROLL_HISTORY_TERM))
        fetchEnrollHistory {
            if (it) {
                enrollHistoryViewModel.getEnrollHistoryTerm()
            }
        }

        getTerms()
        reLogonResponse()
        binding.cardTerm.setOnClickListener {
            termDialog?.showDialog { term ->
                selectedTerm = term
                binding.tvTerm.text = convertToReadableTerm(term.term)
                pref.setIntPreference(Constants.KEY_SELECTED_ENROLL_HISTORY_TERM, term.id)
                enrollHistoryViewModel.hasData(term) { hasData ->
                    if (!hasData) {
                        enrollHistoryViewModel.fetchEnrollHistory(term)
                    } else {
                        enrollHistoryViewModel.getEnrollHistory(term.id)
                    }
                }
            }
        }
        binding.btnRefresh.setOnClickListener {
            binding.swipeRefresh.isRefreshing = true
            isRefresh = true
            enrollHistoryViewModel.fetchEnrollHistory()
        }
    }

    private fun fetchEnrollHistory(isDone : (Boolean)-> Unit) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                enrollHistoryViewModel.fetchEnrollHistory.collect {
                    when (it) {
                        is Resource.Loading -> {
                            if (it.data.isNullOrEmpty() && list.isEmpty()) {
                                _binding?.swipeRefresh?.isRefreshing = true
                            } else {
                                updateEnrollHistoryUi(it.data.orEmpty(), isDone)
                            }
                        }

                        is Resource.Success -> {
                            _binding?.swipeRefresh?.stopRefreshing()
                            loadingDialog?.dismiss()
                            updateEnrollHistoryUi(it.data.orEmpty(), isDone)
                        }

                        is Resource.Error -> {
                            isDone(true)
                            _binding?.swipeRefresh?.stopRefreshing()
                            loadingDialog?.dismiss()
                            if (!it.data.isNullOrEmpty()) {
                                updateEnrollHistoryUi(it.data, isDone)
                            } else {
                                _binding?.apply {
                                    selectedTerm?.let { t -> tvTerm.text = convertToReadableTerm(t.term) }
                                    emptyHistoryLayout.visibility = View.VISIBLE
                                    recyclerView.visibility = View.GONE
                                    list.clear()
                                    adapter.notifyDataSetChanged()
                                }
                            }
                            it.message?.let { msg ->
                                if (msg.contains("session end", true)) {
                                    loginViewModel.reLogon()
                                } else {
                                    val friendly = com.jerson.hcdc_portal.util.ErrorMessageHelper.format(msg)
                                    _binding?.let { b -> SnackBarKt.snackBarLong(b.root, friendly) }
                                }
                            }
                        }

                        else -> Unit
                    }
                }
            }
        }
    }

    private fun updateEnrollHistoryUi(data: List<EnrollHistory>, isDone: (Boolean) -> Unit) {
        if (data.isNotEmpty()) {
            _binding?.apply {
                tvTerm.text = convertToReadableTerm(data[0].term)
                emptyHistoryLayout.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
            }
            list.clear()
            list.addAll(data)
            adapter.notifyDataSetChanged()
        } else {
            _binding?.apply {
                tvTerm.text = "Select term"
                emptyHistoryLayout.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
                list.clear()
                adapter.notifyDataSetChanged()
            }
        }
        isDone(true)
    }


    private fun getTerms() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                enrollHistoryViewModel.fetchTerms.collect {
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
                                val friendly = com.jerson.hcdc_portal.util.ErrorMessageHelper.format(msg)
                                _binding?.let { b -> SnackBarKt.snackBarLong(b.root, friendly) }
                            }
                        }

                        else -> Unit
                    }
                }
            }
        }
    }

    private fun reLogonResponse(){
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                loginViewModel.login.collect{
                    when (it) {
                        is Resource.Loading -> {
                            loadingDialog?.show()
                        }

                        is Resource.Success -> {
                            selectedTerm?.let { term -> enrollHistoryViewModel.fetchEnrollHistory(term) }
                            if(isRefresh){
                                enrollHistoryViewModel.fetchEnrollHistory()
                            }
                        }

                        is Resource.Error -> {
                            it.message?.let{msg->
                                if(!msg.contains("null")){
                                    loadingDialog?.dismiss()
                                    val friendly = com.jerson.hcdc_portal.util.ErrorMessageHelper.format(msg)
                                    _binding?.let { b -> SnackBarKt.snackBarLong(b.root, friendly) }
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