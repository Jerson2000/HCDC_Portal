package com.jerson.hcdc_portal.ui.adapter;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
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
import com.jerson.hcdc_portal.R;
import com.jerson.hcdc_portal.databinding.ItemContainerBuildingBinding;
import com.jerson.hcdc_portal.listener.OnClickListener;
import com.jerson.hcdc_portal.model.BuildingModel;

import java.util.List;

public class BuildingAdapter extends RecyclerView.Adapter<BuildingAdapter.ViewHolder> {

    private List<BuildingModel> list;
    private OnClickListener<Integer> listener;

    public BuildingAdapter(List<BuildingModel> list, OnClickListener<Integer> listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override

    public BuildingAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemContainerBuildingBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull BuildingAdapter.ViewHolder holder, int position) {
        BuildingModel item = list.get(position);
        holder.binding.buildingName.setText(item.getName());

        Glide.with(holder.binding.buildingImage)
                .asBitmap()
                .load(item.getImage_prev())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(Glide.with(holder.binding.getRoot())
                        .asBitmap()
                        .load(R.drawable.logo)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .error(R.drawable.logo))
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        if(!resource.isRecycled()){
                            holder.binding.buildingImage.setImageBitmap(resource);
                        }else resource.recycle();
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemContainerBuildingBinding binding;

        public ViewHolder(@NonNull ItemContainerBuildingBinding itemView) {
            super(itemView.getRoot());
            this.binding = itemView;

            binding.getRoot().setOnClickListener(v->{
                listener.onItemClick(getAdapterPosition());
            });
        }
    }
}
