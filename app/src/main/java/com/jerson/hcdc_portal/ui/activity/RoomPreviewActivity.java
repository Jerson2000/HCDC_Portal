package com.jerson.hcdc_portal.ui.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.jerson.hcdc_portal.PortalApp;
import com.jerson.hcdc_portal.databinding.ActivityRoomPreviewBinding;
import com.jerson.hcdc_portal.model.RoomModel;
import com.jerson.hcdc_portal.ui.adapter.RoomPreviewAdapter;

import java.util.ArrayList;
import java.util.List;

public class RoomPreviewActivity extends AppCompatActivity {
    private ActivityRoomPreviewBinding binding;
    private List<RoomModel.previews> imgList = new ArrayList<>();
    private int pos;
    private RoomPreviewAdapter adapter;
    private final String TAG = RoomPreviewActivity.class.toString();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRoomPreviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Bundle bundle = getIntent().getExtras();

        imgList = (List<RoomModel.previews>) PortalApp.getSerializable(bundle, "previews", RoomModel.previews.class);
        pos = bundle.getInt("pos");

        adapter = new RoomPreviewAdapter(imgList);
        binding.viewPager.setAdapter(adapter);
        binding.viewPager.setCurrentItem(pos);

        binding.previewClose.setOnClickListener(v -> {
            finish();
        });


    }
}