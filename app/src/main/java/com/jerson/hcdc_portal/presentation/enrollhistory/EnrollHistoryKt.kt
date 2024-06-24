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
import androidx.recyclerview.widget.LinearLayoutManager
import com.jerson.hcdc_portal.databinding.FragmentEnrollmentHistoryKtBinding
import com.jerson.hcdc_portal.domain.model.EnrollHistory
import com.jerson.hcdc_portal.presentation.enrollhistory.adapter.EnrollHistoryAdapter
import com.jerson.hcdc_portal.presentation.enrollhistory.viewmodel.EnrollHistoryViewModel
import com.jerson.hcdc_portal.util.AppPreference
import com.jerson.hcdc_portal.util.Constants
import com.jerson.hcdc_portal.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class EnrollHistoryKt : Fragment() {
    private lateinit var binding: FragmentEnrollmentHistoryKtBinding
    private val enrollHistoryViewModel: EnrollHistoryViewModel by viewModels()
    private lateinit var adapter: EnrollHistoryAdapter
    private val list = mutableListOf<EnrollHistory>()
    @Inject
    lateinit var pref: AppPreference
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
        val isLoaded = pref.getBooleanPreference(Constants.KEY_IS_ENROLL_HISTORY_LOADED)

        adapter = EnrollHistoryAdapter(list)
        binding.apply {
            recyclerView.adapter = adapter
            recyclerView.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }

        if (isLoaded)
            enrollHistoryViewModel.getEnrollHistory()

        fetchEnrollHistory()
    }

    private fun fetchEnrollHistory() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                enrollHistoryViewModel.fetchEnrollHistory.collect {
                    when (it) {
                        is Resource.Loading -> {
                            Log.e("HUHU", "fetchEnrollHistory: Loading...")
                        }

                        is Resource.Success -> {
                            if (it.data!!.isNotEmpty()) {
                                binding.apply {
                                    tvTerm.text = it.data[0].term
                                }
                                list.addAll(it.data)
                                adapter.notifyDataSetChanged()
                            } else {
                                binding.apply {
                                    tvTerm.text = "Select term"
                                }
                            }
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