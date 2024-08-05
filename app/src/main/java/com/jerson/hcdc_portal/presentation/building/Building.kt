package com.jerson.hcdc_portal.presentation.building

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.jerson.hcdc_portal.App.Companion.appContext
import com.jerson.hcdc_portal.databinding.ActivityBuildingBinding
import com.jerson.hcdc_portal.domain.model.Building
import com.jerson.hcdc_portal.domain.model.Floor
import com.jerson.hcdc_portal.domain.model.Room
import com.jerson.hcdc_portal.domain.model.Rooms
import com.jerson.hcdc_portal.presentation.building.adapter.BuildingAdapter
import com.jerson.hcdc_portal.presentation.building.viewmodel.RoomViewModel
import com.jerson.hcdc_portal.util.Resource
import com.jerson.hcdc_portal.util.SnackBarKt
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader

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
        adapter = BuildingAdapter(list)
        binding.recyclerView.adapter = adapter

        getBuilding()

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

    private fun extractFirstNumber(str: String): Int {
        val regex = "\\d+".toRegex()
        val matchResult = regex.find(str)
        return matchResult?.value?.toInt() ?: 0
    }

    private fun extractFirstNumberx(str: String): Int =
        "\\d".toRegex().find(str)?.toString()?.toInt() ?: 0

    private fun extractLetter(str:String):String{
        return str.filter { it.isLetter() }
    }
    private fun extractNumber(str:String):String{
        return str.filter { it.isDigit() }
    }
}
