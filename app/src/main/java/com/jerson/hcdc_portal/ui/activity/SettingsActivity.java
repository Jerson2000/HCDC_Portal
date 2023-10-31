package com.jerson.hcdc_portal.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.ViewModelProvider;

import com.jerson.hcdc_portal.PortalApp;
import com.jerson.hcdc_portal.database.DatabasePortal;
import com.jerson.hcdc_portal.databinding.ActivitySettingsBinding;
import com.jerson.hcdc_portal.listener.DynamicListener;
import com.jerson.hcdc_portal.ui.MainActivity;
import com.jerson.hcdc_portal.util.BaseActivity;
import com.jerson.hcdc_portal.util.Dialog;
import com.jerson.hcdc_portal.util.PreferenceManager;
import com.jerson.hcdc_portal.viewmodel.AccountViewModel;
import com.jerson.hcdc_portal.viewmodel.DashboardViewModel;
import com.jerson.hcdc_portal.viewmodel.EnrollHistoryViewModel;
import com.jerson.hcdc_portal.viewmodel.GradesViewModel;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class SettingsActivity extends BaseActivity<ActivitySettingsBinding> {
    private static final String TAG = "SettingsActivity";
    private ActivitySettingsBinding binding;
    private PreferenceManager preferenceManager;
    private DashboardViewModel dashboardViewModel;
    private EnrollHistoryViewModel enrollHistoryViewModel;
    private AccountViewModel accountViewModel;
    private GradesViewModel gradesViewModel;
    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = getBinding();

        preferenceManager = new PreferenceManager(this);
        dashboardViewModel = new ViewModelProvider(this).get(DashboardViewModel.class);
        enrollHistoryViewModel = new ViewModelProvider(this).get(EnrollHistoryViewModel.class);
        accountViewModel = new ViewModelProvider(this).get(AccountViewModel.class);
        gradesViewModel = new ViewModelProvider(this).get(GradesViewModel.class);

        init();

    }

    void init() {
        setSupportActionBar(getBinding().header.toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            getBinding().header.collapsingToolbar.setTitle("Settings");
            getBinding().header.collapsingToolbar.setSubtitle("");
        }
        binding.themeOption.setOnClickListener(v -> {
            binding.themeModeView.setVisibility(getPolarVisibility(binding.themeModeView));
        });

        binding.autoChip.setOnClickListener(v -> {
            setAppTheme(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            binding.themeModeView.setVisibility(getPolarVisibility(binding.themeModeView));

        });

        binding.nightChip.setOnClickListener(v -> {
            setAppTheme(AppCompatDelegate.MODE_NIGHT_YES);
            binding.themeModeView.setVisibility(getPolarVisibility(binding.themeModeView));

        });

        binding.lightChip.setOnClickListener(v -> {
            setAppTheme(AppCompatDelegate.MODE_NIGHT_NO);
            binding.themeModeView.setVisibility(getPolarVisibility(binding.themeModeView));

        });

        binding.btnLogout.setOnClickListener(v -> {
            Dialog.Dialog("WARNING!", "Are you sure you want to logout?", this)
                    .setPositiveButton("Yes", (dialogInterface, which) -> {
                        clear();
                        ProgressBar progressBar = new ProgressBar(this);
                        progressBar.setIndeterminate(true);
                        dialog = Dialog.CustomDialog("Logging out...",this,progressBar)
                                        .show();
                        dialog.setCancelable(false);
                        dialog.setCanceledOnTouchOutside(false);
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss()).show();
        });

        binding.github.setOnClickListener(v->{
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(PortalApp.github)));
        });
    }


    int getPolarVisibility(View view) {
        int mode = view.getVisibility();
        return mode == View.VISIBLE ? View.GONE : View.VISIBLE;
    }

    void setAppTheme(int theme){
        AppCompatDelegate.setDefaultNightMode(theme);
        preferenceManager.putInteger(PortalApp.KEY_SETTINGS_THEME_MODE,theme);
    }

    /* database */

    void clear() {
        deleteSubjects(object -> {
            if (object) {
                deleteGradeLink(delGL -> {
                    if (delGL) {
                        deleteGrade(delG -> {
                            if (delG) {
                                deleteEnrollHistoryLinkData(delEnL -> {
                                    if (delEnL) {
                                        deleteEnrollHistoryData(delEn -> {
                                            if (delEn) {
                                                deleteAccount(delAc -> {
                                                    if (delAc) {
                                                        preferenceManager.putBoolean(PortalApp.KEY_IS_LOGIN,false);
                                                        dialog.dismiss();
                                                        startActivity(new Intent(SettingsActivity.this, LoginActivity.class));
                                                        finish();
                                                    }
                                                });
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
    }

    void deleteAccount(DynamicListener<Boolean> listener) {
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(accountViewModel.deleteAccount()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    listener.dynamicListener(true);
                }, throwable -> {
                    listener.dynamicListener(false);
                })

        );
    }

    void deleteSubjects(DynamicListener<Boolean> isDeleted) {
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(dashboardViewModel.deleteDashboardData()
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    isDeleted.dynamicListener(true);
                }, throwable -> {
                    isDeleted.dynamicListener(false);
                })
        );
    }

    void deleteEnrollHistoryLinkData(DynamicListener<Boolean> isDeleted) {
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(enrollHistoryViewModel.deleteEnrollHistoryLinkData()
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    isDeleted.dynamicListener(true);
                }, throwable -> {
                    isDeleted.dynamicListener(false);
                })
        );
    }

    void deleteEnrollHistoryData(DynamicListener<Boolean> isDeleted) {
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(enrollHistoryViewModel.deleteAllEnrollHistoryData()
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    isDeleted.dynamicListener(true);
                }, throwable -> {
                    isDeleted.dynamicListener(false);
                })
        );
    }

    void deleteGradeLink(DynamicListener<Boolean> isDeleted) {
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(gradesViewModel.deleteGradeLink()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> isDeleted.dynamicListener(true), throwable -> {
                    isDeleted.dynamicListener(false);
                }));
    }

    void deleteGrade(DynamicListener<Boolean> listener) {
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(gradesViewModel.deleteAllGradeData()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    listener.dynamicListener(true);
                }, throwable -> {
                    listener.dynamicListener(false);
                })
        );
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected ActivitySettingsBinding createBinding(LayoutInflater layoutInflater) {
        return ActivitySettingsBinding.inflate(layoutInflater);
    }

}