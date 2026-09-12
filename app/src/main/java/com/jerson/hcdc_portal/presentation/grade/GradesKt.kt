package com.jerson.hcdc_portal.presentation.grade

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
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jerson.hcdc_portal.R
import com.jerson.hcdc_portal.databinding.FragmentGradesKtBinding
import com.jerson.hcdc_portal.domain.model.Grade
import com.jerson.hcdc_portal.domain.model.Term
import com.jerson.hcdc_portal.presentation.grade.adapter.GradeAdapter
import com.jerson.hcdc_portal.presentation.grade.viewmodel.GradeViewModel
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
class GradesKt : Fragment() {
    private var _binding: FragmentGradesKtBinding? = null
    private val binding get() = _binding!!
    private val gradesViewModel: GradeViewModel by viewModels()
    private val loginViewModel: LoginViewModel by viewModels()
    private val list = mutableListOf<Grade>()
    private var loadingDialog: LoadingDialog? = null
    private var termDialog: TermSelectionDialog? = null
    private lateinit var adapter: GradeAdapter
    private var selectedTerm: Term? = null
    private var isRefresh = false

    @Inject
    lateinit var pref: AppPreference
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGradesKtBinding.inflate(inflater, container, false)
        loadingDialog = context?.let { LoadingDialog(it) }
        termDialog = context?.let { TermSelectionDialog(it) }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = GradeAdapter(list)
        binding.apply {
            recyclcerView.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            recyclcerView.adapter = adapter

            swipeRefresh.setupCustomTheme(requireContext())
            swipeRefresh.setOnRefreshListener {
                isRefresh = true
                gradesViewModel.fetchGrades()
            }
        }

        val isLoaded = pref.getBooleanPreference(Constants.KEY_IS_GRADE_LOADED)

        if (isLoaded) gradesViewModel.getGrades(pref.getIntPreference(Constants.KEY_SELECT_GRADE_TERM))
        fetchGrades {
            if (it)
                gradesViewModel.getGradeTerm()
        }
        reLogonResponse()
        getTerms()

        binding.cardTerm.setOnClickListener {
            termDialog?.showDialog { term ->
                selectedTerm = term
                gradesViewModel.hasData(term) {
                    pref.setIntPreference(Constants.KEY_SELECT_GRADE_TERM, term.id)
                    if (!it) {
                        gradesViewModel.fetchGrades(term)
                    } else {
                        gradesViewModel.getGrades(term.id)
                    }
                }
            }
        }
        binding.btnRefresh.setOnClickListener {
            binding.swipeRefresh.isRefreshing = true
            isRefresh = true
            gradesViewModel.fetchGrades()
        }

    }

    private fun fetchGrades(isDone:(Boolean)->Unit) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                gradesViewModel.fetchGrades.collect {
                    when (it) {
                        is Resource.Loading -> {
                            if (it.data.isNullOrEmpty() && list.isEmpty()) {
                                _binding?.swipeRefresh?.isRefreshing = true
                            } else {
                                updateGradesUi(it.data.orEmpty(), isDone)
                            }
                        }

                        is Resource.Success -> {
                            _binding?.swipeRefresh?.stopRefreshing()
                            loadingDialog?.dismiss()
                            updateGradesUi(it.data.orEmpty(), isDone)
                        }

                        is Resource.Error -> {
                            isDone(true)
                            _binding?.swipeRefresh?.stopRefreshing()
                            loadingDialog?.dismiss()
                            if (!it.data.isNullOrEmpty()) {
                                updateGradesUi(it.data, isDone)
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

    private fun updateGradesUi(data: List<Grade>, isDone: (Boolean) -> Unit) {
        if (data.isNotEmpty()) {
            _binding?.apply {
                tvTerm.text = convertToReadableTerm(data[0].term)
                tvUnits.text = data[0].earnedUnits
                tvGWA.text = data[0].weightedAve
                emptyGradesLayout.visibility = View.GONE
                recyclcerView.visibility = View.VISIBLE
            }
            list.clear()
            list.addAll(data)
            adapter.notifyDataSetChanged()
        } else {
            _binding?.apply {
                tvTerm.text = "Select term"
                tvUnits.text = "0"
                tvGWA.text = "0.0"
                emptyGradesLayout.visibility = View.VISIBLE
                recyclcerView.visibility = View.GONE
                list.clear()
                adapter.notifyDataSetChanged()
            }
        }
        isDone(true)
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
                            selectedTerm?.let { term -> gradesViewModel.fetchGrades(term) }
                            if(isRefresh){
                                gradesViewModel.fetchGrades()
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

    private fun getTerms() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                gradesViewModel.fetchTerms.collect {
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

    override fun onDestroyView() {
        super.onDestroyView()
        loadingDialog?.dismiss()
        loadingDialog = null
        termDialog = null
        _binding = null
    }
}