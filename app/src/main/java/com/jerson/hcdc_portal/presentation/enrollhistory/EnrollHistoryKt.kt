package com.jerson.hcdc_portal.presentation.enrollhistory

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
import com.jerson.hcdc_portal.databinding.FragmentEnrollmentHistoryKtBinding
import com.jerson.hcdc_portal.presentation.enrollhistory.viewmodel.EnrollHistoryViewModel
import com.jerson.hcdc_portal.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EnrollHistoryKt : Fragment() {
    private lateinit var binding: FragmentEnrollmentHistoryKtBinding
    private val enrollHistoryViewModel: EnrollHistoryViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEnrollmentHistoryKtBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enrollHistoryViewModel.fetchEnrollHistory()
        fetchEnrollHistory()
    }

    private fun fetchEnrollHistory() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                enrollHistoryViewModel.fetchEnrollHistory.collect {
                    when (it) {
                        is Resource.Loading -> {

                        }

                        is Resource.Success -> {
                            Log.e("HUHU", "fetchEnrollHistory: ${it.data!!.size}")
                        }

                        is Resource.Error -> {
                            Log.e("HUHU", "fetchEnrollHistory: ${it.message}")
                        }

                        else -> Unit
                    }
                }
            }
        }
    }
}