package com.jerson.hcdc_portal.presentation.subjects_offered

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jerson.hcdc_portal.databinding.FragmentSubjectOfferedEmbeddedBinding
import com.jerson.hcdc_portal.domain.model.SubjectOffered
import com.jerson.hcdc_portal.presentation.login.viewmodel.LoginViewModel
import com.jerson.hcdc_portal.presentation.subjects_offered.adapter.SubjectOfferedAdapter
import com.jerson.hcdc_portal.presentation.subjects_offered.viewmodel.SubjectOfferedViewModel
import com.jerson.hcdc_portal.util.Resource
import com.jerson.hcdc_portal.util.SnackBarKt
import com.jerson.hcdc_portal.util.hideKeyboard
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SubjectOfferedFragment : Fragment() {

    private var _binding: FragmentSubjectOfferedEmbeddedBinding? = null
    private val binding get() = _binding!!
    private val subjectOfferedViewModel: SubjectOfferedViewModel by viewModels()
    private val loginViewModel: LoginViewModel by viewModels()
    private lateinit var adapter: SubjectOfferedAdapter
    private val list = mutableListOf<SubjectOffered>()
    private var subjectOfferedPage = 1
    private var totalPages = 148
    private var isSearch = false
    private var isLoadingPagination = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSubjectOfferedEmbeddedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = SubjectOfferedAdapter(list) {
            showOfferDetail(it)
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = adapter

        binding.searchET.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.searchET.text.toString().trim()
                requireActivity().hideKeyboard()
                if (query.isNotEmpty()) {
                    isSearch = true
                    subjectOfferedViewModel.searchSubjectOffered(query)
                }
                true
            } else false
        }

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (!recyclerView.canScrollVertically(1)) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount && firstVisibleItemPosition >= 0) {
                        if (subjectOfferedPage <= totalPages && !isSearch && !isLoadingPagination) {
                            isLoadingPagination = true
                            subjectOfferedPage++
                            subjectOfferedViewModel.fetchSubjectOffered(subjectOfferedPage)
                        }
                    }
                }
            }
        })

        subjectOfferedViewModel.fetchSubjectOffered(subjectOfferedPage)
        observeSubjectOffered()
        observeRelogin()
    }

    private fun observeSubjectOffered() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                subjectOfferedViewModel.fetchSubjectOffered.collect { res ->
                    when (res) {
                        is Resource.Loading -> {
                            if (list.isEmpty()) {
                                _binding?.progressBar?.visibility = View.VISIBLE
                            }
                        }
                        is Resource.Success -> {
                            isLoadingPagination = false
                            _binding?.progressBar?.visibility = View.GONE
                            res.data?.let { data ->
                                if (isSearch) list.clear()
                                list.addAll(data)
                                adapter.notifyDataSetChanged()
                            }
                        }
                        is Resource.Error -> {
                            isLoadingPagination = false
                            _binding?.progressBar?.visibility = View.GONE
                            val msg = res.message.orEmpty()
                            if (msg.contains("session end", true)) {
                                loginViewModel.reLogon()
                            } else if (msg.isNotBlank()) {
                                _binding?.let { b -> SnackBarKt.snackBarLong(b.root, msg) }
                            }
                        }
                        else -> Unit
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
                        subjectOfferedViewModel.fetchSubjectOffered(subjectOfferedPage)
                    }
                }
            }
        }
    }

    private fun showOfferDetail(item: SubjectOffered) {
        val details =
            "Offer No.: ${item.so_no}\n" +
                    "Description: ${item.description}\n" +
                    "Units: ${item.units}\n" +
                    "Max Students: ${item.maximum}\n" +
                    "Enrolled: ${item.enrolled}\n" +
                    "Remaining Slots: ${item.remaining_slot}\n" +
                    "Term: ${item.sysem}\n" +
                    "Course: ${item.course}"

        context?.let {
            MaterialAlertDialogBuilder(it)
                .setTitle(item.subject_code)
                .setMessage(details)
                .setPositiveButton("Close", null)
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
