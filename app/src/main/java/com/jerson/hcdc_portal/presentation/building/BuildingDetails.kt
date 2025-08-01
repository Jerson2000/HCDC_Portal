package com.jerson.hcdc_portal.presentation.building

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import coil.ImageLoader
import coil.load
import com.jerson.hcdc_portal.R
import com.jerson.hcdc_portal.databinding.ActivityBuildingDetailsBinding
import com.jerson.hcdc_portal.domain.model.Building
import com.jerson.hcdc_portal.domain.model.Rooms
import com.jerson.hcdc_portal.presentation.building.adapter.BuildingDetailsAdapter
import com.jerson.hcdc_portal.presentation.building.adapter.RoomAdapter
import com.jerson.hcdc_portal.util.Constants
import com.jerson.hcdc_portal.util.getParcelableCompat


class BuildingDetails: AppCompatActivity() {
    private lateinit var binding:ActivityBuildingDetailsBinding
    private lateinit var adapter:BuildingDetailsAdapter
    private lateinit var roomsAdapter:RoomAdapter
    private var building:Building?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBuildingDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        building = intent.extras?.getParcelableCompat("building")
        building?.let {
            setViews(it)
        }

        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

    }

    private fun setViews(x: Building){
        val imgLoader = ImageLoader.Builder(this).allowHardware(false).build()
        val rooms = mutableListOf<Rooms>()

        adapter = BuildingDetailsAdapter(x.floors){
            if (it.rooms.size > 0){
                binding.roomsLayout.visibility = View.VISIBLE
                rooms.clear()
                rooms.addAll(it.rooms)
                roomsAdapter.notifyDataSetChanged()
                binding.nestedScrollBuilding.post {
                    binding.nestedScrollBuilding.fullScroll(View.FOCUS_DOWN)
                }
            }
            else binding.roomsLayout.visibility = View.GONE

        }
        roomsAdapter = RoomAdapter(rooms) {
            val imageList = arrayListOf<String>()
            for (z in it.preview)
                imageList.add(z.img_path)

            startActivity(Intent(this, ImageViewer::class.java).putExtra("imgList", imageList).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION))
        }
        binding.roomsRecyclerView.adapter = roomsAdapter

        binding.apply {
            recyclerView.adapter = adapter
            bName.text = x.name
            bHistory.text = x.history
            bLocated.text =x.located
            bFloorUpTo.text = x.floor_desc
            bImage.load("${Constants.githubContent}${x.img_path}",imgLoader)
        }

        binding.bImage.setOnClickListener{
            val imageList = arrayListOf<String>()
            imageList.add(x.img_path)
            startActivity(Intent(this, ImageViewer::class.java).putExtra("imgList", imageList).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION))
        }
    }


}