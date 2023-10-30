package com.jerson.hcdc_portal.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.jerson.hcdc_portal.PortalApp;
import com.jerson.hcdc_portal.R;
import com.jerson.hcdc_portal.databinding.ActivityLoginBinding;
import com.jerson.hcdc_portal.listener.DynamicListener;
import com.jerson.hcdc_portal.ui.MainActivity;
import com.jerson.hcdc_portal.util.BaseActivity;
import com.jerson.hcdc_portal.util.NetworkUtil;
import com.jerson.hcdc_portal.util.PreferenceManager;
import com.jerson.hcdc_portal.util.SnackBarUtil;
import com.jerson.hcdc_portal.viewmodel.DashboardViewModel;
import com.jerson.hcdc_portal.viewmodel.LoginViewModel;

import java.util.HashMap;
import java.util.Locale;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class LoginActivity extends BaseActivity<ActivityLoginBinding> {
    private ActivityLoginBinding binding;
    private LoginViewModel viewModel;
    private DashboardViewModel dashboardViewModel;
    private static final String TAG = "LoginActivity";
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = getBinding();

        preferenceManager = new PreferenceManager(this);

        if (preferenceManager.getBoolean(PortalApp.KEY_IS_LOGIN)) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

        init();
        listeners();

    }

    void init() {
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        dashboardViewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        Glide.with(this).load(R.drawable.logo).into(binding.logo);

        observeErr();

    }

    void listeners() {

        binding.loginBtn.setOnClickListener(v -> {
            if (NetworkUtil.isConnected()) {
                if (!binding.emailET.getText().toString().equals("") && !binding.passET.getText().toString().equals("")) {
                    login();
                } else {
                    SnackBarUtil.SnackBarLong(binding.snackBarLayout, "Some field/s are empty.").show();
                }


            } else {
                SnackBarUtil.SnackBarLong(binding.snackBarLayout, "No internet connection.").show();
            }
        });

    }


    void login() {

        String email = binding.emailET.getText().toString();
        String pass = binding.passET.getText().toString();


        isLoading(true, false);
        /*viewModel.Login(email, pass);*/
        NetworkUtil.checkSession(logged -> {
            if (logged) {
                viewModel.Login(email, pass, loginListener);
            }
        });


    }

    DynamicListener<Boolean> loginListener = new DynamicListener<Boolean>() {
        @Override
        public void dynamicListener(Boolean object) {
            if (object) {
                /* deleting all data in dashboard table */
                deleteData(object1 -> {
                    if (object1) {
                        saveData(); /* then saving it */
                    }
                });

                isLoading(false, false);

                preferenceManager.putBoolean(PortalApp.KEY_IS_LOGIN, true);
                preferenceManager.putString(PortalApp.KEY_EMAIL, binding.emailET.getText().toString());
                preferenceManager.putString(PortalApp.KEY_PASSWORD, binding.passET.getText().toString());

                SnackBarUtil.SnackBarLong(binding.snackBarLayout, "Logged In").show();
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                }, 1300);
            }
        }
    };

    void observeErr() {


        viewModel.getErr().observe(this, err -> {

            if (err.getMessage().toLowerCase(Locale.ROOT).contains("credentials")) {
                SnackBarUtil.SnackBarLong(binding.snackBarLayout, err.getMessage()).show();
                isLoading(false, true);

            } else {
                isLoading(false, false);
                SnackBarUtil.SnackBarIndefiniteDuration(binding.snackBarLayout, err.getMessage())
                        .setAction("Retry", view -> {
                            viewModel.Login(binding.emailET.getText().toString(), binding.passET.getText().toString(), loginListener);
                            isLoading(true, false);
                        })
                        .show();
            }

        });

    }


    void isLoading(boolean loading, boolean isWrong) {
        if (loading) {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.loginBtn.setEnabled(false);
        } else {
            binding.progressBar.setVisibility(View.GONE);
            binding.loginBtn.setEnabled(false);
        }

        if (isWrong) {
            binding.progressBar.setVisibility(View.GONE);
            binding.loginBtn.setEnabled(true);
        }
    }

    /* Saving dashboard data in database*/
    void saveData() {
        viewModel.getDashboard().observe(this, data -> {
            if (data != null) {
                CompositeDisposable compositeDisposable = new CompositeDisposable();
                compositeDisposable.add(dashboardViewModel.insertDashboard(data)
                        .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                        .subscribe(() -> {
                            Log.d(TAG, "saveData: saved " + data.size());
                        }, throwable -> {
                            Log.e(TAG, "saveData: ", throwable);
                        })
                );
            }
        });

    }

    void deleteData(DynamicListener<Boolean> isDeleted) {
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(dashboardViewModel.deleteDashboardData()
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    isDeleted.dynamicListener(true);
                    Log.d(TAG, "deleteData: success");
                }, throwable -> {
                    isDeleted.dynamicListener(false);
                    Log.e(TAG, "deleteData: ", throwable);
                })
        );
    }

    @Override
    protected ActivityLoginBinding createBinding(LayoutInflater layoutInflater) {
        return ActivityLoginBinding.inflate(layoutInflater);
    }

}