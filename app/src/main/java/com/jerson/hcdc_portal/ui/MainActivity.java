package com.jerson.hcdc_portal.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.jerson.hcdc_portal.R;
import com.jerson.hcdc_portal.databinding.ActivityMainBinding;
import com.jerson.hcdc_portal.network.Clients;
import com.jerson.hcdc_portal.util.AppConstants;
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
            case R.id.evaluation:
                navController.popBackStack();
                navController.navigate(R.id.evaluationFragment);
                break;
            case R.id.accounts:
                navController.popBackStack();
                navController.navigate(R.id.accountFragment);
                break;
            default:
                SnackBarUtil.SnackBarShort(binding.mainLayout, "Is not implemented").show();
        }
//        toggleIcon(item);

        return true;
    }

   /* void toggleIcon(MenuItem item){
        boolean itemState = !item.isChecked();
        switch (item.getItemId()){
            case R.id.dashboard:
                item.setChecked(itemState);
                item.setIcon(itemState ? ResourcesCompat.getDrawable(getResources(),R.drawable.ic_dashboard,null) : ResourcesCompat.getDrawable(getResources(),R.drawable.ic_dashboard_outline,null));
//                item.setIcon(R.drawable.ic_dashboard);
                break;
            case R.id.grades:
                item.setChecked(itemState);
                item.setIcon(itemState ? ResourcesCompat.getDrawable(getResources(),R.drawable.ic_article,null) : ResourcesCompat.getDrawable(getResources(),R.drawable.ic_article_outline,null));
//                item.setIcon(R.drawable.ic_article);
                break;
            case R.id.evaluation:
                item.setChecked(itemState);
                item.setIcon(itemState ? ResourcesCompat.getDrawable(getResources(),R.drawable.ic_person_search,null) : ResourcesCompat.getDrawable(getResources(),R.drawable.ic_person_search_outline,null));
//                item.setIcon(R.drawable.ic_person_search);
                break;
            case R.id.accounts:
                item.setChecked(itemState);
                item.setIcon(itemState ? ResourcesCompat.getDrawable(getResources(),R.drawable.ic_account_wallet,null) : ResourcesCompat.getDrawable(getResources(),R.drawable.ic_account_wallet_outline,null));
//                item.setIcon(R.drawable.ic_account_wallet);
                break;
        }

    }*/
}