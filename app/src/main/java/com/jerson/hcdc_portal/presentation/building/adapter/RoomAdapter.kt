package com.jerson.hcdc_portal.presentation.building.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.load
import com.jerson.hcdc_portal.databinding.ItemContainerRoomBinding
import com.jerson.hcdc_portal.databinding.ItemRoomsBinding
import com.jerson.hcdc_portal.domain.model.Rooms
import com.jerson.hcdc_portal.util.Constants

class RoomAdapter(private val list:List<Rooms>,private val itemCallback:(Rooms)->Unit):RecyclerView.Adapter<RoomAdapter.ViewHolder>(){
    inner class ViewHolder(private val binding:ItemRoomsBinding):RecyclerView.ViewHolder(binding.root){
        private val imgLoader = ImageLoader.Builder(binding.root.context).allowHardware(false).build()
        fun bind(item:Rooms){
            if(item.preview.isNotEmpty()){
                binding.roomIV.load("${Constants.githubContent}${item.preview[0].img_path}",imgLoader)
                binding.roomDesc.text = item.id
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemRoomsBinding.inflate(
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