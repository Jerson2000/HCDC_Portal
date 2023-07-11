package com.jerson.hcdc_portal.ui.adapter;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.jerson.hcdc_portal.R;
import com.jerson.hcdc_portal.databinding.ItemContainerRoomBinding;
import com.jerson.hcdc_portal.listener.DynamicListener;
import com.jerson.hcdc_portal.model.RoomModel;
import com.jerson.hcdc_portal.util.GlideApp;


import java.util.List;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.ViewHolder> {

    private List<RoomModel.previews> imageList;
    private DynamicListener<Integer> listener;

    public RoomAdapter(List<RoomModel.previews> imageList,DynamicListener<Integer> listener) {
        this.imageList = imageList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RoomAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      return new ViewHolder(ItemContainerRoomBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull RoomAdapter.ViewHolder holder, int position) {
        holder.binding.roomDesc.setText(imageList.get(position).getDescription());
        holder.binding.roomIV.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CENTER_INSIDE);
        holder.binding.roomIV.setDoubleTapZoomStyle(SubsamplingScaleImageView.ZOOM_FOCUS_CENTER);
        /*holder.binding.roomIV.setScaleAndCenter(holder.binding.roomIV.getMinScale(), holder.binding.roomIV.getCenter());*/
        GlideApp.with(holder.binding.roomIV)
                .asBitmap()
                .load(imageList.get(position).getImg())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(GlideApp.with(holder.binding.getRoot())
                        .asBitmap()
                        .load(R.drawable.logo)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                        .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                        .error(R.drawable.logo))
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        /*holder.binding.roomIV.setImage(ImageSource.bitmap(resource));*/
                        if(!resource.isRecycled()){
                            holder.binding.roomIV.setImage(ImageSource.bitmap(resource));
                        }else resource.recycle();
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });

    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ItemContainerRoomBinding binding;
        public ViewHolder(ItemContainerRoomBinding itemContainerRoomBinding) {
            super(itemContainerRoomBinding.getRoot());
            this.binding = itemContainerRoomBinding;

            /*binding.getRoot().setOnClickListener(v->{
                listener.dynamicListener(getAdapterPosition());
            });*/
            binding.roomIV.setOnClickListener(v->{
                listener.dynamicListener(getAdapterPosition());
            });

        }
    }
}
