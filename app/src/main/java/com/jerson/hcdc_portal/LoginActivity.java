package com.jerson.hcdc_portal;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import com.jerson.hcdc_portal.databinding.ActivityLoginBinding;
import com.jerson.hcdc_portal.databinding.ActivityMainBinding;
import com.jerson.hcdc_portal.model.DashboardModel;
import com.jerson.hcdc_portal.network.Clients;
import com.jerson.hcdc_portal.ui.MainActivity;
import com.jerson.hcdc_portal.util.AppConstants;
import com.jerson.hcdc_portal.util.Dialog;
import com.jerson.hcdc_portal.util.SnackBarUtil;
import com.jerson.hcdc_portal.viewmodel.DashboardViewModel;
import com.jerson.hcdc_portal.viewmodel.LoginViewModel;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private LoginViewModel viewModel;
    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

//        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

//        textChangeListener();

        binding.loginBtn.setOnClickListener(v -> {
            if (binding.emailET.getText().toString().equals("")) {
                binding.emailLayout.setError("Cannot be empty");
            }
            if (binding.passET.getText().toString().equals("")) {
                binding.emailLayout.setError("Cannot be empty");
            }

            if (!binding.passET.getText().toString().equals("") && !binding.emailET.getText().toString().equals("")) {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.loginBtn.setEnabled(false);
                Init(binding.emailET.getText().toString(),binding.passET.getText().toString());

            }


        });

    }

    void Init(String email, String pass) {
        viewModel.login(email, pass).observeForever(response -> {
            if(response.toLowerCase(Locale.ROOT).contains("timeout")||response.toLowerCase(Locale.ROOT).contains("error fetching url")){
                SnackBarUtil.SnackBarIndefiniteDuration(binding.snackBarLayout, "Connection Timeout")
                        .setAction("Retry", view -> {
                            retry(email,pass);
                        })
                        .show();
                binding.progressBar.setVisibility(View.GONE);
                return;
            }

            if(response.toLowerCase(Locale.ROOT).contains("credentials")){
                SnackBarUtil.SnackBarLong(binding.snackBarLayout, response).show();
                return;
            }

            binding.progressBar.setVisibility(View.GONE);
            SnackBarUtil.SnackBarLong(binding.snackBarLayout, response).show();

            if (response.toLowerCase(Locale.ROOT).contains("logged in")) {
                new Handler().postDelayed(() -> {
                    startActivity(new Intent(this, MainActivity.class));
                }, 1500);

            }


        });


    }

    void retry(String email,String pass){
        viewModel.login(email,pass);
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.loginBtn.setEnabled(false);
    }

    void textChangeListener() {
        binding.emailET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.emailLayout.setError(null);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        binding.passET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.passLayout.setError(null);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }


}