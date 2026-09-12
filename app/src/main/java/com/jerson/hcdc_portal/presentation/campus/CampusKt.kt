package com.jerson.hcdc_portal.presentation.campus

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jerson.hcdc_portal.R
import com.jerson.hcdc_portal.databinding.FragmentCampusKtBinding
import com.jerson.hcdc_portal.databinding.ItemContainerBuildingBinding
import com.jerson.hcdc_portal.databinding.ItemRoomCardBinding
import com.jerson.hcdc_portal.domain.model.Building
import com.jerson.hcdc_portal.domain.model.Rooms
import com.jerson.hcdc_portal.presentation.building.BuildingDetails
import com.jerson.hcdc_portal.presentation.building.viewmodel.RoomViewModel
import com.jerson.hcdc_portal.util.Constants
import com.jerson.hcdc_portal.util.Resource
import com.jerson.hcdc_portal.util.SnackBarKt
import coil.ImageLoader
import coil.load
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

sealed class CampusDisplayItem {
    data class BuildingItem(val building: Building) : CampusDisplayItem()
    data class RoomItem(
        val room: Rooms,
        val floorNo: Int,
        val building: Building
    ) : CampusDisplayItem()
}

@AndroidEntryPoint
class CampusKt : Fragment() {

    private var _binding: FragmentCampusKtBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RoomViewModel by viewModels()

    private val allBuildings = mutableListOf<Building>()
    private val displayItems = mutableListOf<CampusDisplayItem>()
    private lateinit var adapter: CampusAdapter
    private var currentFilterChip = "All"
    private var currentSearchQuery = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCampusKtBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = CampusAdapter(displayItems) { building ->
            startActivity(Intent(context, BuildingDetails::class.java).putExtra("building", building))
        }

        binding.campusRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.campusRecyclerView.adapter = adapter

        setupSearchAndChips()
        observeBuildings()
    }

    private fun setupSearchAndChips() {
        binding.etCampusSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentSearchQuery = s?.toString()?.trim().orEmpty()
                applyCampusFilter()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.chipGroupDestinations.setOnCheckedStateChangeListener { _, checkedIds ->
            currentFilterChip = when (checkedIds.firstOrNull()) {
                R.id.chipClassrooms -> "Classroom"
                R.id.chipLabs -> "Lab"
                R.id.chipAVR -> "AVR"
                R.id.chipOffices -> "Office"
                else -> "All"
            }
            applyCampusFilter()
        }
    }

    private fun observeBuildings() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.fetchRoom.collect { res ->
                    when (res) {
                        is Resource.Loading -> {
                            if (allBuildings.isEmpty()) {
                                _binding?.campusProgressBar?.visibility = View.VISIBLE
                            }
                        }
                        is Resource.Success -> {
                            _binding?.campusProgressBar?.visibility = View.GONE
                            res.data?.let {
                                allBuildings.clear()
                                allBuildings.addAll(it.building)
                                applyCampusFilter()
                            }
                        }
                        is Resource.Error -> {
                            _binding?.campusProgressBar?.visibility = View.GONE
                            res.message?.let { msg ->
                                _binding?.let { b -> SnackBarKt.snackBarLong(b.root, msg) }
                            }
                        }
                        else -> Unit
                    }
                }
            }
        }
    }

    private fun applyCampusFilter() {
        displayItems.clear()

        val query = currentSearchQuery
        val chip = currentFilterChip

        if (query.isNotEmpty()) {
            // Search query matches both rooms and buildings
            val matchedRooms = mutableListOf<CampusDisplayItem.RoomItem>()
            val matchedBuildings = mutableListOf<CampusDisplayItem.BuildingItem>()

            allBuildings.forEach { bld ->
                if (bld.name.contains(query, ignoreCase = true) || bld.located.contains(query, ignoreCase = true)) {
                    matchedBuildings.add(CampusDisplayItem.BuildingItem(bld))
                }
                bld.floors.forEach { floor ->
                    floor.rooms.forEach { room ->
                        if (room.room_no.contains(query, ignoreCase = true) ||
                            room.desc.contains(query, ignoreCase = true) ||
                            room.id.contains(query, ignoreCase = true)
                        ) {
                            matchedRooms.add(CampusDisplayItem.RoomItem(room, floor.floor_no, bld))
                        }
                    }
                }
            }

            displayItems.addAll(matchedRooms)
            displayItems.addAll(matchedBuildings)
        } else if (chip != "All") {
            // Filter by facility category
            allBuildings.forEach { bld ->
                var buildingMatches = false
                bld.floors.forEach { floor ->
                    floor.rooms.forEach { room ->
                        val matches = when (chip) {
                            "Classroom" -> room.desc.contains("room", ignoreCase = true) ||
                                    room.desc.contains("class", ignoreCase = true) ||
                                    room.desc.contains("lecture", ignoreCase = true) ||
                                    room.room_no.all { it.isDigit() }
                            "Lab" -> room.desc.contains("lab", ignoreCase = true) ||
                                    room.id.contains("lab", ignoreCase = true) ||
                                    room.room_no.contains("lab", ignoreCase = true)
                            "AVR" -> room.desc.contains("avr", ignoreCase = true) ||
                                    room.desc.contains("audio", ignoreCase = true) ||
                                    room.room_no.contains("avr", ignoreCase = true)
                            "Office" -> room.desc.contains("office", ignoreCase = true) ||
                                    room.desc.contains("dept", ignoreCase = true) ||
                                    room.desc.contains("admin", ignoreCase = true) ||
                                    room.desc.contains("dean", ignoreCase = true) ||
                                    room.desc.contains("faculty", ignoreCase = true)
                            else -> false
                        }
                        if (matches) {
                            displayItems.add(CampusDisplayItem.RoomItem(room, floor.floor_no, bld))
                            buildingMatches = true
                        }
                    }
                }
                // If the building itself represents the facility or has matching description
                if (!buildingMatches && (bld.name.contains(chip, ignoreCase = true) || bld.floor_desc.contains(chip, ignoreCase = true))) {
                    displayItems.add(CampusDisplayItem.BuildingItem(bld))
                }
            }
        } else {
            // Show all buildings by default
            displayItems.addAll(allBuildings.map { CampusDisplayItem.BuildingItem(it) })
        }

        adapter.notifyDataSetChanged()

        _binding?.apply {
            if (displayItems.isEmpty()) {
                campusEmptyState.visibility = View.VISIBLE
                campusRecyclerView.visibility = View.GONE
            } else {
                campusEmptyState.visibility = View.GONE
                campusRecyclerView.visibility = View.VISIBLE
            }
        }
    }

    inner class CampusAdapter(
        private val items: List<CampusDisplayItem>,
        private val onBuildingClick: (Building) -> Unit
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private val TYPE_BUILDING = 0
        private val TYPE_ROOM = 1

        override fun getItemViewType(position: Int): Int {
            return when (items[position]) {
                is CampusDisplayItem.BuildingItem -> TYPE_BUILDING
                is CampusDisplayItem.RoomItem -> TYPE_ROOM
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return if (viewType == TYPE_BUILDING) {
                val b = ItemContainerBuildingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                BuildingViewHolder(b)
            } else {
                val b = ItemRoomCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                RoomViewHolder(b)
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (val item = items[position]) {
                is CampusDisplayItem.BuildingItem -> (holder as BuildingViewHolder).bind(item.building)
                is CampusDisplayItem.RoomItem -> (holder as RoomViewHolder).bind(item)
            }
        }

        override fun getItemCount(): Int = items.size

        inner class BuildingViewHolder(private val b: ItemContainerBuildingBinding) :
            RecyclerView.ViewHolder(b.root) {
            fun bind(building: Building) {
                b.buildingName.text = building.name
                b.tvBuildingLocation.text = building.located.ifBlank { "Main Campus" }
                val floorCount = building.floors.size
                b.tvBuildingFloorsCount.text = if (floorCount > 1) "$floorCount Floors" else "$floorCount Floor"
                val roomCount = building.floors.sumOf { it.rooms.size }
                b.tvBuildingRoomsCount.text = if (roomCount > 0) "$roomCount Rooms & Labs" else building.floor_desc.ifBlank { "University Facilities" }

                val imgLoader = ImageLoader.Builder(b.root.context).allowHardware(false).build()
                b.buildingImage.load("${Constants.githubContent}${building.img_path}", imgLoader) {
                    placeholder(R.drawable.logo)
                    error(R.drawable.logo)
                }
                b.root.setOnClickListener { onBuildingClick(building) }
            }
        }

        inner class RoomViewHolder(private val b: ItemRoomCardBinding) :
            RecyclerView.ViewHolder(b.root) {
            fun bind(item: CampusDisplayItem.RoomItem) {
                b.tvRoomNo.text = "Room ${item.room.room_no}"
                b.tvRoomFloor.text = "${item.floorNo}th Floor"
                b.tvRoomDesc.text = item.room.desc.ifBlank { "Classroom" }
                b.tvRoomBuilding.text = item.building.name
                b.btnViewFloor.setOnClickListener { onBuildingClick(item.building) }
                b.root.setOnClickListener { onBuildingClick(item.building) }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
