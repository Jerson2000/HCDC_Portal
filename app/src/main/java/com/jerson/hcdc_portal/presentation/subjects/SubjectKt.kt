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
import com.jerson.hcdc_portal.util.Resource
import com.jerson.hcdc_portal.util.SnackBarKt
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SubjectKt : Fragment() {
    private lateinit var binding: FragmentSubjectsKtBinding
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
        binding = FragmentSubjectsKtBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingDialog = context?.let { cnt -> LoadingDialog(cnt) }
        adapter = SubjectAdapter(list){
            startActivity(Intent(context,SubjectDetailsKt::class.java).putExtra("subject",it))
        }
        binding.apply {
            recyclerView.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            recyclerView.adapter = adapter
            bgImage.load(R.drawable.studying_2){
                size(300,300)
            }
        }
        dashboardViewModel.getSchedules()
        listenerFetch()
        reLogonResponse()

        binding.btnRefresh.setOnClickListener{
            context?.let { cntxt ->
                MaterialAlertDialogBuilder(cntxt)
                    .setTitle("Refresh")
                    .setMessage("Are you sure you want to refresh?")
                    .setPositiveButton("Yes"){dialog,_->
                        dashboardViewModel.fetchSchedules()
                        dialog.dismiss()
                    }
                    .setNegativeButton("No"){dialog,_->
                        dialog.dismiss()
                    }.show()

            }

        }
    }

    private fun listenerFetch() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                dashboardViewModel.fetchSchedules.collect {
                    when (it) {
                        is Resource.Loading -> {
                            loadingDialog!!.show()
                        }

                        is Resource.Success -> {
                            loadingDialog!!.dismiss()
                            it.data?.let { it1 ->
                                list.clear()
                                list.addAll(it1)
                                list.distinctBy { x-> x.subjectCode }
                            }
                            adapter.notifyDataSetChanged()
                        }

                        is Resource.Error -> {
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

    private fun reLogonResponse() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                loginViewModel.login.collect {
                    when (it) {
                        is Resource.Loading -> {
                            loadingDialog!!.show()
                        }

                        is Resource.Success -> {
                            dashboardViewModel.fetchSchedules()
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