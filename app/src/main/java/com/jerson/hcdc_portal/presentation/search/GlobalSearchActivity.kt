package com.jerson.hcdc_portal.presentation.search

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jerson.hcdc_portal.R
import com.jerson.hcdc_portal.data.local.PortalDB
import com.jerson.hcdc_portal.databinding.ActivityGlobalSearchBinding
import com.jerson.hcdc_portal.databinding.ItemSearchHeaderBinding
import com.jerson.hcdc_portal.databinding.ItemSearchResultBinding
import com.jerson.hcdc_portal.domain.model.Building
import com.jerson.hcdc_portal.domain.model.Grade
import com.jerson.hcdc_portal.domain.model.Room
import com.jerson.hcdc_portal.domain.model.Schedule
import androidx.activity.viewModels
import com.jerson.hcdc_portal.presentation.building.viewmodel.RoomViewModel
import com.jerson.hcdc_portal.presentation.building.BuildingDetails
import com.jerson.hcdc_portal.presentation.subjects.SubjectDetailsKt
import com.jerson.hcdc_portal.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SearchListItem {
    data class Header(val title: String) : SearchListItem()
    data class Result(
        val title: String,
        val subtitle: String,
        val iconRes: Int,
        val onClick: () -> Unit
    ) : SearchListItem()
}

@AndroidEntryPoint
class GlobalSearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGlobalSearchBinding
    private val searchResults = mutableListOf<SearchListItem>()
    private lateinit var adapter: SearchResultsAdapter

    @Inject
    lateinit var portalDB: PortalDB

    private val roomViewModel: RoomViewModel by viewModels()

    private var cachedSchedules = listOf<Schedule>()
    private var cachedGrades = listOf<Grade>()
    private var cachedBuildings = listOf<Building>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGlobalSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = SearchResultsAdapter(searchResults)
        binding.searchResultsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.searchResultsRecyclerView.adapter = adapter

        binding.btnBack.setOnClickListener {
            finish()
        }

        setupSuggestions()
        loadDataForSearch()

        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString()?.trim().orEmpty()
                performSearch(query)
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupSuggestions() {
        binding.chipSuggestIT.setOnClickListener { binding.searchEditText.setText("IT") }
        binding.chipSuggestLab.setOnClickListener { binding.searchEditText.setText("Lab") }
        binding.chipSuggestRoom.setOnClickListener { binding.searchEditText.setText("406") }
        binding.chipSuggestBuilding.setOnClickListener { binding.searchEditText.setText("Building") }
    }

    private fun loadDataForSearch() {
        lifecycleScope.launch {
            // 1. Schedules
            try {
                portalDB.scheduleDao().getSchedules().firstOrNull()?.let {
                    cachedSchedules = it
                }
            } catch (_: Exception) {}

            // 2. Grades
            try {
                portalDB.gradeDao().getAllGrades().firstOrNull()?.let {
                    cachedGrades = it
                }
            } catch (_: Exception) {}

            // 3. Buildings & Rooms
            try {
                roomViewModel.fetchRoom.collect { res ->
                    if (res is Resource.Success && res.data != null) {
                        cachedBuildings = res.data.building
                    }
                }
            } catch (_: Exception) {}
        }
    }

    private fun performSearch(query: String) {
        if (query.isEmpty()) {
            binding.searchResultsRecyclerView.visibility = View.GONE
            binding.emptyStateLayout.visibility = View.GONE
            binding.initialSuggestionsLayout.visibility = View.VISIBLE
            searchResults.clear()
            adapter.notifyDataSetChanged()
            return
        }

        binding.initialSuggestionsLayout.visibility = View.GONE
        searchResults.clear()

        // 1. Match Subjects
        val matchedSubjects = cachedSchedules.filter {
            it.subjectCode?.contains(query, ignoreCase = true) == true ||
                    it.description.contains(query, ignoreCase = true)
        }.distinctBy { it.subjectCode }

        if (matchedSubjects.isNotEmpty()) {
            searchResults.add(SearchListItem.Header(getString(R.string.category_subjects)))
            matchedSubjects.forEach { sched ->
                searchResults.add(
                    SearchListItem.Result(
                        title = "${sched.subjectCode} — ${sched.description}",
                        subtitle = "${sched.days.orEmpty()} • ${sched.time.orEmpty()} • Room ${sched.room.orEmpty()}",
                        iconRes = R.drawable.ic_books,
                        onClick = {
                            startActivity(
                                Intent(this, SubjectDetailsKt::class.java).putExtra("subject", sched)
                            )
                        }
                    )
                )
            }
        }

        // 2. Match Schedule Entries
        val matchedSchedules = cachedSchedules.filter {
            it.days?.contains(query, ignoreCase = true) == true ||
                    it.time?.contains(query, ignoreCase = true) == true
        }

        if (matchedSchedules.isNotEmpty() && matchedSubjects.isEmpty()) {
            searchResults.add(SearchListItem.Header(getString(R.string.category_schedule)))
            matchedSchedules.forEach { sched ->
                searchResults.add(
                    SearchListItem.Result(
                        title = "${sched.subjectCode} — ${sched.days.orEmpty()} ${sched.time.orEmpty()}",
                        subtitle = "${sched.description} • Room ${sched.room.orEmpty()}",
                        iconRes = R.drawable.ic_schedule,
                        onClick = {
                            startActivity(
                                Intent(this, SubjectDetailsKt::class.java).putExtra("subject", sched)
                            )
                        }
                    )
                )
            }
        }

        // 3. Match Rooms & Buildings
        val matchedRooms = mutableListOf<Triple<String, String, Building>>()
        cachedBuildings.forEach { building ->
            building.floors.forEach { floor ->
                floor.rooms.forEach { room ->
                    if (room.room_no.contains(query, ignoreCase = true) ||
                        room.desc.contains(query, ignoreCase = true)
                    ) {
                        matchedRooms.add(Triple(room.room_no, "${building.name} • ${floor.floor_no}th Floor", building))
                    }
                }
            }
        }

        if (matchedRooms.isNotEmpty()) {
            searchResults.add(SearchListItem.Header(getString(R.string.category_rooms)))
            matchedRooms.forEach { (roomNo, location, bld) ->
                searchResults.add(
                    SearchListItem.Result(
                        title = "Room $roomNo",
                        subtitle = location,
                        iconRes = R.drawable.ic_location_on,
                        onClick = {
                            startActivity(
                                Intent(this, BuildingDetails::class.java).putExtra("building", bld)
                            )
                        }
                    )
                )
            }
        }

        // 4. Match Buildings
        val matchedBuildings = cachedBuildings.filter {
            it.name.contains(query, ignoreCase = true) ||
                    it.located.contains(query, ignoreCase = true)
        }

        if (matchedBuildings.isNotEmpty()) {
            searchResults.add(SearchListItem.Header(getString(R.string.category_buildings)))
            matchedBuildings.forEach { bld ->
                searchResults.add(
                    SearchListItem.Result(
                        title = bld.name,
                        subtitle = "${bld.located} • ${bld.floor_desc}",
                        iconRes = R.drawable.ic_apartment,
                        onClick = {
                            startActivity(
                                Intent(this, BuildingDetails::class.java).putExtra("building", bld)
                            )
                        }
                    )
                )
            }
        }

        // 5. Match Grades
        val matchedGrades = cachedGrades.filter {
            it.subjectCode?.contains(query, ignoreCase = true) == true ||
                    it.description.contains(query, ignoreCase = true)
        }
        if (matchedGrades.isNotEmpty() && matchedSubjects.isEmpty()) {
            searchResults.add(SearchListItem.Header("GRADES"))
            matchedGrades.forEach { grade ->
                val finalG = if (grade.finalGrade.isNullOrBlank()) "Ongoing" else "Final: ${grade.finalGrade}"
                searchResults.add(
                    SearchListItem.Result(
                        title = "${grade.subjectCode} — ${grade.description}",
                        subtitle = "$finalG • ${grade.finalRemark.orEmpty()}",
                        iconRes = R.drawable.ic_school,
                        onClick = {}
                    )
                )
            }
        }

        adapter.notifyDataSetChanged()

        if (searchResults.isEmpty()) {
            binding.searchResultsRecyclerView.visibility = View.GONE
            binding.emptyStateLayout.visibility = View.VISIBLE
        } else {
            binding.searchResultsRecyclerView.visibility = View.VISIBLE
            binding.emptyStateLayout.visibility = View.GONE
        }
    }

    inner class SearchResultsAdapter(private val items: List<SearchListItem>) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private val TYPE_HEADER = 0
        private val TYPE_ITEM = 1

        override fun getItemViewType(position: Int): Int {
            return when (items[position]) {
                is SearchListItem.Header -> TYPE_HEADER
                is SearchListItem.Result -> TYPE_ITEM
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return if (viewType == TYPE_HEADER) {
                val b = ItemSearchHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                HeaderViewHolder(b)
            } else {
                val b = ItemSearchResultBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                ItemViewHolder(b)
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (val item = items[position]) {
                is SearchListItem.Header -> (holder as HeaderViewHolder).bind(item)
                is SearchListItem.Result -> (holder as ItemViewHolder).bind(item)
            }
        }

        override fun getItemCount(): Int = items.size

        inner class HeaderViewHolder(private val b: ItemSearchHeaderBinding) : RecyclerView.ViewHolder(b.root) {
            fun bind(header: SearchListItem.Header) {
                b.tvSectionHeader.text = header.title
            }
        }

        inner class ItemViewHolder(private val b: ItemSearchResultBinding) : RecyclerView.ViewHolder(b.root) {
            fun bind(res: SearchListItem.Result) {
                b.tvResultTitle.text = res.title
                b.tvResultSubtitle.text = res.subtitle
                b.ivResultIcon.setImageResource(res.iconRes)
                b.root.setOnClickListener { res.onClick() }
            }
        }
    }
}
