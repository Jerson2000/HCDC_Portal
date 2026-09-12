package com.jerson.hcdc_portal.presentation.subjects

import android.content.Intent
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
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jerson.hcdc_portal.R
import com.jerson.hcdc_portal.databinding.FragmentSubjectsKtBinding
import com.jerson.hcdc_portal.domain.model.Schedule
import com.jerson.hcdc_portal.presentation.dashboard.viewmodel.DashboardViewModel
import com.jerson.hcdc_portal.presentation.login.viewmodel.LoginViewModel
import com.jerson.hcdc_portal.presentation.subjects.adapter.SubjectAdapter
import com.jerson.hcdc_portal.util.LoadingDialog
import com.jerson.hcdc_portal.util.ScheduleHelper
import com.jerson.hcdc_portal.util.Resource
import com.jerson.hcdc_portal.util.SnackBarKt
import com.jerson.hcdc_portal.util.setupCustomTheme
import com.jerson.hcdc_portal.util.stopRefreshing
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SubjectKt : Fragment() {
    private var _binding: FragmentSubjectsKtBinding? = null
    private val binding get() = _binding!!
    private val dashboardViewModel: DashboardViewModel by viewModels()
    private val loginViewModel: LoginViewModel by viewModels()
    private lateinit var adapter: SubjectAdapter
    private val list = mutableListOf<Schedule>()
    private var loadingDialog: LoadingDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSubjectsKtBinding.inflate(inflater, container, false)
        return binding.root
    }

    private val allSubjectsList = mutableListOf<Schedule>()
    private var currentDayFilter = "All"
    private var currentSearchQuery = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingDialog = context?.let { cnt -> LoadingDialog(cnt) }
        adapter = SubjectAdapter(list) {
            startActivity(Intent(context, SubjectDetailsKt::class.java).putExtra("subject", it))
        }
        binding.apply {
            recyclerView.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            recyclerView.adapter = adapter

            swipeRefresh.setupCustomTheme(requireContext())
            swipeRefresh.setOnRefreshListener {
                dashboardViewModel.fetchSchedules()
            }

            btnRefresh.setOnClickListener {
                swipeRefresh.isRefreshing = true
                dashboardViewModel.fetchSchedules()
            }

            searchET.addTextChangedListener(object : android.text.TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    currentSearchQuery = s?.toString()?.trim().orEmpty()
                    btnClearSearch.visibility = if (currentSearchQuery.isNotEmpty()) View.VISIBLE else View.GONE
                    applyFilter()
                }
                override fun afterTextChanged(s: android.text.Editable?) {}
            })

            btnClearSearch.setOnClickListener {
                searchET.text?.clear()
            }

            chipGroupDays.setOnCheckedStateChangeListener { _, checkedIds ->
                currentDayFilter = when (checkedIds.firstOrNull()) {
                    R.id.chipMon -> "M"
                    R.id.chipTue -> "T"
                    R.id.chipWed -> "W"
                    R.id.chipThu -> "Th"
                    R.id.chipFri -> "F"
                    R.id.chipSat -> "S"
                    else -> "All"
                }
                applyFilter()
            }
        }
        dashboardViewModel.getSchedules()
        listenerFetch()
        reLogonResponse()
    }

    private fun applyFilter() {
        val filtered = allSubjectsList.filter { schedule ->
            val matchesDay = ScheduleHelper.matchesDay(schedule.days, currentDayFilter)

            val matchesSearch = if (currentSearchQuery.isEmpty()) true
            else {
                (schedule.subjectCode?.contains(currentSearchQuery, ignoreCase = true) == true) ||
                        schedule.description.contains(currentSearchQuery, ignoreCase = true)
            }
            matchesDay && matchesSearch
        }

        list.clear()
        list.addAll(filtered)
        adapter.notifyDataSetChanged()

        _binding?.apply {
            if (filtered.isEmpty()) {
                emptyStateLayout.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                emptyStateLayout.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
            }
        }
    }

    private fun listenerFetch() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                dashboardViewModel.fetchSchedules.collect {
                    when (it) {
                        is Resource.Loading -> {
                            if (allSubjectsList.isEmpty()) {
                                _binding?.swipeRefresh?.isRefreshing = true
                            }
                        }

                        is Resource.Success -> {
                            _binding?.swipeRefresh?.stopRefreshing()
                            loadingDialog?.dismiss()
                            it.data?.let { it1 ->
                                val uniqueSchedules = it1.distinctBy { x -> x.subjectCode }
                                allSubjectsList.clear()
                                allSubjectsList.addAll(uniqueSchedules)
                                applyFilter()
                            }
                        }

                        is Resource.Error -> {
                            _binding?.swipeRefresh?.stopRefreshing()
                            loadingDialog?.dismiss()
                            it.message?.let { msg ->
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

    private fun reLogonResponse() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                loginViewModel.login.collect {
                    when (it) {
                        is Resource.Loading -> {
                            _binding?.swipeRefresh?.isRefreshing = true
                        }

                        is Resource.Success -> {
                            dashboardViewModel.fetchSchedules()
                        }

                        is Resource.Error -> {
                            it.message?.let { msg ->
                                if (!msg.contains("null")) {
                                    _binding?.swipeRefresh?.isRefreshing = false
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
        _binding = null
    }
}