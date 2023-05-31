package com.jerson.hcdc_portal.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.jerson.hcdc_portal.PortalApp;
import com.jerson.hcdc_portal.databinding.ActivityLoginBinding;
import com.jerson.hcdc_portal.listener.DynamicListener;
import com.jerson.hcdc_portal.ui.MainActivity;
import com.jerson.hcdc_portal.util.PreferenceManager;
import com.jerson.hcdc_portal.util.SnackBarUtil;
import com.jerson.hcdc_portal.viewmodel.DashboardViewModel;
import com.jerson.hcdc_portal.viewmodel.LoginViewModel;

import java.util.Locale;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private LoginViewModel viewModel;
    private DashboardViewModel dashboardViewModel;
    private static final String TAG = "LoginActivity";
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        preferenceManager = new PreferenceManager(this);

        if(preferenceManager.getBoolean(PortalApp.KEY_IS_LOGIN)){
            finish();
        }

        init();
        listeners();

    }

    void init() {

        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        dashboardViewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

    }

    void listeners() {

        binding.loginBtn.setOnClickListener(v -> {
            if (PortalApp.isConnected()) {
                login();
            } else {
                SnackBarUtil.SnackBarLong(binding.snackBarLayout, "No internet connection.").show();
            }

        });

    }


    void login() {

        String email = binding.emailET.getText().toString();
        String pass = binding.passET.getText().toString();

        if (binding.emailET.getText().toString().equals("") || binding.passET.getText().toString().equals("")) {
            SnackBarUtil.SnackBarLong(binding.snackBarLayout, "Some field/s are empty.").show();
        }

        if (!binding.passET.getText().toString().equals("") && !binding.emailET.getText().toString().equals("")) {

            isLoading(true, false);

            viewModel.Login(email, pass).observe(this, res -> {
                System.out.println(res);
                if (res.toLowerCase(Locale.ROOT).contains("timeout") ||
                        res.toLowerCase(Locale.ROOT).contains("error fetching url") ||
                        res.toLowerCase(Locale.ROOT).contains("time out")) {
                    SnackBarUtil.SnackBarIndefiniteDuration(binding.snackBarLayout, "Connection Timeout")
                            .setAction("Retry", view -> {
                                retry(email, pass);
                                isLoading(false, true);
                            })
                            .show();
                    binding.progressBar.setVisibility(View.GONE);
                }

                if (res.toLowerCase(Locale.ROOT).contains("credentials")) {
                    SnackBarUtil.SnackBarLong(binding.snackBarLayout, res).show();
                    isLoading(false, true);
                }

                if (res.toLowerCase(Locale.ROOT).contains("logged in")) {
                    binding.progressBar.setVisibility(View.GONE);

                    /* deleting all data in dashboard table */
                    deleteData(object -> {
                        if(object){
                            saveData(); /* then saving it */
                        }
                    });

                    preferenceManager.putBoolean(PortalApp.KEY_IS_LOGIN,true);
                    preferenceManager.putString(PortalApp.KEY_EMAIL,email);
                    preferenceManager.putString(PortalApp.KEY_PASSWORD,pass);


                    SnackBarUtil.SnackBarLong(binding.snackBarLayout, res).show();
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        startActivity(new Intent(this, MainActivity.class));
                    }, 1300);


                }
            });
        }

    }


    void retry(String email, String pass) {
        viewModel.Login(email, pass);
        isLoading(true, false);
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
        viewModel.getDashboard().observe(this,data->{
            if(data!=null){
                CompositeDisposable compositeDisposable = new CompositeDisposable();
                compositeDisposable.add(dashboardViewModel.insertDashboard(data)
                        .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                        .subscribe(() -> {
                            Log.d(TAG, "saveData: saved");
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

}