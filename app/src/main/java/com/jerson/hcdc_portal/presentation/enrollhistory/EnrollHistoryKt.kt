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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class EnrollHistoryKt : Fragment() {
    private lateinit var binding: FragmentEnrollmentHistoryKtBinding
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
        binding = FragmentEnrollmentHistoryKtBinding.inflate(inflater)
        loadingDialog = context?.let { LoadingDialog(it) }
        termDialog = context?.let {TermSelectionDialog(it) }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.bgImage.load(R.drawable.reunite){
            size(300,300)
        }

        val isLoaded = pref.getBooleanPreference(Constants.KEY_IS_ENROLL_HISTORY_LOADED)

        adapter = EnrollHistoryAdapter(list)
        binding.apply {
            recyclerView.adapter = adapter
            recyclerView.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }

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
                enrollHistoryViewModel.hasData(term) {
                    if (!it) {
                        enrollHistoryViewModel.fetchEnrollHistory(term)
                    }else{
                        pref.setIntPreference(Constants.KEY_SELECTED_ENROLL_HISTORY_TERM,term.id)
                        enrollHistoryViewModel.getEnrollHistory(term.id)
                    }
                }
            }
        }
        binding.btnRefresh.setOnClickListener{
            context?.let { cntxt ->
                MaterialAlertDialogBuilder(cntxt)
                    .setTitle("Refresh")
                    .setMessage("Are you sure you want to refresh?")
                    .setPositiveButton("Yes"){dialog,_->
                        isRefresh = true
                        enrollHistoryViewModel.fetchEnrollHistory()
                        dialog.dismiss()
                    }
                    .setNegativeButton("No"){dialog,_->
                        dialog.dismiss()
                    }.show()

            }
        }
    }

    private fun fetchEnrollHistory(isDone : (Boolean)-> Unit) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                enrollHistoryViewModel.fetchEnrollHistory.collect {
                    when (it) {
                        is Resource.Loading -> {
                            loadingDialog!!.show()
                        }

                        is Resource.Success -> {
                            loadingDialog!!.dismiss()
                            if (it.data!!.isNotEmpty()) {
                                binding.apply {
                                    tvTerm.text = convertToReadableTerm(it.data[0].term)
                                }
                                list.clear()
                                list.addAll(it.data)
                                adapter.notifyDataSetChanged()
                            } else {
                                binding.apply {
                                    tvTerm.text = "Select term"
                                    list.clear()
                                    adapter.notifyDataSetChanged()
                                }
                            }
                            isDone(true)
                        }

                        is Resource.Error -> {
                            isDone(true)
                            it.message?.let {msg->
                                if (msg.contains("session end", true))
                                    loginViewModel.reLogon()
                                else {
                                    loadingDialog?.dismiss()
                                    SnackBarKt.snackBarLong(binding.root, it.message)
                                }
                            }
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
                            it.message?.let { msg -> SnackBarKt.snackBarLong(binding.root, msg) }
                        }

                        else -> Unit
                    }
                }
            }
        }
    }

    private fun reLogonResponse(){
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                loginViewModel.login.collect{
                    when (it) {
                        is Resource.Loading -> {
                            loadingDialog!!.show()
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
                                    SnackBarKt.snackBarLong(binding.root, msg)
                                }
                            }
                        }

                        else -> Unit
                    }
                }
            }
        }
    }
}