package com.jerson.hcdc_portal.presentation.dashboard

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
import com.jerson.hcdc_portal.databinding.FragmentDashboardKtBinding
import com.jerson.hcdc_portal.presentation.dashboard.viewmodel.DashboardViewModel
import com.jerson.hcdc_portal.util.AppPreference
import com.jerson.hcdc_portal.util.Constants
import com.jerson.hcdc_portal.util.Constants.KEY_STUDENTS_UNITS
import com.jerson.hcdc_portal.util.LoadingDialog
import com.jerson.hcdc_portal.util.Resource
import com.jerson.hcdc_portal.util.SnackBarKt
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class DashboardKt : Fragment() {
    private lateinit var binding: FragmentDashboardKtBinding
    private val dashboardViewModel: DashboardViewModel by viewModels()
    private var loadingDialog: LoadingDialog? = null

    @Inject
    lateinit var pref:AppPreference
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDashboardKtBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dashboardViewModel.getSchedules()
        listenerFetch()
        loadingDialog = context?.let { LoadingDialog(it) }
        loadingDialog?.show()
    }

    private fun listenerFetch() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                dashboardViewModel.fetchSchedules.collect {
                    when (it) {
                        is Resource.Loading -> {
                            Log.e("HUHU", "listenerFetch: Loading...", )
                        }

                        is Resource.Success -> {
                            binding.apply{
                                totalSubTV.text = it.data!!.size.toString()
                                unitsTV.text = pref.getStringPreference(KEY_STUDENTS_UNITS)
                            }
                        }

                        is Resource.Error -> {
                            it.message?.let { it1 -> SnackBarKt.snackBarLong(binding!!.root, it1) }
                        }

                        else -> Unit
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }


}
