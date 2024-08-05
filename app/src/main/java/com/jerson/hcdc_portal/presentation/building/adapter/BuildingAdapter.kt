package com.jerson.hcdc_portal.presentation.building.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.jerson.hcdc_portal.databinding.ItemContainerBuildingBinding
import com.jerson.hcdc_portal.domain.model.Building
import com.jerson.hcdc_portal.util.Constants

class BuildingAdapter(private val list:List<Building>):RecyclerView.Adapter<BuildingAdapter.ViewHolder>(){
    inner class ViewHolder(private val binding: ItemContainerBuildingBinding):RecyclerView.ViewHolder(binding.root){
        fun bind(item:Building){
            binding.buildingName.text = item.name
            binding.buildingImage.load("${Constants.githubContent}${item.img_path}")
            println("HUHU ${Constants.githubContent}${item.img_path}")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemContainerBuildingBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.bind(item)
    }
}