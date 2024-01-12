package com.jerson.hcdc_portal.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.bumptech.glide.Glide;
import com.jerson.hcdc_portal.PortalApp;
import com.jerson.hcdc_portal.R;
import com.jerson.hcdc_portal.databinding.ActivitySplashBinding;
import com.jerson.hcdc_portal.util.BaseActivity;
import com.jerson.hcdc_portal.util.DownloadRoomsWorker;
import com.jerson.hcdc_portal.util.NetworkUtil;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends BaseActivity<ActivitySplashBinding> {

    @Override
    protected void onStart() {
        super.onStart();
        if (NetworkUtil.isConnected()) downloadRooms();
        if(!getBindingNull()){
            Glide.with(this)
                    .load(R.drawable.bg)
                    .into(getBinding().bg);

            Glide.with(this)
                    .load(R.drawable.logo)
                    .into(getBinding().logo);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (PortalApp.getPreferenceManager().getBoolean(PortalApp.KEY_IS_LOGIN)) {
            if (NetworkUtil.isConnected()) autoReLogin();
            else loading(MainActivity.class);
        } else {
            loading(LoginActivity.class);
        }
    }


    void autoReLogin() {
        getBinding().displayS.setText("Checking session...");
        NetworkUtil.checkSession(session -> {
            if (session) {
                getBinding().displayS.setText("Syncing...");
                NetworkUtil.reLogin(logged -> {
                    if (logged) {
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    }
                });
            } else {
                loading(MainActivity.class);
            }
        });
    }

    void loading(Class<?> T) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(getApplicationContext(), T));
            finish();
        }, 2000);
    }

    void downloadRooms() {
        Data inputData = new Data.Builder()
                .putString("url", "https://raw.githubusercontent.com/Jerson2000/HCDC_Portal/assets/assets/rooms.json")
                .putString("fileName", "rooms.json")
                .build();

        OneTimeWorkRequest downloadRequest =
                new OneTimeWorkRequest.Builder(DownloadRoomsWorker.class)
                        .setInputData(inputData)
                        .build();
        WorkManager.getInstance(this).enqueue(downloadRequest);
    }


    @Override
    protected ActivitySplashBinding createBinding(LayoutInflater layoutInflater) {
        return ActivitySplashBinding.inflate(layoutInflater);
    }
}