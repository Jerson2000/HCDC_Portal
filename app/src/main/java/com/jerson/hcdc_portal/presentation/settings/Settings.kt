package com.jerson.hcdc_portal.presentation.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.jerson.hcdc_portal.R
import com.jerson.hcdc_portal.databinding.ActivitySettingsBinding
import com.jerson.hcdc_portal.util.AppPreference
import com.jerson.hcdc_portal.util.Constants
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class Settings:AppCompatActivity() {
    private lateinit var binding:ActivitySettingsBinding

    @Inject
    lateinit var pref:AppPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.header.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            binding.header.collapsingToolbar.title = "Settings"
        }
        checkChipTheme()

        binding.themeOption.setOnClickListener{
            binding.themeModeView.toggleVisibility()
        }

        binding.themeModeView.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val checkedId = checkedIds.first()
                when (checkedId) {
                    R.id.auto_chip -> {
                        setAppTheme(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                        binding.themeModeView.toggleVisibility()
                    }
                    R.id.night_chip -> {
                        setAppTheme(AppCompatDelegate.MODE_NIGHT_YES)
                        binding.themeModeView.toggleVisibility()
                    }
                    R.id.light_chip -> {
                        setAppTheme(AppCompatDelegate.MODE_NIGHT_NO)
                        binding.themeModeView.toggleVisibility()
                    }
                }
            }
        }

        binding.header.toolbar.setNavigationOnClickListener{
            onBackPressedDispatcher.onBackPressed()
        }
        binding.github.setOnClickListener{
            startActivity(Intent(Intent.ACTION_VIEW,Uri.parse(Constants.github)))
        }


    }

    private fun setAppTheme(theme:Int){
        AppCompatDelegate.setDefaultNightMode(theme)
        pref.setIntPreference(Constants.KEY_SETTINGS_THEME_MODE,theme)
    }

    private fun View.toggleVisibility(){
        this.visibility = if(visibility==View.VISIBLE) View.GONE else View.VISIBLE
    }

    private fun checkChipTheme(){
        when(pref.getIntPreference(Constants.KEY_SETTINGS_THEME_MODE)){
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> binding.autoChip.isChecked = true
            AppCompatDelegate.MODE_NIGHT_NO -> binding.lightChip.isChecked = true
            AppCompatDelegate.MODE_NIGHT_YES -> binding.nightChip.isChecked = true
        }
    }


}