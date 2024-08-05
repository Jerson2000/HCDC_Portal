package com.jerson.hcdc_portal.presentation.subjects

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jerson.hcdc_portal.databinding.ActivitySubjectDetailsKtBinding
import com.jerson.hcdc_portal.domain.model.Schedule
import com.jerson.hcdc_portal.util.extractBuilding
import com.jerson.hcdc_portal.util.getParcelableCompat
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SubjectDetailsKt:AppCompatActivity() {

    private lateinit var binding:ActivitySubjectDetailsKtBinding
    private var subject:Schedule?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubjectDetailsKtBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.header.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            binding.header.collapsingToolbar.title = "Subject"
            binding.header.collapsingToolbar.subtitle = "Subject Information"
        }

        subject = intent.extras?.getParcelableCompat("subject")
        subject?.let{
            setupViews(it)
        }

        binding.header.toolbar.setNavigationOnClickListener {
           onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupViews(subject:Schedule){
        binding.apply {
            subjCode.text = subject.subjectCode
            subjDesc.text = subject.description
            offerNo.text = subject.offeredNo
            schedule.text = "${subject.time} - ${subject.days}"
            room.text = if (subject.room.isNullOrEmpty()) "TBD" else subject.room
            floor.text = if(!subject.room.isNullOrEmpty() && extractFirstNumber(subject.room)> 0) "Floor: ${extractFirstNumber(subject.room).toString()[0]}" else "Floor"
            building.text = if (!subject.room.isNullOrEmpty()) extractBuilding(subject.room) else "Building"
        }
    }

    private fun extractFirstNumber(str: String): Int {
        val regex = "\\d+".toRegex()
        val matchResult = regex.find(str)
        return matchResult?.value?.toInt() ?: 0
    }

}