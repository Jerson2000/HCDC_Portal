package com.jerson.hcdc_portal.ui.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.jerson.hcdc_portal.databinding.ActivitySettingsBinding;

public class SettingsActivity extends AppCompatActivity {
    private ActivitySettingsBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}