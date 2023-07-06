package com.jerson.hcdc_portal.ui.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
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
import com.jerson.hcdc_portal.util.JustGlideModule;
import com.jerson.hcdc_portal.viewmodel.RoomViewModel;

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
                        imageList.addAll(s.getPreviews());
                        adapter.notifyDataSetChanged();
                    }
                }
            }

        });
    }

    void setViews() {
        binding.subjDesc.setText(subjectData.getDescription());
        binding.offerNo.setText(subjectData.getOfferNo());
        binding.subjCode.setText(subjectData.getSubjCode());
        binding.schedule.setText(subjectData.getDays().concat(" - ").concat(subjectData.getTime()));
        binding.room.setText(subjectData.getRoom().replace("-", ""));

    }


    @Override
    public void dynamicListener(Integer object) {
        ViewGroup parentView = (ViewGroup) imagePreviewBinding.getRoot().getParent();
        if (parentView != null) {
            parentView.removeView(imagePreviewBinding.getRoot());
        }

        dialog = Dialog.CustomDialog("", this, imagePreviewBinding.getRoot())
                .show();

        GlideApp.with(imagePreviewBinding.getRoot())
                .asBitmap()
                .load(imageList.get(object).getImg())
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {

                        if(!resource.isRecycled()){
                            imagePreviewBinding.roomIV.setImage(ImageSource.bitmap(resource));
                        }else resource.recycle();


                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });
        Toast.makeText(this, Glide.get(this).getBitmapPool().toString(), Toast.LENGTH_SHORT).show();

    }





    @Override
    protected void onDestroy() {
        super.onDestroy();
        /*Glide.with(imagePreviewBinding.getRoot()).clear(imagePreviewBinding.roomIV);
        Glide.get(getApplicationContext()).getBitmapPool().clearMemory();*/
    }
}