package com.jerson.hcdc_portal.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jerson.hcdc_portal.R;
import com.jerson.hcdc_portal.databinding.ItemContainerDashboardBinding;
import com.jerson.hcdc_portal.model.DashboardModel;
import com.squareup.picasso.Picasso;

import java.util.List;

public class DashboardAdapter extends RecyclerView.Adapter<DashboardAdapter.ViewHolder> {

    private Context context;
    private List<DashboardModel> list;

    public DashboardAdapter(Context context, List<DashboardModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public DashboardAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemContainerDashboardBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull DashboardAdapter.ViewHolder holder, int position) {
        DashboardModel model = list.get(position);

        holder.binding.desc.setText(model.getDescription());
        holder.binding.subjCode.setText(model.getSubjCode());
        Picasso.get().load(R.drawable.v2_book).into(holder.binding.bookIV);

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ItemContainerDashboardBinding binding;
        public ViewHolder(ItemContainerDashboardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
