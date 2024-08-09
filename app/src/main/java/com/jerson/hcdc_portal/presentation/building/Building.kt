package com.jerson.hcdc_portal.presentation.building

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.jerson.hcdc_portal.databinding.ActivityBuildingBinding
import com.jerson.hcdc_portal.domain.model.Building
import com.jerson.hcdc_portal.presentation.building.adapter.BuildingAdapter
import com.jerson.hcdc_portal.presentation.building.viewmodel.RoomViewModel
import com.jerson.hcdc_portal.util.Resource
import com.jerson.hcdc_portal.util.SnackBarKt
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class Building : AppCompatActivity() {
    private lateinit var binding: ActivityBuildingBinding
    private val viewModel: RoomViewModel by viewModels()
    private val list = mutableListOf<Building>()
    private lateinit var adapter: BuildingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBuildingBinding.inflate(layoutInflater)
        setContentView(binding.root)


        setSupportActionBar(binding.header.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            binding.header.collapsingToolbar.title = "Building"
            binding.header.collapsingToolbar.subtitle = "Building Information"
        }

        adapter = BuildingAdapter(list){
            startActivity(Intent(this, BuildingDetails::class.java).putExtra("building",it))
        }
        binding.recyclerView.adapter = adapter

        getBuilding()

        binding.header.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

    }

    private fun getBuilding() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.fetchRoom.collect {
                    when (it) {
                        is Resource.Loading -> {

                        }

                        is Resource.Success -> {
                            list.clear()
                            list.addAll(it.data!!.building)
                            adapter.notifyDataSetChanged()

                        }

                        is Resource.Error -> {
                            SnackBarKt.snackBarLong(binding.root, it.message.toString())
                        }

                        else -> Unit
                    }
                }
            }
        }
    }
}
