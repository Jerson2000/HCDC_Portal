package com.jerson.hcdc_portal.ui.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jerson.hcdc_portal.PortalApp;
import com.jerson.hcdc_portal.R;
import com.jerson.hcdc_portal.databinding.ActivitySubjectOfferedBinding;
import com.jerson.hcdc_portal.listener.OnClickListener;
import com.jerson.hcdc_portal.model.SubjectOfferedModel;
import com.jerson.hcdc_portal.ui.adapter.SubjectOfferedAdapter;
import com.jerson.hcdc_portal.util.BaseActivity;
import com.jerson.hcdc_portal.util.Dialog;
import com.jerson.hcdc_portal.viewmodel.SubjectOfferedViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SubjectOfferedActivity extends BaseActivity<ActivitySubjectOfferedBinding> implements OnClickListener<Integer> {
    private static final String TAG = "SubjectOfferedActivity";
    private SubjectOfferedViewModel subjectOfferedViewModel;
    private List<SubjectOfferedModel.SubjectOffered> offeredList = new ArrayList<>();
    private SubjectOfferedAdapter adapter;
    private int page = 1;
    private int totalPages = 148;
    private AlertDialog dialog;
    private boolean isSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!getBindingNull()) {
            init();
        }
    }

    void init() {
        setSupportActionBar(getBinding().header.toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            getBinding().header.collapsingToolbar.setTitle("Subjects Offered");
            getBinding().header.collapsingToolbar.setSubtitle("Subjects offered for all courses");
        }

        subjectOfferedViewModel = new ViewModelProvider(this).get(SubjectOfferedViewModel.class);
        adapter = new SubjectOfferedAdapter(offeredList, this);
        getBinding().recyclerView.setLayoutManager(new LinearLayoutManager(this));
        getBinding().recyclerView.setAdapter(adapter);

        getBinding().recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int visibleItemCount, totalItemCount, firstVisibleItemPosition;
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                assert layoutManager != null;
                visibleItemCount = layoutManager.getChildCount();
                totalItemCount = layoutManager.getItemCount();
                firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                if (!recyclerView.canScrollVertically(1)) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount && firstVisibleItemPosition >= 0) {
                        if (page <= totalPages && !isSearch) {
                            page++;
                            getSubjectOffered();
                        }

                    }
                }


            }
        });

        getSubjectOffered();
        showErrDisplay();

    }

    void getSubjectOffered() {
        toggleLoading();
        subjectOfferedViewModel.getSubjectOffered(page).observe(this, data -> {
            if (data != null) {
                toggleLoading();
                int oldCount = offeredList.size();
                offeredList.addAll(data.getSubjectOfferedList());
                totalPages = data.getSubjectOfferedList().get(0).getTotalPage();
                adapter.notifyItemRangeInserted(oldCount, offeredList.size());
            }
        });
    }

    void searchSubjectOffered(String query) {
        toggleLoading();
        subjectOfferedViewModel.searchSubjectOffered(query).observe(this, data -> {
            if (data != null) {
                toggleLoading();
                offeredList.clear();
                offeredList.addAll(data.getSubjectOfferedList());
                totalPages = data.getSubjectOfferedList().get(0).getTotalPage();
                adapter.notifyDataSetChanged();
            }else{
                getBinding().errLayout.emoji.setText(PortalApp.SAD_EMOJIS[new Random().nextInt(6)]);
                getBinding().errLayout.response.setText("No data available.");
                getBinding().recyclerView.setVisibility(View.GONE);
            }
        });
    }

    void showErrDisplay() {
        subjectOfferedViewModel.getErr().observe(this, data -> {
            getBinding().errLayout.emoji.setText(PortalApp.SAD_EMOJIS[new Random().nextInt(6)]);
            getBinding().errLayout.response.setText(data.getMessage());
            getBinding().recyclerView.setVisibility(View.GONE);
            PortalApp.showToast(data.getMessage());
            toggleLoading();
        });
    }

    private void toggleLoading() {
        if (!getBindingNull()) {
            if (page == 1) {
                if (getBinding().progressBar.getVisibility() == View.GONE)
                    getBinding().progressBar.setVisibility(View.VISIBLE);
                else getBinding().progressBar.setVisibility(View.GONE);
            } else {
                if (getBinding().bottomProgressBar.getVisibility() == View.GONE)
                    getBinding().bottomProgressBar.setVisibility(View.VISIBLE);
                else getBinding().bottomProgressBar.setVisibility(View.GONE);
            }

        }

    }


    @Override
    public void onItemClick(Integer object) {
        SubjectOfferedModel.SubjectOffered model = offeredList.get(object);
        String s = "Offer #: " + model.getOfferedNo() + "\nCourse: " + model.getCourse() + "\n" +
                "Subject Description: " + model.getSubject() + "\n" +
                "Unit: " + model.getUnits() + "\nEnrolled: " + model.getEnrolled() + "\n" +
                "Maximum: " + model.getMaximum() + "\nSlot: " + model.getSlot();

        Dialog.Dialog("Subject Detailed", s, this).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);

        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setIconifiedByDefault(false);
        searchView.setQueryHint("Search subjects...");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!query.equals("")) {
                    searchSubjectOffered(query);
                    isSearch = true;
                    return true;
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected ActivitySubjectOfferedBinding createBinding(LayoutInflater layoutInflater) {
        return ActivitySubjectOfferedBinding.inflate(layoutInflater);
    }
}