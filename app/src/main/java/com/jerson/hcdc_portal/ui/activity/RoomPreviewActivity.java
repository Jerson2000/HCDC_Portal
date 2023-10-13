package com.jerson.hcdc_portal.ui.activity;

import android.os.Bundle;
import android.view.LayoutInflater;

import com.jerson.hcdc_portal.databinding.ActivityRoomPreviewBinding;
import com.jerson.hcdc_portal.model.RoomModel;
import com.jerson.hcdc_portal.ui.adapter.RoomPreviewAdapter;
import com.jerson.hcdc_portal.util.BaseActivity;

import java.util.ArrayList;
import java.util.List;

public class RoomPreviewActivity extends BaseActivity<ActivityRoomPreviewBinding> {
    private ActivityRoomPreviewBinding binding;
    private List<RoomModel.previews> imgList = new ArrayList<>();
    private int pos;
    private RoomPreviewAdapter adapter;
    private final String TAG = RoomPreviewActivity.class.toString();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = getBinding();

        Bundle bundle = getIntent().getExtras();

        imgList = (List<RoomModel.previews>) bundle.getSerializable("previews");
        pos = bundle.getInt("pos");

        adapter = new RoomPreviewAdapter(imgList);
        binding.viewPager.setAdapter(adapter);
        binding.viewPager.setCurrentItem(pos);

        binding.previewClose.setOnClickListener(v -> {
            finish();
        });


    }

    @Override
    protected ActivityRoomPreviewBinding createBinding(LayoutInflater layoutInflater) {
        return ActivityRoomPreviewBinding.inflate(layoutInflater);
    }
}