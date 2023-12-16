package com.jerson.hcdc_portal.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jerson.hcdc_portal.databinding.ItemContainerBuildingFloorBinding;
import com.jerson.hcdc_portal.listener.OnClickListener;
import com.jerson.hcdc_portal.model.BuildingModel;

import java.util.List;

public class BuildingDetailsAdapter extends RecyclerView.Adapter<BuildingDetailsAdapter.ViewHolder> {
    private List<BuildingModel.Floor> list;
    private OnClickListener<Integer> listener;

    public BuildingDetailsAdapter(List<BuildingModel.Floor> list, OnClickListener<Integer> listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BuildingDetailsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemContainerBuildingFloorBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull BuildingDetailsAdapter.ViewHolder holder, int position) {
        BuildingModel.Floor item = list.get(position);
        holder.binding.floor.setText(getFloorText(item.getFloor_no()));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemContainerBuildingFloorBinding binding;

        public ViewHolder(@NonNull ItemContainerBuildingFloorBinding itemView) {
            super(itemView.getRoot());
            this.binding = itemView;

            binding.getRoot().setOnClickListener(v -> {
                listener.onItemClick(getAdapterPosition());
            });
        }
    }

    String getFloorText(int i) {
        String s;
        switch (i) {
            case 1:
                s = "1st";
                break;
            case 2:
                s = "2nd";
                break;
            case 3:
                s = "3rd";
                break;
            default:
                s = i + "th";
        }

        return s;
    }
}
