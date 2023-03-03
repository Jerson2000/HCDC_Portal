package com.jerson.hcdc_portal;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.jerson.hcdc_portal.databinding.ActivityLoginBinding;
import com.jerson.hcdc_portal.ui.MainActivity;
import com.jerson.hcdc_portal.util.SnackBarUtil;
import com.jerson.hcdc_portal.viewmodel.DashboardViewModel;
import com.jerson.hcdc_portal.viewmodel.LoginViewModel;

import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private LoginViewModel viewModel;
    private DashboardViewModel dashboardViewModel;
    private static final String TAG = "LoginActivity";
    private Executor executor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        executor = Executors.newSingleThreadExecutor();

        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        dashboardViewModel = new ViewModelProvider(this).get(DashboardViewModel.class);
        textChangeListener();
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
                login(binding.emailET.getText().toString(), binding.passET.getText().toString(),getApplicationContext());

            }


        });

        loadDatabase();


    }



    void login(String email, String pass, Context context) {
        viewModel.Login(email, pass, getApplicationContext());
        viewModel.getRes().observe(this, res -> {
            System.out.println(res);

            if (res.toLowerCase(Locale.ROOT).contains("timeout") ||
                    res.toLowerCase(Locale.ROOT).contains("error fetching url") ||
                    res.toLowerCase(Locale.ROOT).contains("time out")) {
                SnackBarUtil.SnackBarIndefiniteDuration(binding.snackBarLayout, "Connection Timeout")
                        .setAction("Retry", view -> {
                            retry(email, pass,context);
                        })
                        .show();
                binding.progressBar.setVisibility(View.GONE);
                return;
            }

            if (res.toLowerCase(Locale.ROOT).contains("credentials")) {
                SnackBarUtil.SnackBarLong(binding.snackBarLayout, res).show();
                binding.progressBar.setVisibility(View.GONE);
                binding.loginBtn.setEnabled(true);
                return;
            }

            binding.progressBar.setVisibility(View.GONE);
            SnackBarUtil.SnackBarLong(binding.snackBarLayout, res).show();

            if (res.toLowerCase(Locale.ROOT).contains("logged in")) {
                new Handler().postDelayed(() -> {
                    startActivity(new Intent(this, MainActivity.class));
                }, 1500);

            }
        });
    }

    // check if the dashboard table have data
    private void loadDatabase() {
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(dashboardViewModel.loadDashboard()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> {
                    // Handle the successful retrieval
                    Log.d("TAG", "Data retrieved successfully: " + data.size());
                }, throwable -> {
                    // Handle the error
                    Log.e("TAG", "Error retrieving data", throwable);
                }));
    }

    void retry(String email, String pass, Context context) {
        login(email,pass,context);
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