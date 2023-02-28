package com.jerson.hcdc_portal;

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
import com.jerson.hcdc_portal.network.HttpClient;
import com.jerson.hcdc_portal.ui.MainActivity;
import com.jerson.hcdc_portal.util.SnackBarUtil;
import com.jerson.hcdc_portal.viewmodel.DashboardViewModel;
import com.jerson.hcdc_portal.viewmodel.LoginViewModel;

import org.jsoup.nodes.Document;

import java.util.Locale;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.FormBody;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private LoginViewModel viewModel;
    private DashboardViewModel dashboardViewModel;
    private static final String TAG = "LoginActivity";
    String authToken;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

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
                Init(binding.emailET.getText().toString(), binding.passET.getText().toString());

            }


        });

        loadWatchList();
        test();

    }

    void Init(String email, String pass) {
        viewModel.login(email, pass).observeForever(response -> {
            if (response.toLowerCase(Locale.ROOT).contains("timeout") ||
                    response.toLowerCase(Locale.ROOT).contains("error fetching url") ||
                    response.toLowerCase(Locale.ROOT).contains("time out")) {
                SnackBarUtil.SnackBarIndefiniteDuration(binding.snackBarLayout, "Connection Timeout")
                        .setAction("Retry", view -> {
                            retry(email, pass);
                        })
                        .show();
                binding.progressBar.setVisibility(View.GONE);
                return;
            }

            if (response.toLowerCase(Locale.ROOT).contains("credentials")) {
                SnackBarUtil.SnackBarLong(binding.snackBarLayout, response).show();
                binding.progressBar.setVisibility(View.GONE);
                binding.loginBtn.setEnabled(true);
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

    void test() {
        HttpClient.getInstance(getApplicationContext()).GET("http://studentportal.hcdc.edu.ph/login", new OnHttpResponseListener<Document>() {
            @Override
            public void onResponse(Document response) {
//                Log.d(TAG, "onResponse: "+response);
//                System.out.println(response);
                authToken = response.select("input[name=_token]").first().attr("value");
                System.out.println(authToken);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "onFailure: ", e.fillInStackTrace());
            }
        });


    }

    void postTest() {
        FormBody formBody = new FormBody.Builder()

                .build();

        HttpClient.getInstance(getApplicationContext()).POST("http://studentportal.hcdc.edu.ph/loginPost", formBody, new OnHttpResponseListener<Document>() {
            @Override
            public void onResponse(Document response) {
                System.out.println(response);
            }

            @Override
            public void onFailure(Exception e) {
                System.out.println(e.getStackTrace());
            }
        });
    }

    // check if the dashboard table have data
    private void loadWatchList() {
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

    void retry(String email, String pass) {
        viewModel.login(email, pass);
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