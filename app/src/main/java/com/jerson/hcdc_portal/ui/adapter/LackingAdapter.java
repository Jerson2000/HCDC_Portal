package com.jerson.hcdc_portal.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jerson.hcdc_portal.databinding.ItemContainerLackingBinding;

import java.util.List;

public class LackingAdapter extends RecyclerView.Adapter<LackingAdapter.ViewHolder> {

    private List<String> list;

    public LackingAdapter(List<String> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public LackingAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemContainerLackingBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull LackingAdapter.ViewHolder holder, int position) {
        holder.binding.lack.setText(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ItemContainerLackingBinding binding;

        public ViewHolder(@NonNull ItemContainerLackingBinding itemView) {
            super(itemView.getRoot());
            this.binding = itemView;
        }
    }
}
