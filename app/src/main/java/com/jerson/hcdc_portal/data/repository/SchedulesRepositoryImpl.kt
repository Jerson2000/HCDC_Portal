package com.jerson.hcdc_portal.data.repository

import com.jerson.hcdc_portal.data.local.PortalDB
import com.jerson.hcdc_portal.data.remote.HttpClients
import com.jerson.hcdc_portal.domain.model.Schedule
import com.jerson.hcdc_portal.domain.repository.SchedulesRepository
import com.jerson.hcdc_portal.util.Constants
import com.jerson.hcdc_portal.util.Resource
import com.jerson.hcdc_portal.util.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import javax.inject.Inject

class SchedulesRepositoryImpl@Inject constructor(
    private val db: PortalDB,
    private val client:OkHttpClient
): SchedulesRepository {
    override suspend fun fetchSchedules(): Flow<Resource<List<Schedule>>> = channelFlow{
        try{
            withContext(Dispatchers.IO){
                send(Resource.Loading())
                val response = client.newCall(HttpClients.getRequest(Constants.baseUrl)).await()
                if(response.isSuccessful){
                    val bod = response.body.string()
                    val html = Jsoup.parse(bod)
                    if(html.body().text().lowercase().contains("something went wrong"))
                        send(Resource.Error("${response.code} - ${response.message}"))
                    else{
                        db.scheduleDao().deleteAllSchedules()
                        db.scheduleDao().upsertSchedules(parseSchedule(html))
                        send(Resource.Success(parseSchedule(html)))
                    }
                }else{
                    send(Resource.Error(response.message))
                }
            }
        }catch (e:Exception){
            send(Resource.Error(e.message))
        }
    }

    override suspend fun getSchedules(): Flow<Resource<List<Schedule>>> = flow {
        emit(Resource.Loading())
        db.scheduleDao().getSchedules()
            .catch {
                emit(Resource.Error(it.message))
            }
            .collect{
                emit(Resource.Success(it))
            }
    }
    private fun parseSchedule(response: Document): List<Schedule> {
        val list = mutableListOf<Schedule>()
        val tBody = response.select("div.col-sm-9 tbody")
        for (body in tBody) {
            val tableData = body.select("tr")
            for (rowData in tableData) {
                val offerNo = rowData.select("td:eq(0)")
                val gClass = rowData.select("td:eq(1)")
                val subjCode = rowData.select("td:eq(2)")
                val desc = rowData.select("td:eq(3)")
                val unit = rowData.select("td:eq(4)")
                val days = rowData.select("td:eq(5)")
                val time = rowData.select("td:eq(6)")
                val room = rowData.select("td:eq(7)")
                val lecLab = rowData.select("td:eq(8)")
                val model = Schedule(
                    0,
                    offerNo.text(),
                    gClass.text(),
                    subjCode.text(),
                    desc.text(),
                    unit.text(),
                    days.text(),
                    time.text(),
                    room.text(),
                    lecLab.text()
                )
                list.add(model)
            }
        }
        return list
    }
}