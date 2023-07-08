package com.jerson.hcdc_portal.ui.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.jerson.hcdc_portal.PortalApp;
import com.jerson.hcdc_portal.databinding.ActivitySubjectDetailBinding;
import com.jerson.hcdc_portal.databinding.ViewImagePreviewBinding;
import com.jerson.hcdc_portal.listener.DynamicListener;
import com.jerson.hcdc_portal.model.DashboardModel;
import com.jerson.hcdc_portal.model.RoomModel;
import com.jerson.hcdc_portal.ui.adapter.RoomAdapter;
import com.jerson.hcdc_portal.util.Dialog;
import com.jerson.hcdc_portal.util.GlideApp;
import com.jerson.hcdc_portal.viewmodel.RoomViewModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SubjectDetailActivity extends AppCompatActivity implements DynamicListener<Integer> {
    private ActivitySubjectDetailBinding binding;
    private DashboardModel subjectData;
    private RoomViewModel roomViewModel;
    private RoomAdapter adapter;
    private List<RoomModel.previews> imageList = new ArrayList<>();
    private ViewImagePreviewBinding imagePreviewBinding;
    private AlertDialog dialog;
    private final String TAG =  SubjectDetailActivity.class.toString();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySubjectDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Bundle bundle = getIntent().getExtras();
        subjectData = (DashboardModel) PortalApp.getSerializable(bundle, "subject", DashboardModel.class);
        roomViewModel = new ViewModelProvider(this).get(RoomViewModel.class);
        imagePreviewBinding = ViewImagePreviewBinding.inflate(LayoutInflater.from(this));

        init();
    }

    void init() {
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        adapter = new RoomAdapter(imageList, this);
        binding.recyclerView.setAdapter(adapter);

        binding.btnBack.setOnClickListener(v -> onBackPressed());
        setViews();
        getRooms();


    }

    void getRooms() {
        roomViewModel.getRooms().observe(this, data -> {
            if (data != null) {
                for (RoomModel.rooms s : data.getRooms()) {
                    if (s.getRoomId().trim().equals(subjectData.getRoom().replace("-", "").trim())) {
                        /*if (!s.getPreviews().equals("")) imageList.addAll(s.getPreviews());*/
                        for(RoomModel.previews model : s.getPreviews()){
                            if(!model.getImg().equals("")){
                                imageList.add(model);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                }
            }

        });
    }

    void observeRooms(){

    }

    void setViews() {
        binding.subjDesc.setText(subjectData.getDescription());
        binding.offerNo.setText(subjectData.getOfferNo());
        binding.subjCode.setText(subjectData.getSubjCode());
        binding.schedule.setText(subjectData.getDays().concat(" - ").concat(subjectData.getTime()));
        binding.room.setText(subjectData.getRoom().replace("-", ""));

    }


    @Override
    public void dynamicListener(Integer position) {

        Intent intent = new Intent(this,RoomPreviewActivity.class);
        intent.putExtra("previews", (Serializable) imageList);
        intent.putExtra("pos",position);
        startActivity(intent);

    }

}