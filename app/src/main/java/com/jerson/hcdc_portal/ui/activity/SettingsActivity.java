package com.jerson.hcdc_portal.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.ViewModelProvider;

import com.jerson.hcdc_portal.PortalApp;
import com.jerson.hcdc_portal.database.DatabasePortal;
import com.jerson.hcdc_portal.databinding.ActivitySettingsBinding;
import com.jerson.hcdc_portal.listener.DynamicListener;
import com.jerson.hcdc_portal.ui.MainActivity;
import com.jerson.hcdc_portal.util.Dialog;
import com.jerson.hcdc_portal.util.PreferenceManager;
import com.jerson.hcdc_portal.viewmodel.AccountViewModel;
import com.jerson.hcdc_portal.viewmodel.DashboardViewModel;
import com.jerson.hcdc_portal.viewmodel.EnrollHistoryViewModel;
import com.jerson.hcdc_portal.viewmodel.GradesViewModel;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class SettingsActivity extends AppCompatActivity {
    private static final String TAG = "SettingsActivity";
    private ActivitySettingsBinding binding;
    private PreferenceManager preferenceManager;
    private DashboardViewModel dashboardViewModel;
    private EnrollHistoryViewModel enrollHistoryViewModel;
    private AccountViewModel accountViewModel;
    private GradesViewModel gradesViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(this);
        dashboardViewModel = new ViewModelProvider(this).get(DashboardViewModel.class);
        enrollHistoryViewModel = new ViewModelProvider(this).get(EnrollHistoryViewModel.class);
        accountViewModel = new ViewModelProvider(this).get(AccountViewModel.class);
        gradesViewModel = new ViewModelProvider(this).get(GradesViewModel.class);

        init();

    }

    void init() {
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
                    .setPositiveButton("Yes", (dialog, which) -> {
                        clear();
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss()).show();
        });
        binding.btnBack.setOnClickListener(v->onBackPressed());
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
                deleteGradeLink(new DynamicListener<Boolean>() {
                    @Override
                    public void dynamicListener(Boolean object) {
                        if (object) {
                            deleteGrade(new DynamicListener<Boolean>() {
                                @Override
                                public void dynamicListener(Boolean object) {
                                    if (object) {
                                        deleteEnrollHistoryLinkData(new DynamicListener<Boolean>() {
                                            @Override
                                            public void dynamicListener(Boolean object) {
                                                if (object) {
                                                    deleteEnrollHistoryData(new DynamicListener<Boolean>() {
                                                        @Override
                                                        public void dynamicListener(Boolean object) {
                                                            if (object) {
                                                                deleteAccount(new DynamicListener<Boolean>() {
                                                                    @Override
                                                                    public void dynamicListener(Boolean object) {
                                                                        if (object) {
                                                                            preferenceManager.putBoolean(PortalApp.KEY_IS_LOGIN,false);
                                                                            startActivity(new Intent(SettingsActivity.this, LoginActivity.class));
                                                                            finish();
                                                                        }
                                                                    }
                                                                });
                                                            }
                                                        }
                                                    });
                                                }
                                            }
                                        });
                                    }
                                }
                            });
                        }
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
                    /*Log.d(TAG, "deleteAccount: success");*/
                    listener.dynamicListener(true);
                }, throwable -> {
                    Log.e(TAG, "deleteAccount: ", throwable);
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
                    Log.d(TAG, "deleteSubjects: success");
                }, throwable -> {
                    isDeleted.dynamicListener(false);
                    Log.e(TAG, "deleteSubjects: ", throwable);
                })
        );
    }

    void deleteEnrollHistoryLinkData(DynamicListener<Boolean> isDeleted) {
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(enrollHistoryViewModel.deleteEnrollHistoryLinkData()
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    isDeleted.dynamicListener(true);
                    Log.d(TAG, "deleteEnrollHistoryLinkData: success");
                }, throwable -> {
                    isDeleted.dynamicListener(false);
                    Log.e(TAG, "deleteEnrollHistoryLinkData: ", throwable);
                })
        );
    }

    void deleteEnrollHistoryData(DynamicListener<Boolean> isDeleted) {
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(enrollHistoryViewModel.deleteAllEnrollHistoryData()
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    isDeleted.dynamicListener(true);
                    Log.d(TAG, "deleteEnrollHistoryData: success");
                }, throwable -> {
                    isDeleted.dynamicListener(false);
                    Log.e(TAG, "deleteEnrollHistoryData: ", throwable);
                })
        );
    }

    void deleteGradeLink(DynamicListener<Boolean> isDeleted) {
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(gradesViewModel.deleteGradeLink()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> isDeleted.dynamicListener(true), throwable -> {
                    Log.e(TAG, "deleteGradeLink: ", throwable);
                    isDeleted.dynamicListener(false);
                }));
    }

    void deleteGrade(DynamicListener<Boolean> listener) {
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(gradesViewModel.deleteAllGradeData()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    /*Log.e(TAG, "deleteGrade: success");*/
                    listener.dynamicListener(true);
                }, throwable -> {
                    Log.e(TAG, "deleteGrade: ", throwable);
                    listener.dynamicListener(false);
                })
        );
    }

}