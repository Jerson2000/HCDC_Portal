package com.jerson.hcdc_portal.ui.adapter;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.jerson.hcdc_portal.databinding.ItemContainerRoomPreviewBinding;
import com.jerson.hcdc_portal.model.RoomModel;
import com.jerson.hcdc_portal.util.GlideApp;

import java.util.List;

public class RoomPreviewAdapter extends PagerAdapter {
    private List<RoomModel.previews> imgList;

    public RoomPreviewAdapter(List<RoomModel.previews> imgList) {
        this.imgList = imgList;
    }

    @Override
    public int getCount() {
        return imgList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        ItemContainerRoomPreviewBinding binding = ItemContainerRoomPreviewBinding.inflate(LayoutInflater.from(container.getContext()), container, false);

        SubsamplingScaleImageView img = binding.roomView;

        GlideApp.with(img)
                .asBitmap()
                .load(imgList.get(position).getImg())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {

                        if (!resource.isRecycled()) {
                            img.setImage(ImageSource.bitmap(resource));
                        } else resource.recycle();


                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });

        container.addView(binding.getRoot());

        return binding.getRoot();
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((RelativeLayout) object);
    }
}
