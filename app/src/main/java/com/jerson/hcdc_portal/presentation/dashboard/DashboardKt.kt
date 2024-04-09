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
import com.jerson.hcdc_portal.R
import com.jerson.hcdc_portal.databinding.FragmentDashboardKtBinding
import com.jerson.hcdc_portal.presentation.dashboard.viewmodel.DashboardViewModel
import com.jerson.hcdc_portal.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import javax.inject.Inject

@AndroidEntryPoint
class DashboardKt : Fragment(R.layout.fragment_dashboard_kt) {
    private lateinit var binding: FragmentDashboardKtBinding

    @Inject
    lateinit var client: OkHttpClient
    private val dashboardViewModel: DashboardViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDashboardKtBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        /*dashboardViewModel.fetchSchedules()*/
        dashboardViewModel.getSchedules()
        listenerFetch()
    }

    private fun listenerFetch() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                dashboardViewModel.fetchSchedules.collect {
                    when (it) {
                        is Resource.Loading -> {

                        }

                        is Resource.Success -> {
                            Log.e("HUHU", "listenerFetch: ${it.data!!.size}", )
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
