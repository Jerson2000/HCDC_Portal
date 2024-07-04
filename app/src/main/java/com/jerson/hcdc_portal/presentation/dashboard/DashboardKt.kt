package com.jerson.hcdc_portal.presentation.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.jerson.hcdc_portal.databinding.FragmentDashboardKtBinding
import com.jerson.hcdc_portal.domain.model.Schedule
import com.jerson.hcdc_portal.presentation.dashboard.viewmodel.DashboardViewModel
import com.jerson.hcdc_portal.presentation.login.viewmodel.LoginViewModel
import com.jerson.hcdc_portal.presentation.subjects.adapter.SubjectAdapter
import com.jerson.hcdc_portal.util.AppPreference
import com.jerson.hcdc_portal.util.Constants
import com.jerson.hcdc_portal.util.Constants.KEY_STUDENTS_UNITS
import com.jerson.hcdc_portal.util.Constants.KEY_STUDENT_COURSE
import com.jerson.hcdc_portal.util.Constants.KEY_STUDENT_ID
import com.jerson.hcdc_portal.util.Constants.KEY_STUDENT_NAME
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

@OptIn(ExperimentalEncodingApi::class)
@AndroidEntryPoint
class DashboardKt : Fragment() {
    private lateinit var binding: FragmentDashboardKtBinding
    private val dashboardViewModel: DashboardViewModel by viewModels()
    private var loadingDialog: LoadingDialog? = null
    private val loginViewModel:LoginViewModel by viewModels()
    private lateinit var adapter:SubjectAdapter
    private val list= mutableListOf<Schedule>()

    @Inject
    lateinit var pref:AppPreference
    @Inject
    lateinit var okHttpClient: OkHttpClient
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
        loadingDialog = context?.let { LoadingDialog(it) }
        adapter = SubjectAdapter(list)
        val isLoaded = pref.getBooleanPreference(Constants.KEY_IS_ACCOUNT_LOADED)

        if (isLoaded) dashboardViewModel.getSchedules()
        listenerFetch()

        binding.apply {
            recyclerView.layoutManager = LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false)
            recyclerView.adapter = adapter
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
                            binding.apply{
                                var listSize = it.data!!.size
                                if(it.data.isNotEmpty())
                                    listSize = it.data.distinctBy { x-> x.subjectCode }.size
                                totalSubTV.text = listSize.toString()
                                unitsTV.text = pref.getStringPreference(KEY_STUDENTS_UNITS)
                                profileIV.load(userAvatar(pref))
                                nameTV.text = pref.getStringPreference(KEY_STUDENT_NAME)
                                couseTV.text =pref.getStringPreference(KEY_STUDENT_COURSE) +" ${pref.getStringPreference(KEY_STUDENT_ID)}"
                                list.clear()
                                val filtered = it.data.filter {x->
                                    x.days?.contains(getToday()) == true
                                }
                                list.addAll(filtered)
                                adapter.notifyDataSetChanged()
                            }
                        }

                        is Resource.Error -> {
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
            else -> SimpleDateFormat("EEE",Locale.getDefault()).format(Date()).first().toString()
        }
        return day
    }

    /* val imageLoader = context?.let { ImageLoader.Builder(it).okHttpClient(okHttpClient).build() }
        if (imageLoader != null) {
            binding.asd.load("https://studentportal.hcdc.edu.ph/images/hcdc_logo.png",imageLoader) {
                placeholder(R.drawable.ic_article)
                error(R.drawable.ic_dashboard)
                listener(
                    onError = { request, throwable ->
                        Log.e("HUHU", "onViewCreated: ${throwable.throwable.message}\nrequest: ${request.data}", )
                    }
                )
            }
        }*/


}
