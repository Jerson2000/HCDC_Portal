package com.jerson.hcdc_portal.ui.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.jerson.hcdc_portal.PortalApp;
import com.jerson.hcdc_portal.R;
import com.jerson.hcdc_portal.databinding.ActivityBuildingBinding;
import com.jerson.hcdc_portal.listener.OnClickListener;
import com.jerson.hcdc_portal.model.BuildingModel;
import com.jerson.hcdc_portal.ui.adapter.BuildingAdapter;
import com.jerson.hcdc_portal.util.BaseActivity;
import com.jerson.hcdc_portal.viewmodel.RoomViewModel;

import java.util.ArrayList;
import java.util.List;

public class BuildingActivity extends BaseActivity<ActivityBuildingBinding> implements OnClickListener<Integer> {
    private static final String TAG = "BuildingActivity";
    private BuildingAdapter adapter;
    private List<BuildingModel> list = new ArrayList<>();
    private RoomViewModel roomViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(!getBindingNull()) init();
    }

    void init(){

        setSupportActionBar(getBinding().header.toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            getBinding().header.collapsingToolbar.setTitle("Building");
            getBinding().header.collapsingToolbar.setSubtitle("Buildings details");
        }

        roomViewModel = new ViewModelProvider(this).get(RoomViewModel.class);
        adapter = new BuildingAdapter(list,this);
        getBinding().recyclerView.setLayoutManager(new GridLayoutManager(this,2));
        getBinding().recyclerView.setAdapter(adapter);
        getBuilding();
        showErr();
    }

    void getBuilding(){
        roomViewModel.getBuilding().observe(this,data->{
            if(data!=null){
                System.out.println(data.getBuilding().get(0).getHistory());
                list.addAll(data.getBuilding());
                list.size();
                adapter.notifyDataSetChanged();
            }
        });
    }

    void showErr(){
        roomViewModel.getErr().observe(this,err-> PortalApp.showToast(err.getMessage()));
    }

    @Override
    protected ActivityBuildingBinding createBinding(LayoutInflater layoutInflater) {
        return ActivityBuildingBinding.inflate(layoutInflater);
    }

    @Override
    public void onItemClick(Integer object) {
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}