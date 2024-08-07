package com.jerson.hcdc_portal.presentation.building.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jerson.hcdc_portal.databinding.ItemContainerBuildingFloorBinding
import com.jerson.hcdc_portal.domain.model.Floor

class BuildingDetailsAdapter(private val list:List<Floor>,private val itemCallback:(Floor)-> Unit): RecyclerView.Adapter<BuildingDetailsAdapter.ViewHolder>() {
    private var prevSelectedPos = -1
    inner class ViewHolder(private val binding: ItemContainerBuildingFloorBinding):RecyclerView.ViewHolder(binding.root){
        fun bind(item:Floor,pos:Int){
            binding.floor.text = item.floor_no.toString()
            binding.card.setOnClickListener{
                itemSelectedIndicator(binding.viewSelected,pos)
                itemCallback(item)
            }

            binding.viewSelected.backgroundTintList =
                if (pos == prevSelectedPos) ColorStateList.valueOf(Color.GREEN)
                else ColorStateList.valueOf(Color.TRANSPARENT)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemContainerBuildingFloorBinding.inflate(
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
        holder.bind(item,position)

    }

    private fun itemSelectedIndicator(view: View, curPos: Int) {
        if (prevSelectedPos != RecyclerView.NO_POSITION) {
            notifyItemChanged(prevSelectedPos)
        }
        prevSelectedPos = curPos
        notifyItemChanged(curPos)
        view.backgroundTintList =  ColorStateList.valueOf(Color.GREEN)
    }
}