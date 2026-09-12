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
                val inputStream = if (file.exists() && file.length() > 0) {
                    FileInputStream(file)
                } else {
                    appContext.assets.open("rooms.json")
                }
                inputStream.use { stream ->
                    InputStreamReader(stream).use { reader ->
                        val room = Gson().fromJson(reader, Room::class.java)
                        if (room != null) {
                            send(Resource.Success(room))
                        } else {
                            send(Resource.Error("Failed to parse building data."))
                        }
                    }
                }
            }
        } catch (e: Exception) {
            send(Resource.Error(e.message))
        }
    }
}