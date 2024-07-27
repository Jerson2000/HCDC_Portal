package com.jerson.hcdc_portal.util

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jerson.hcdc_portal.R
import com.jerson.hcdc_portal.databinding.DialogCustomProfileBinding
import com.jerson.hcdc_portal.databinding.ItemCustomProfileBinding
import com.jerson.hcdc_portal.util.Constants.KEY_CUSTOM_PROFILE_VALUE
import com.jerson.hcdc_portal.util.Constants.KEY_IS_CUSTOM_PROFILE
import javax.inject.Inject

class CustomProfileAdapter(private val list:List<Int>,private val callbackItem:(Int)-> Unit):RecyclerView.Adapter<CustomProfileAdapter.ViewHolder>(){
    private var prevSelectedPos = -1
    inner class ViewHolder(private val binding:ItemCustomProfileBinding):RecyclerView.ViewHolder(binding.root){
        fun bind(item:Int,pos:Int){
            binding.imageView.load(item)
            binding.imageView.setOnClickListener{
                itemSelectedIndicator(binding.layout,pos)
                callbackItem(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemCustomProfileBinding.inflate(
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
        holder.itemView.setBackgroundColor(
            if (position == prevSelectedPos) Color.GREEN else Color.TRANSPARENT
        )
    }
    private fun itemSelectedIndicator(view: View, curPos: Int) {
        if (prevSelectedPos != RecyclerView.NO_POSITION) {
            notifyItemChanged(prevSelectedPos)
        }
        prevSelectedPos = curPos
        notifyItemChanged(curPos)
        view.setBackgroundColor(Color.GREEN)
    }

}

class CustomProfileDialog(private val pref:AppPreference,private val callbackItem: (Any) -> Unit) : DialogFragment() {
    private lateinit var adapter: CustomProfileAdapter
    private lateinit var binding: DialogCustomProfileBinding
    private val list = mutableListOf<Int>()
    private var selected:Int?=null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogCustomProfileBinding.inflate(layoutInflater)

        adapter = CustomProfileAdapter(list) {
            selected = it
        }
        binding.recyclerView.adapter = adapter

        list.add(R.drawable.profile_male_1)
        list.add(R.drawable.profile_male_2)
        list.add(R.drawable.profile_male_3)
        list.add(R.drawable.profile_female_1)
        list.add(R.drawable.profile_female_2)
        list.add(R.drawable.profile_female_3)
        adapter.notifyDataSetChanged()

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle("Select Profile Picture")
            .setView(binding.root)
            .setPositiveButton("OK") { dialog, _ ->
                selected?.let { callbackItem(it) }
                if(selected!=null){
                    pref.setBooleanPreference(KEY_IS_CUSTOM_PROFILE,true)
                    pref.setIntPreference(KEY_CUSTOM_PROFILE_VALUE, selected!!)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
            .setNeutralButton("Reset"){dialog,_->
                pref.setBooleanPreference(KEY_IS_CUSTOM_PROFILE,false)
                callbackItem(userAvatar(pref))
                dialog.dismiss()
            }
            .create()
    }
}

