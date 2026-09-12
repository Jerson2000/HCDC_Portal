package com.jerson.hcdc_portal.presentation.subjects.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jerson.hcdc_portal.databinding.ItemContainerDashboardBinding
import com.jerson.hcdc_portal.domain.model.Schedule

class SubjectAdapter(private val list:List<Schedule>,private val itemCallback:(Schedule)->Unit):RecyclerView.Adapter<SubjectAdapter.ViewHolder>() {
    inner class ViewHolder(private val binding:ItemContainerDashboardBinding):RecyclerView.ViewHolder(binding.root){
        fun bind(item: Schedule) {
            binding.apply {
                subjCode.text = item.subjectCode
                desc.text = item.description
                schedDays.text = item.days
                schedTime.text = item.time

                val room = if (item.room.isNullOrBlank()) "TBA" else item.room
                schedRoom.text = room

                // Check today status
                if (com.jerson.hcdc_portal.util.ScheduleHelper.isToday(item)) {
                    val state = com.jerson.hcdc_portal.util.ScheduleHelper.getClassState(item)
                    schedStatusBadge.visibility = android.view.View.VISIBLE
                    when (state) {
                        com.jerson.hcdc_portal.util.ClassState.CURRENT -> {
                            schedStatusBadge.text = "Now"
                            schedStatusBadge.setTextColor(androidx.core.content.ContextCompat.getColor(root.context, com.jerson.hcdc_portal.R.color.badge_passed_text))
                            schedStatusBadge.background = androidx.core.content.ContextCompat.getDrawable(root.context, com.jerson.hcdc_portal.R.drawable.bg_chip_soft)
                        }
                        com.jerson.hcdc_portal.util.ClassState.UPCOMING -> {
                            schedStatusBadge.text = "Upcoming"
                            schedStatusBadge.setTextColor(androidx.core.content.ContextCompat.getColor(root.context, com.jerson.hcdc_portal.R.color.hcdc_secondary))
                            schedStatusBadge.background = androidx.core.content.ContextCompat.getDrawable(root.context, com.jerson.hcdc_portal.R.drawable.bg_chip_soft)
                        }
                        com.jerson.hcdc_portal.util.ClassState.COMPLETED -> {
                            schedStatusBadge.text = "Done"
                            schedStatusBadge.setTextColor(androidx.core.content.ContextCompat.getColor(root.context, com.jerson.hcdc_portal.R.color.badge_neutral_text))
                            schedStatusBadge.background = androidx.core.content.ContextCompat.getDrawable(root.context, com.jerson.hcdc_portal.R.drawable.bg_chip_soft)
                        }
                        else -> {
                            schedStatusBadge.visibility = android.view.View.GONE
                        }
                    }
                } else {
                    schedStatusBadge.visibility = android.view.View.GONE
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemContainerDashboardBinding.inflate(
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
        val subject = list[position]
        holder.bind(subject)
        holder.itemView.setOnClickListener{
            itemCallback(subject)
        }
    }
}