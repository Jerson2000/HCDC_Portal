package com.jerson.hcdc_portal.util

import android.app.Dialog
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.load
import coil.size.Scale
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jerson.hcdc_portal.R
import com.jerson.hcdc_portal.databinding.DialogCustomProfileBinding
import com.jerson.hcdc_portal.databinding.ItemCustomProfileBinding
import com.jerson.hcdc_portal.util.Constants.KEY_CUSTOM_PROFILE_VALUE
import com.jerson.hcdc_portal.util.Constants.KEY_IS_CUSTOM_PROFILE
import javax.inject.Inject

class CustomProfileAdapter(
    private val list: List<Int>,
    private val onItemSelected: (Int) -> Unit
) : RecyclerView.Adapter<CustomProfileAdapter.ViewHolder>() {

    private var selectedPosition = RecyclerView.NO_POSITION

    init {
        setHasStableIds(true)
    }

    inner class ViewHolder(private val binding: ItemCustomProfileBinding) : RecyclerView.ViewHolder(binding.root) {
        private val imgLoader by lazy {
            ImageLoader.Builder(binding.root.context).allowHardware(false).build()
        }

        fun bind(item: Int, isSelected: Boolean) {
            binding.imageView.load(item, imgLoader) {
                size(200, 200)
            }

            binding.layout.setBackgroundColor(
                if (isSelected) Color.GREEN else Color.TRANSPARENT
            )

            binding.root.setOnClickListener {
                val previousPosition = selectedPosition
                selectedPosition = adapterPosition
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)
                onItemSelected(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemCustomProfileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position], position == selectedPosition)
    }

    override fun getItemId(position: Int): Long = list[position].toLong()
}

class CustomProfileDialog(
    private val pref: AppPreference,
    private val callbackItem: (Any) -> Unit
) : DialogFragment() {

    private lateinit var binding: DialogCustomProfileBinding
    private var selected: Int? = null
    private val list = listOf(
        R.drawable.profile_male_1,
        R.drawable.profile_male_2,
        R.drawable.profile_male_3,
        R.drawable.profile_female_1,
        R.drawable.profile_female_2,
        R.drawable.profile_female_3
    )

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogCustomProfileBinding.inflate(layoutInflater)

        val adapter = CustomProfileAdapter(list) {
            selected = it
        }
        binding.recyclerView.adapter = adapter

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle("Select Profile Picture")
            .setView(binding.root)
            .setPositiveButton("OK") { dialog, _ ->
                selected?.let {
                    callbackItem(it)
                    pref.setBooleanPreference(KEY_IS_CUSTOM_PROFILE, true)
                    pref.setIntPreference(KEY_CUSTOM_PROFILE_VALUE, it)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
            .setNeutralButton("Reset") { dialog, _ ->
                pref.setBooleanPreference(KEY_IS_CUSTOM_PROFILE, false)
                callbackItem(userAvatar(pref))
                dialog.dismiss()
            }
            .create()
    }
}

