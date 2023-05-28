package com.jerson.hcdc_portal.ui;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.navigation.NavigationBarView;
import com.jerson.hcdc_portal.R;
import com.jerson.hcdc_portal.databinding.ActivityMainBinding;
import com.jerson.hcdc_portal.util.SnackBarUtil;

public class MainActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener {
    private static final String TAG = "MainActivity";

    ActivityMainBinding binding;
    NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        Init();

    }

    void Init() {
        binding.navbar.setOnItemSelectedListener(this);
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.dashboard:
                navController.popBackStack();
                navController.navigate(R.id.dashboardFragment);
                break;
            case R.id.grades:
                navController.popBackStack();
                navController.navigate(R.id.gradesFragment);
                break;
            case R.id.enrollHistory:
                navController.popBackStack();
                navController.navigate(R.id.enrollHistory);
                break;
            case R.id.accounts:
                navController.popBackStack();
                navController.navigate(R.id.accountFragment);
                break;
            default:

        }

        return true;
    }
}