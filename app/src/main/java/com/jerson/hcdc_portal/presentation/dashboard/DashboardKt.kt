package com.jerson.hcdc_portal.presentation.dashboard

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
import coil.ImageLoader
import coil.load
import com.jerson.hcdc_portal.R
import com.jerson.hcdc_portal.databinding.FragmentDashboardKtBinding
import com.jerson.hcdc_portal.domain.model.Schedule
import com.jerson.hcdc_portal.presentation.dashboard.viewmodel.DashboardViewModel
import com.jerson.hcdc_portal.presentation.evaluation.EvaluationKt
import com.jerson.hcdc_portal.presentation.login.viewmodel.LoginViewModel
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
import com.jerson.hcdc_portal.util.Resource
import com.jerson.hcdc_portal.util.SnackBarKt
import com.jerson.hcdc_portal.util.userAvatar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.io.encoding.ExperimentalEncodingApi


@AndroidEntryPoint
class DashboardKt : Fragment() {
    private lateinit var binding: FragmentDashboardKtBinding
    private val dashboardViewModel: DashboardViewModel by viewModels()
    private var loadingDialog: LoadingDialog? = null
    private val loginViewModel: LoginViewModel by viewModels()
    private lateinit var adapter: SubjectAdapter
    private val list = mutableListOf<Schedule>()
    private lateinit var imageLoader: ImageLoader

    @Inject
    lateinit var pref: AppPreference

    @Inject
    lateinit var okHttpClient: OkHttpClient


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDashboardKtBinding.inflate(inflater, container, false)
        imageLoader = context?.let {
            ImageLoader.Builder(it).allowHardware(false).okHttpClient(okHttpClient).build()
        }!!
        loadingDialog = context?.let { LoadingDialog(it) }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = SubjectAdapter(list){
            startActivity(Intent(context,SubjectDetailsKt::class.java).putExtra("subject",it))
        }
        val isLoaded = pref.getBooleanPreference(Constants.KEY_IS_ACCOUNT_LOADED)

        if (isLoaded) dashboardViewModel.getSchedules()
        listenerFetch{
            if(it)loadProfileView()
            else loadProfileView()
        }

        binding.apply {
            recyclerView.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            recyclerView.adapter = adapter
            evaluation.setOnClickListener {
                startActivity(Intent(context, EvaluationKt::class.java))
            }
        }
        val announcement = pref.getStringPreference(Constants.KEY_ENROLL_ANNOUNCE)
        if (announcement.isNotEmpty() || announcement.isNotBlank()) {
            binding.enrollAnnounce.text = announcement
            binding.enrollAnnounceLayout.visibility = View.VISIBLE
        }
        binding.subjectOffered.setOnClickListener{
            startActivity(Intent(context, SubjectOfferedKt::class.java))
        }

        binding.profileIV.setOnLongClickListener{
            val dialog = CustomProfileDialog(pref){
                binding.profileIV.load(it)
            }
            dialog.show(parentFragmentManager, "CustomProfileDialog")
            true
        }
        binding.btnSetting.setOnClickListener{
            startActivity(Intent(context, Settings::class.java))
        }
    }

    private fun listenerFetch(isDone:(Boolean)-> Unit) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                dashboardViewModel.fetchSchedules.collect {
                    when (it) {
                        is Resource.Loading -> {
                            loadingDialog!!.show()
                        }

                        is Resource.Success -> {
                            loadingDialog!!.dismiss()
                            binding.apply {
                                var listSize = it.data!!.size
                                if (it.data.isNotEmpty())
                                    listSize = it.data.distinctBy { x -> x.subjectCode }.size
                                totalSubTV.text = listSize.toString()
                                list.clear()
                                val filtered = it.data.filter { x ->
                                    x.days?.contains(getToday()) == true
                                }
                                list.addAll(filtered)
                                adapter.notifyDataSetChanged()
                                isDone(true)
                            }
                        }

                        is Resource.Error -> {
                            isDone(true)
                            loadingDialog!!.dismiss()
                            it.message?.let { it1 -> SnackBarKt.snackBarLong(binding.root, it1) }
                        }

                        else -> Unit
                    }
                }
            }
        }
    }

    private fun getToday(): String {
        val day = when (SimpleDateFormat("EEE", Locale.getDefault()).format(Date())) {
            "Thu" -> "Th"
            "Sun" -> "Su"
            else -> SimpleDateFormat("EEE", Locale.getDefault()).format(Date()).first().toString()
        }
        return day
    }
    private fun loadProfileView(){
        binding.apply {
            unitsTV.text = pref.getStringPreference(KEY_STUDENTS_UNITS)
            profileIV.load(userAvatar(pref), imageLoader)
            nameTV.text = pref.getStringPreference(KEY_STUDENT_NAME)
            couseTV.text = pref.getStringPreference(KEY_STUDENT_COURSE) + " ${
                pref.getStringPreference(KEY_STUDENT_ID)
            }"
            enrolledTV.text = pref.getStringPreference(KEY_IS_ENROLLED)
        }
    }
}
