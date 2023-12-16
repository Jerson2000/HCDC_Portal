package com.jerson.hcdc_portal.ui.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.GridLayoutManager;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.jerson.hcdc_portal.PortalApp;
import com.jerson.hcdc_portal.R;
import com.jerson.hcdc_portal.databinding.ActivityBuildingDetailsBinding;
import com.jerson.hcdc_portal.listener.OnClickListener;
import com.jerson.hcdc_portal.model.BuildingModel;
import com.jerson.hcdc_portal.model.DashboardModel;
import com.jerson.hcdc_portal.ui.adapter.BuildingDetailsAdapter;
import com.jerson.hcdc_portal.util.BaseActivity;

import java.util.ArrayList;
import java.util.List;

public class BuildingDetailsActivity extends BaseActivity<ActivityBuildingDetailsBinding> implements OnClickListener<Integer> {
    private static final String TAG = "BuildingDetailsActivity";
    private BuildingModel building;
    private List<BuildingModel.Floor> list = new ArrayList<>();
    private BuildingDetailsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(!getBindingNull()) init();
    }
    void init(){
        setSupportActionBar(getBinding().toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
        building = PortalApp.getSerializable(getIntent().getExtras(), "building", BuildingModel.class);
        adapter = new BuildingDetailsAdapter(list,this);
        getBinding().recyclerView.setLayoutManager(new GridLayoutManager(this,2));
        getBinding().recyclerView.setAdapter(adapter);
        setViews();
    }

    void setViews(){
        if(building!=null){
            Glide.with(this)
                    .asBitmap()
                    .load(building.getImage_prev())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .error(Glide.with(this)
                            .asBitmap()
                            .load(R.drawable.logo)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .error(R.drawable.logo))
                    .into(getBinding().bImage);
            getBinding().bName.setText(building.getName()+" ( "+building.getId().toUpperCase()+" )" );
            getBinding().bHistory.setText(building.getHistory());
            getBinding().bLocated.setText(building.getLocated());
            getBinding().bFloorUpTo.setText(building.getFloor_desc());

            list.addAll(building.getFloors());
        }
    }

    @Override
    public void onItemClick(Integer object) {

    }

    @Override
    protected ActivityBuildingDetailsBinding createBinding(LayoutInflater layoutInflater) {
        return ActivityBuildingDetailsBinding.inflate(layoutInflater);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


}