package com.jerson.hcdc_portal.data.repository

import android.app.Application
import com.google.gson.Gson
import com.jerson.hcdc_portal.App.Companion.appContext
import com.jerson.hcdc_portal.domain.model.Building
import com.jerson.hcdc_portal.domain.model.Floor
import com.jerson.hcdc_portal.domain.model.Room
import com.jerson.hcdc_portal.domain.model.Rooms
import com.jerson.hcdc_portal.domain.repository.RoomRepository
import com.jerson.hcdc_portal.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import javax.inject.Inject

class RoomRepositoryImpl @Inject constructor(
):RoomRepository {
    override suspend fun getBuilding(): Flow<Resource<Room>> = channelFlow{
        send(Resource.Loading())
        val file = File(appContext.cacheDir, "rooms.json")
        val filePath = file.absolutePath

        try {
            withContext(Dispatchers.IO) {
                val inputStream = FileInputStream(filePath)
                val inputStreamReader = InputStreamReader(inputStream)
                val room = Gson().fromJson(inputStreamReader, Room::class.java)

                inputStreamReader.close()
                send(Resource.Success(room))
            }
        } catch (e: Exception) {
            println("getBuilding: Err-> $e")
            send(Resource.Error(e.message))
        }
    }
}