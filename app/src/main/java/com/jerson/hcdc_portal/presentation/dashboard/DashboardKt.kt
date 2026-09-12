package com.jerson.hcdc_portal.presentation.dashboard

import android.content.Intent
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
import coil.ImageLoader
import coil.load
import com.jerson.hcdc_portal.R
import com.jerson.hcdc_portal.data.local.PortalDB
import com.jerson.hcdc_portal.databinding.FragmentDashboardKtBinding
import com.jerson.hcdc_portal.domain.model.Schedule
import com.jerson.hcdc_portal.presentation.building.Building
import com.jerson.hcdc_portal.presentation.dashboard.viewmodel.DashboardViewModel
import com.jerson.hcdc_portal.presentation.evaluation.EvaluationKt
import com.jerson.hcdc_portal.presentation.login.viewmodel.LoginViewModel
import com.jerson.hcdc_portal.presentation.main.MainKt
import com.jerson.hcdc_portal.presentation.search.GlobalSearchActivity
import com.jerson.hcdc_portal.presentation.settings.Settings
import com.jerson.hcdc_portal.presentation.subjects.SubjectDetailsKt
import com.jerson.hcdc_portal.presentation.subjects.adapter.SubjectAdapter
import com.jerson.hcdc_portal.presentation.subjects_offered.SubjectOfferedKt
import com.jerson.hcdc_portal.util.AppPreference
import com.jerson.hcdc_portal.util.Constants
import com.jerson.hcdc_portal.util.Constants.KEY_IS_ENROLLED
import com.jerson.hcdc_portal.util.Constants.KEY_STUDENTS_UNITS
import com.jerson.hcdc_portal.util.Constants.KEY_STUDENT_COURSE
import com.jerson.hcdc_portal.util.Constants.KEY_STUDENT_ID
import com.jerson.hcdc_portal.util.Constants.KEY_STUDENT_NAME
import com.jerson.hcdc_portal.util.CustomProfileDialog
import com.jerson.hcdc_portal.util.LoadingDialog
import com.jerson.hcdc_portal.util.MoreDialog
import com.jerson.hcdc_portal.util.Resource
import com.jerson.hcdc_portal.util.ScheduleHelper
import com.jerson.hcdc_portal.util.SnackBarKt
import com.jerson.hcdc_portal.util.setupCustomTheme
import com.jerson.hcdc_portal.util.stopRefreshing
import com.jerson.hcdc_portal.util.userAvatar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class DashboardKt : Fragment() {
    private var _binding: FragmentDashboardKtBinding? = null
    private val binding get() = _binding!!
    private val dashboardViewModel: DashboardViewModel by viewModels()
    private val loginViewModel: LoginViewModel by viewModels()
    private var loadingDialog: LoadingDialog? = null
    private lateinit var adapter: SubjectAdapter
    private val list = mutableListOf<Schedule>()

    @Inject
    lateinit var imageLoader: ImageLoader

    @Inject
    lateinit var pref: AppPreference

    @Inject
    lateinit var okHttpClient: OkHttpClient

    @Inject
    lateinit var portalDB: PortalDB

    private var isUserInitiatedRefresh = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardKtBinding.inflate(inflater, container, false)
        loadingDialog = context?.let { LoadingDialog(it) }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = SubjectAdapter(list) {
            startActivity(Intent(context, SubjectDetailsKt::class.java).putExtra("subject", it))
        }

        binding.apply {
            recyclerView.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            recyclerView.adapter = adapter

            swipeRefresh.setupCustomTheme(requireContext())
            swipeRefresh.setOnRefreshListener {
                isUserInitiatedRefresh = true
                dashboardViewModel.fetchSchedules()
            }
            syncStatusCard.setOnClickListener {
                isUserInitiatedRefresh = true
                swipeRefresh.isRefreshing = true
                dashboardViewModel.fetchSchedules()
            }

            // Quick Actions Navigation
            quickActionGrades.setOnClickListener {
                (activity as? MainKt)?.navigateToTab(R.id.academics)
            }
            quickActionSubjects.setOnClickListener {
                (activity as? MainKt)?.navigateToTab(R.id.academics)
            }
            quickActionAccounts.setOnClickListener {
                (activity as? MainKt)?.navigateToTab(R.id.finance)
            }
            quickActionCampus.setOnClickListener {
                (activity as? MainKt)?.navigateToTab(R.id.campus)
            }

            // Search & Settings Top Bar
            btnGlobalSearch.setOnClickListener {
                startActivity(Intent(context, GlobalSearchActivity::class.java))
            }
            btnSetting.setOnClickListener {
                startActivity(Intent(context, Settings::class.java))
            }

            // Schedule View Actions
            btnViewSchedule.setOnClickListener {
                (activity as? MainKt)?.navigateToTab(R.id.academics)
            }
            btnViewFullSchedule.setOnClickListener {
                (activity as? MainKt)?.navigateToTab(R.id.academics)
            }

            // Financial Summary Click
            cardFinanceSummary.setOnClickListener {
                (activity as? MainKt)?.navigateToTab(R.id.finance)
            }
            btnViewFinanceDetails.setOnClickListener {
                (activity as? MainKt)?.navigateToTab(R.id.finance)
            }

            // Profile long-click to change custom avatar
            profileIV.setOnLongClickListener {
                val dialog = CustomProfileDialog.newInstance(pref) {
                    _binding?.profileIV?.load(it)
                }
                dialog.show(parentFragmentManager, "CustomProfileDialog")
                true
            }

            // Compatibility hooks
            evaluation.setOnClickListener {
                startActivity(Intent(context, EvaluationKt::class.java))
            }
            subjectOffered.setOnClickListener {
                startActivity(Intent(context, SubjectOfferedKt::class.java))
            }
            buildings.setOnClickListener {
                startActivity(Intent(context, Building::class.java))
            }
            btnMore.setOnClickListener {
                val dialog = MoreDialog()
                dialog.show(parentFragmentManager, "MoreDialog")
            }
        }

        // Announcement banner
        val announcement = pref.getStringPreference(Constants.KEY_ENROLL_ANNOUNCE)
        if (announcement.isNotBlank()) {
            binding.enrollAnnounce.text = announcement
            binding.enrollAnnounceLayout.visibility = View.VISIBLE
        }

        loadProfileView()
        listenerFetch {
            loadProfileView()
        }
        observeCachedFinance()
        observeRelogon()
    }

    private fun observeRelogon() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                loginViewModel.login.collect { res ->
                    if (res is Resource.Success) {
                        dashboardViewModel.fetchSchedules()
                    }
                }
            }
        }
    }

    private fun observeCachedFinance() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                try {
                    portalDB.accountDao().getAccounts().collect { accounts ->
                        if (accounts.isNotEmpty()) {
                            val active = accounts.firstOrNull { !it.dueAmount.isNullOrBlank() } ?: accounts[0]
                            val amount = active.dueAmount
                            val period = active.dueTextPeriod
                            _binding?.apply {
                                if (!amount.isNullOrBlank()) {
                                    tvDashboardBalance.text = amount
                                }
                                if (!period.isNullOrBlank()) {
                                    tvDashboardDuePeriod.text = period
                                }
                            }
                        }
                    }
                } catch (_: Exception) {}
            }
        }
    }

    private fun listenerFetch(isDone: (Boolean) -> Unit) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                dashboardViewModel.fetchSchedules.collect {
                    when (it) {
                        is Resource.Loading -> {
                            val context = context ?: return@collect
                            _binding?.apply {
                                syncStatusTitle.text = getString(R.string.sync_status_syncing)
                                syncStatusSubtitle.text = "Fetching latest portal updates…"
                                syncStatusIcon.setImageResource(R.drawable.ic_cloud_sync)
                                syncStatusCard.setCardBackgroundColor(
                                    ContextCompat.getColor(context, R.color.status_syncing_bg)
                                )
                                syncStatusTitle.setTextColor(
                                    ContextCompat.getColor(context, R.color.status_syncing_text)
                                )
                                syncStatusSubtitle.setTextColor(
                                    ContextCompat.getColor(context, R.color.status_syncing_text)
                                )
                                if (it.data.isNullOrEmpty()) {
                                    progressBar.visibility = View.VISIBLE
                                } else {
                                    progressBar.visibility = View.GONE
                                    updateScheduleUi(it.data, isDone)
                                }
                            }
                        }

                        is Resource.Success -> {
                            val context = context ?: return@collect
                            _binding?.apply {
                                progressBar.visibility = View.GONE
                                swipeRefresh.stopRefreshing()
                                syncStatusTitle.text = getString(R.string.sync_status_synced)
                                syncStatusSubtitle.text = "Offline ready • Tap to sync"
                                syncStatusIcon.setImageResource(R.drawable.ic_cloud_done)
                                syncStatusCard.setCardBackgroundColor(
                                    ContextCompat.getColor(context, R.color.status_synced_bg)
                                )
                                syncStatusTitle.setTextColor(
                                    ContextCompat.getColor(context, R.color.status_synced_text)
                                )
                                syncStatusSubtitle.setTextColor(
                                    ContextCompat.getColor(context, R.color.status_synced_text)
                                )
                                updateScheduleUi(it.data.orEmpty(), isDone)
                            }
                            loadingDialog?.dismiss()
                            isUserInitiatedRefresh = false
                        }

                        is Resource.Error -> {
                            isDone(true)
                            val context = context ?: return@collect
                            val hasCachedData = !it.data.isNullOrEmpty()
                            _binding?.apply {
                                progressBar.visibility = View.GONE
                                swipeRefresh.stopRefreshing()
                                syncStatusTitle.text = getString(R.string.sync_status_offline)
                                syncStatusSubtitle.text = "Saved data is available • Tap to retry"
                                syncStatusIcon.setImageResource(R.drawable.ic_cloud_off)
                                syncStatusCard.setCardBackgroundColor(
                                    ContextCompat.getColor(context, R.color.status_offline_bg)
                                )
                                syncStatusTitle.setTextColor(
                                    ContextCompat.getColor(context, R.color.status_offline_text)
                                )
                                syncStatusSubtitle.setTextColor(
                                    ContextCompat.getColor(context, R.color.status_offline_text)
                                )
                                if (hasCachedData) {
                                    updateScheduleUi(it.data!!, isDone)
                                }
                            }
                            loadingDialog?.dismiss()

                            val msg = it.message.orEmpty()
                            if (msg.contains("session end", ignoreCase = true)) {
                                loginViewModel.reLogon()
                            } else if (isUserInitiatedRefresh || !hasCachedData) {
                                if (msg.isNotBlank()) {
                                    _binding?.let { b -> SnackBarKt.snackBarLong(b.root, msg) }
                                }
                            }
                            isUserInitiatedRefresh = false
                        }


                        else -> Unit
                    }
                }
            }
        }
    }

    private fun updateScheduleUi(data: List<Schedule>, isDone: (Boolean) -> Unit) {
        var listSize = data.size
        if (data.isNotEmpty()) {
            listSize = data.distinctBy { x -> x.subjectCode }.size
        }

        val filtered = data.filter { x -> ScheduleHelper.isToday(x) }

        // Compute Hero Next Class
        val nextClass = ScheduleHelper.getNextClass(data)
        _binding?.apply {
            totalSubTV.text = listSize.toString()

            if (nextClass != null) {
                layoutNextClassDetails.visibility = View.VISIBLE
                layoutNoNextClass.visibility = View.GONE
                tvNextClassCode.text = nextClass.schedule.subjectCode
                tvNextClassTitle.text = nextClass.schedule.description
                tvNextClassTime.text = nextClass.schedule.time
                tvNextClassRoom.text = ScheduleHelper.formatRoomBuilding(nextClass.schedule.room)
                tvNextClassCountdown.text = nextClass.statusText
            } else {
                layoutNextClassDetails.visibility = View.GONE
                layoutNoNextClass.visibility = View.VISIBLE
                tvNextClassCountdown.text = getString(R.string.no_more_classes)
            }

            list.clear()
            list.addAll(filtered)
            adapter.notifyDataSetChanged()

            if (filtered.isEmpty()) {
                emptyScheduleView.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                emptyScheduleView.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
            }
        }
        isDone(true)
    }

    private fun loadProfileView() {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val greetingTime = when (hour) {
            in 4..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            else -> "Good evening"
        }

        val fullName = pref.getStringPreference(KEY_STUDENT_NAME)
        val firstName = fullName.split(" ").firstOrNull { it.isNotBlank() }?.replaceFirstChar { it.uppercase() } ?: "Student"

        _binding?.apply {
            welcomeTV.text = "$greetingTime, $firstName 👋"
            unitsTV.text = pref.getStringPreference(KEY_STUDENTS_UNITS).ifEmpty { "0.0" }
            profileIV.load(userAvatar(pref), imageLoader) {
                size(200, 200)
            }
            nameTV.text = fullName.ifEmpty { "Student Name" }
            val course = pref.getStringPreference(KEY_STUDENT_COURSE)
            val studentId = pref.getStringPreference(KEY_STUDENT_ID)
            couseTV.text = "$course • $studentId".trim(' ', '•')
            val enrolledStatus = pref.getStringPreference(KEY_IS_ENROLLED)
            enrolledTV.text = if (enrolledStatus.isNotBlank()) enrolledStatus else "Officially Enrolled"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        loadingDialog?.dismiss()
        loadingDialog = null
        _binding = null
    }
}
