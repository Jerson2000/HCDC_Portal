package com.jerson.hcdc_portal.presentation.building.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.load
import com.jerson.hcdc_portal.R
import com.jerson.hcdc_portal.databinding.ItemContainerBuildingBinding
import com.jerson.hcdc_portal.domain.model.Building
import com.jerson.hcdc_portal.util.Constants

class BuildingAdapter(private val list:List<Building>,private val itemCallback:(Building)->Unit):RecyclerView.Adapter<BuildingAdapter.ViewHolder>(){
    inner class ViewHolder(private val binding: ItemContainerBuildingBinding):RecyclerView.ViewHolder(binding.root){
        fun bind(item:Building){
            binding.buildingName.text = item.name
            binding.tvBuildingLocation.text = item.located.ifBlank { "Main Campus" }
            val floorCount = item.floors.size
            binding.tvBuildingFloorsCount.text = if (floorCount > 1) "$floorCount Floors" else "$floorCount Floor"
            val roomCount = item.floors.sumOf { it.rooms.size }
            binding.tvBuildingRoomsCount.text = if (roomCount > 0) "$roomCount Rooms & Labs" else item.floor_desc.ifBlank { "University Facilities" }

            val imgLoader = ImageLoader.Builder(binding.root.context).allowHardware(false).build()
            binding.buildingImage.load("${Constants.githubContent}${item.img_path}", imgLoader) {
                placeholder(R.drawable.logo)
                error(R.drawable.logo)
            }
            binding.root.setOnClickListener {
                itemCallback(item)
            }
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