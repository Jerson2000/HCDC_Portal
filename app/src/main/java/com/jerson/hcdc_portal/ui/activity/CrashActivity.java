package com.jerson.hcdc_portal.ui.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.jerson.hcdc_portal.PortalApp;
import com.jerson.hcdc_portal.R;
import com.jerson.hcdc_portal.databinding.ActivityCrashBinding;

import java.util.Random;

public class CrashActivity extends AppCompatActivity {
    ActivityCrashBinding binding;
    public static Throwable PENDING_ERROR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCrashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.errLayout.emoji.setText(PortalApp.SAD_EMOJIS[new Random().nextInt(6)]);
        if (PENDING_ERROR != null)
            binding.errLayout.response.setText(PENDING_ERROR.toString());
        else
            binding.errLayout.response.setText(getIntent().getExtras().containsKey("ex") ? getIntent().getStringExtra("ex") : "");

        binding.restartBtn.setOnClickListener(v -> {
            triggerRebirth(PortalApp.getAppContext());
        });
    }

    public static void triggerRebirth(Context context) {
        PackageManager packageManager = context.getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(context.getPackageName());
        ComponentName componentName = intent.getComponent();
        Intent mainIntent = Intent.makeRestartActivityTask(componentName);
        // Required for API 34 and later
        // Ref: https://developer.android.com/about/versions/14/behavior-changes-14#safer-intents
        mainIntent.setPackage(context.getPackageName());
        context.startActivity(mainIntent);
        Runtime.getRuntime().exit(0);
    }

    @Override
    public void onBackPressed() {
    }
}