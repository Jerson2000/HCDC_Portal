package com.jerson.hcdc_portal.ui.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.android.material.navigation.NavigationBarView;
import com.jerson.hcdc_portal.PortalApp;
import com.jerson.hcdc_portal.R;
import com.jerson.hcdc_portal.databinding.ActivityMainBinding;
import com.jerson.hcdc_portal.util.BaseActivity;
import com.jerson.hcdc_portal.util.DownloadRoomsWorker;
import com.jerson.hcdc_portal.util.DownloadWorker;
import com.jerson.hcdc_portal.util.NetworkUtil;
import com.jerson.hcdc_portal.util.PreferenceManager;
import com.jerson.hcdc_portal.util.SnackBarUtil;
import com.jerson.hcdc_portal.viewmodel.AccountViewModel;
import com.jerson.hcdc_portal.viewmodel.DashboardViewModel;
import com.jerson.hcdc_portal.viewmodel.EnrollHistoryViewModel;
import com.jerson.hcdc_portal.viewmodel.GradesViewModel;
import com.jerson.hcdc_portal.viewmodel.LoginViewModel;

public class MainActivity extends BaseActivity<ActivityMainBinding> implements NavigationBarView.OnItemSelectedListener {
    private static final String TAG = "MainActivity";

    private ActivityMainBinding binding;
    private NavController navController;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = getBinding();

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
            case R.id.subjects:
                navController.popBackStack();
                navController.navigate(R.id.subjectsFragment);
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

    @Override
    protected ActivityMainBinding createBinding(LayoutInflater layoutInflater) {
        return ActivityMainBinding.inflate(layoutInflater);
    }
}