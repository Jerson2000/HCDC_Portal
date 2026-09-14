package com.jerson.hcdc_portal.presentation.prospectus

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jerson.hcdc_portal.R
import com.jerson.hcdc_portal.databinding.FragmentProspectusBinding
import com.jerson.hcdc_portal.databinding.ItemProspectusSemesterHeaderBinding
import com.jerson.hcdc_portal.databinding.ItemProspectusSubjectBinding
import com.jerson.hcdc_portal.databinding.ItemProspectusYearHeaderBinding
import com.jerson.hcdc_portal.domain.model.Prospectus
import com.jerson.hcdc_portal.domain.model.ProspectusSubject
import com.jerson.hcdc_portal.presentation.prospectus.viewmodel.ProspectusViewModel
import com.jerson.hcdc_portal.presentation.prospectus.viewmodel.SyncStatus
import com.jerson.hcdc_portal.util.Resource
import com.jerson.hcdc_portal.util.setupCustomTheme
import com.jerson.hcdc_portal.util.stopRefreshing
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class ProspectusListItem {
    data class YearHeader(val yearTitle: String) : ProspectusListItem()
    data class SemesterHeader(val semesterTitle: String, val totalUnitsDisplay: String) : ProspectusListItem()
    data class SubjectItem(val subject: ProspectusSubject) : ProspectusListItem()
}

@AndroidEntryPoint
class ProspectusFragment : Fragment() {

    private var _binding: FragmentProspectusBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProspectusViewModel by viewModels()

    private val items = mutableListOf<ProspectusListItem>()
    private lateinit var adapter: ProspectusAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProspectusBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.swipeRefresh.setupCustomTheme(requireContext())
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.resolveAndLoadProspectus(forceRefresh = true)
        }

        binding.btnRetry.setOnClickListener {
            viewModel.resolveAndLoadProspectus(forceRefresh = true)
        }

        adapter = ProspectusAdapter(items)
        binding.rvProspectus.layoutManager = LinearLayoutManager(requireContext())
        binding.rvProspectus.adapter = adapter

        observeViewModel()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observe prospectus data
                launch {
                    viewModel.prospectusState.collect { resource ->
                        when (resource) {
                            is Resource.Loading -> {
                                if (items.isEmpty()) {
                                    binding.progressBar.visibility = View.VISIBLE
                                    binding.layoutEmpty.visibility = View.GONE
                                }
                            }
                            is Resource.Success -> {
                                binding.progressBar.visibility = View.GONE
                                binding.swipeRefresh.stopRefreshing()
                                val prospectus = resource.data
                                if (prospectus != null) {
                                    bindProspectus(prospectus)
                                } else if (items.isEmpty()) {
                                    binding.layoutEmpty.visibility = View.VISIBLE
                                }
                            }
                            is Resource.Error -> {
                                binding.progressBar.visibility = View.GONE
                                binding.swipeRefresh.stopRefreshing()
                                if (items.isEmpty()) {
                                    binding.layoutEmpty.visibility = View.VISIBLE
                                    val friendly = com.jerson.hcdc_portal.util.ErrorMessageHelper.format(resource.message)
                                    binding.tvEmptyDesc.text = friendly
                                }
                            }
                            else -> Unit
                        }
                    }
                }

                // Observe sync status badge
                launch {
                    viewModel.syncStatus.collect { status ->
                        updateSyncBadge(status)
                    }
                }
            }
        }
    }

    private fun bindProspectus(prospectus: Prospectus) {
        val meta = prospectus.metadata

        binding.tvProgramCodeBadge.text = meta.programCode.ifBlank { "PROGRAM" }
        binding.tvProgramFullName.text = meta.programName.ifBlank { "Curriculum Prospectus" }
        binding.tvRevision.text = meta.revision.ifBlank { "Official Curriculum" }
        binding.tvEffectiveAy.text = if (meta.effectiveAy.isNotBlank()) "Effective AY: ${meta.effectiveAy}" else ""
        binding.tvTotalUnits.text = "Total Units: ${prospectus.totalCurriculumUnits}"

        items.clear()
        for (year in prospectus.yearLevels) {
            items.add(ProspectusListItem.YearHeader(year.yearLevelName))
            for (sem in year.semesters) {
                val totalDisplay = if (sem.totalUnitsDisplay.isNotBlank()) "${sem.totalUnitsDisplay} Units" else ""
                items.add(ProspectusListItem.SemesterHeader(sem.semesterName, totalDisplay))
                for (subj in sem.subjects) {
                    items.add(ProspectusListItem.SubjectItem(subj))
                }
            }
        }

        adapter.notifyDataSetChanged()
        binding.layoutEmpty.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
        binding.rvProspectus.visibility = if (items.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun updateSyncBadge(status: SyncStatus) {
        when (status) {
            is SyncStatus.Syncing -> {
                binding.tvSyncBadge.text = "Syncing..."
                binding.tvSyncBadge.setTextColor(ContextCompat.getColor(requireContext(), R.color.hcdc_primary))
            }
            is SyncStatus.Synced -> {
                val timeStr = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(status.timestamp))
                binding.tvSyncBadge.text = "Synced • $timeStr"
                binding.tvSyncBadge.setTextColor(ContextCompat.getColor(requireContext(), R.color.badge_passed_text))
            }
            is SyncStatus.Offline -> {
                binding.tvSyncBadge.text = "Offline / Cached"
                binding.tvSyncBadge.setTextColor(ContextCompat.getColor(requireContext(), R.color.badge_failed_text))
            }
            SyncStatus.Idle -> Unit
        }
    }


    inner class ProspectusAdapter(
        private val list: List<ProspectusListItem>
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private val TYPE_YEAR = 0
        private val TYPE_SEMESTER = 1
        private val TYPE_SUBJECT = 2

        override fun getItemViewType(position: Int): Int {
            return when (list[position]) {
                is ProspectusListItem.YearHeader -> TYPE_YEAR
                is ProspectusListItem.SemesterHeader -> TYPE_SEMESTER
                is ProspectusListItem.SubjectItem -> TYPE_SUBJECT
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return when (viewType) {
                TYPE_YEAR -> {
                    val b = ItemProspectusYearHeaderBinding.inflate(inflater, parent, false)
                    YearViewHolder(b)
                }
                TYPE_SEMESTER -> {
                    val b = ItemProspectusSemesterHeaderBinding.inflate(inflater, parent, false)
                    SemesterViewHolder(b)
                }
                else -> {
                    val b = ItemProspectusSubjectBinding.inflate(inflater, parent, false)
                    SubjectViewHolder(b)
                }
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (val item = list[position]) {
                is ProspectusListItem.YearHeader -> (holder as YearViewHolder).bind(item)
                is ProspectusListItem.SemesterHeader -> (holder as SemesterViewHolder).bind(item)
                is ProspectusListItem.SubjectItem -> (holder as SubjectViewHolder).bind(item)
            }
        }

        override fun getItemCount(): Int = list.size

        inner class YearViewHolder(private val b: ItemProspectusYearHeaderBinding) :
            RecyclerView.ViewHolder(b.root) {
            fun bind(item: ProspectusListItem.YearHeader) {
                b.tvYearLevelTitle.text = item.yearTitle.uppercase(Locale.ROOT)
            }
        }

        inner class SemesterViewHolder(private val b: ItemProspectusSemesterHeaderBinding) :
            RecyclerView.ViewHolder(b.root) {
            fun bind(item: ProspectusListItem.SemesterHeader) {
                b.tvSemesterTitle.text = item.semesterTitle
                b.tvSemesterTotalUnits.text = item.totalUnitsDisplay
                b.tvSemesterTotalUnits.visibility = if (item.totalUnitsDisplay.isNotBlank()) View.VISIBLE else View.GONE
            }
        }

        inner class SubjectViewHolder(private val b: ItemProspectusSubjectBinding) :
            RecyclerView.ViewHolder(b.root) {
            fun bind(item: ProspectusListItem.SubjectItem) {
                val s = item.subject
                b.tvSubjectCode.text = s.code
                b.tvSubjectName.text = s.name
                b.tvUnits.text = "${s.units} Units"
                b.tvLabBadge.visibility = if (s.isLab) View.VISIBLE else View.GONE
                b.tvPrerequisites.text = s.preRequisites.ifBlank { "None" }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
