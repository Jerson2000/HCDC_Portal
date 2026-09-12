package com.jerson.hcdc_portal.presentation.more

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil.load
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jerson.hcdc_portal.BuildConfig
import com.jerson.hcdc_portal.R
import com.jerson.hcdc_portal.databinding.FragmentMoreKtBinding
import com.jerson.hcdc_portal.presentation.chat.ChatGPT
import com.jerson.hcdc_portal.presentation.login.LoginKt
import com.jerson.hcdc_portal.presentation.login.viewmodel.LoginViewModel
import com.jerson.hcdc_portal.presentation.main.viewmodel.AppViewModel
import com.jerson.hcdc_portal.presentation.search.GlobalSearchActivity
import com.jerson.hcdc_portal.util.AppPreference
import com.jerson.hcdc_portal.util.Constants
import com.jerson.hcdc_portal.util.LoadingDialog
import com.jerson.hcdc_portal.util.Resource
import com.jerson.hcdc_portal.util.SnackBarKt.snackBarLong
import com.jerson.hcdc_portal.util.downloadApk
import com.jerson.hcdc_portal.util.userAvatar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MoreKt : Fragment() {

    private var _binding: FragmentMoreKtBinding? = null
    private val binding get() = _binding!!
    private val loginViewModel: LoginViewModel by viewModels()
    private val appViewModel: AppViewModel by viewModels()
    private var loadingDialog: LoadingDialog? = null
    private var isManualCheck = false
    private var pendingApkUrl: String? = null

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ ->
            val apk = pendingApkUrl
            pendingApkUrl = null
            if (apk != null) {
                context?.let { ctx ->
                    downloadApk(apk, ctx)
                }
            }
        }

    private val requestStoragePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            val apk = pendingApkUrl
            pendingApkUrl = null
            if (isGranted && apk != null) {
                context?.let { ctx ->
                    downloadApk(apk, ctx)
                }
            } else if (!isGranted) {
                _binding?.let {
                    snackBarLong(it.root, "Storage permission was denied. Cannot download update.")
                }
            }
        }

    @Inject
    lateinit var pref: AppPreference

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMoreKtBinding.inflate(inflater, container, false)
        loadingDialog = context?.let { LoadingDialog(it) }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadProfile()
        setupThemeChips()

        binding.cardAiAssistant.setOnClickListener {
            startActivity(Intent(context, ChatGPT::class.java))
        }

        binding.cardGlobalSearch.setOnClickListener {
            startActivity(Intent(context, GlobalSearchActivity::class.java))
        }

        binding.layoutCheckUpdate.setOnClickListener {
            isManualCheck = true
            loadingDialog?.show()
            appViewModel.checkForUpdate()
        }

        observeAppUpdate()

        binding.layoutVisionMission.setOnClickListener {
            context?.let { ctx ->
                MaterialAlertDialogBuilder(ctx)
                    .setTitle(R.string.vision_mission)
                    .setMessage(R.string.vm_core_values)
                    .setPositiveButton("Close", null)
                    .show()
            }
        }

        binding.layoutGithub.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(Constants.github)))
        }

        binding.btnMoreLogout.setOnClickListener {
            context?.let { ctx ->
                MaterialAlertDialogBuilder(ctx)
                    .setTitle("Logging out")
                    .setMessage("Are you sure you want to log out of HCDC Portal?")
                    .setPositiveButton("Log Out") { dialog, _ ->
                        loadingDialog?.show()
                        loginViewModel.logout { success ->
                            loadingDialog?.dismiss()
                            dialog.dismiss()
                            if (success) {
                                startActivity(
                                    Intent(context, LoginKt::class.java)
                                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                )
                            }
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }
    }

    private fun loadProfile() {
        val studentName = pref.getStringPreference(Constants.KEY_STUDENT_NAME)
        val studentId = pref.getStringPreference(Constants.KEY_STUDENT_ID)
        val studentCourse = pref.getStringPreference(Constants.KEY_STUDENT_COURSE)
        val enrolled = pref.getStringPreference(Constants.KEY_IS_ENROLLED)

        _binding?.apply {
            tvMoreName.text = studentName.ifEmpty { "Student" }
            tvMoreIdCourse.text = "$studentCourse • $studentId".trim(' ', '•')
            tvMoreEnrolled.text = enrolled.ifEmpty { "Officially Enrolled" }
            ivMoreAvatar.load(userAvatar(pref))
        }
    }

    private fun setupThemeChips() {
        when (pref.getIntPreference(Constants.KEY_SETTINGS_THEME_MODE)) {
            AppCompatDelegate.MODE_NIGHT_NO -> binding.chipThemeLight.isChecked = true
            AppCompatDelegate.MODE_NIGHT_YES -> binding.chipThemeDark.isChecked = true
            else -> binding.chipThemeAuto.isChecked = true
        }

        binding.chipGroupTheme.setOnCheckedStateChangeListener { _, checkedIds ->
            val mode = when (checkedIds.firstOrNull()) {
                R.id.chipThemeLight -> AppCompatDelegate.MODE_NIGHT_NO
                R.id.chipThemeDark -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
            AppCompatDelegate.setDefaultNightMode(mode)
            pref.setIntPreference(Constants.KEY_SETTINGS_THEME_MODE, mode)
        }
    }

    private fun observeAppUpdate() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                appViewModel.appUpdate.collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            loadingDialog?.dismiss()
                            if (!isManualCheck) {
                                return@collect
                            }
                            isManualCheck = false

                            val context = context ?: return@collect
                            val downloadUrl = resource.data.orEmpty()

                            if (downloadUrl.isEmpty()) {
                                MaterialAlertDialogBuilder(context)
                                    .setTitle("Up to Date")
                                    .setMessage("You're using the latest version of HCDC Portal (v${BuildConfig.VERSION_NAME}). All security patches and features are current.")
                                    .setPositiveButton("OK", null)
                                    .show()
                            } else {
                                MaterialAlertDialogBuilder(context)
                                    .setTitle("New Update Available")
                                    .setMessage("A newer version of HCDC Portal is available. Would you like to download and install the update now?")
                                    .setPositiveButton("Update Now") { dialog, _ ->
                                        dialog.dismiss()
                                        startUpdateDownload(downloadUrl)
                                    }
                                    .setNegativeButton("Later", null)
                                    .show()
                            }
                            appViewModel.resetUpdateState()
                        }

                        is Resource.Error -> {
                            loadingDialog?.dismiss()
                            if (isManualCheck) {
                                isManualCheck = false
                                _binding?.let {
                                    val errorMsg = resource.message ?: "Could not check for updates."
                                    snackBarLong(it.root, errorMsg)
                                }
                                appViewModel.resetUpdateState()
                            }
                        }

                        is Resource.Loading -> {
                            // Handled via loadingDialog?.show()
                        }

                        else -> Unit
                    }
                }
            }
        }
    }

    private fun startUpdateDownload(url: String) {
        val ctx = context ?: return

        // 1. For Android 13+ (TIRAMISU, API 33+), check/request notification permission for the progress bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasNotificationPermission = ContextCompat.checkSelfPermission(
                ctx,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasNotificationPermission) {
                pendingApkUrl = url
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                return
            }
        }

        // 2. For Android < 10 (Q, API 29), check/request WRITE_EXTERNAL_STORAGE
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            val hasStoragePermission = ContextCompat.checkSelfPermission(
                ctx,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasStoragePermission) {
                pendingApkUrl = url
                requestStoragePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                return
            }
        }

        downloadApk(url, ctx)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        loadingDialog?.dismiss()
        loadingDialog = null
        _binding = null
    }
}
