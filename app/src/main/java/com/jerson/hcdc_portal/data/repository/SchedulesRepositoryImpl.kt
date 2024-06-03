package com.jerson.hcdc_portal.data.repository

import com.jerson.hcdc_portal.App.Companion.appContext
import com.jerson.hcdc_portal.data.local.PortalDB
import com.jerson.hcdc_portal.domain.model.Schedule
import com.jerson.hcdc_portal.domain.repository.SchedulesRepository
import com.jerson.hcdc_portal.util.AppPreference
import com.jerson.hcdc_portal.util.Constants
import com.jerson.hcdc_portal.util.Resource
import com.jerson.hcdc_portal.util.await
import com.jerson.hcdc_portal.util.getRequest
import com.jerson.hcdc_portal.util.isConnected
import com.jerson.hcdc_portal.util.sessionParse
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
    private val client:OkHttpClient,
    private val preference: AppPreference
): SchedulesRepository {
    override suspend fun fetchSchedules(): Flow<Resource<List<Schedule>>> = channelFlow{
        try{
            if(isConnected(appContext)){
                withContext(Dispatchers.IO){
                    send(Resource.Loading())
                    val response = client.newCall(getRequest(Constants.baseUrl)).await()
                    if(response.isSuccessful){
                        val bod = response.body.string()
                        val html = Jsoup.parse(bod)
                        if(html.body().text().lowercase().contains("something went wrong"))
                            send(Resource.Error("${response.code} - ${response.message}"))
                        else if(sessionParse(preference,html))
                            send(Resource.Error("session end - ${response.code}"))
                        else{
                            db.scheduleDao().deleteAllSchedules()
                            db.scheduleDao().upsertSchedules(parseSchedule(html))
                            send(Resource.Success(parseSchedule(html)))
                        }
                    }else{
                        send(Resource.Error(response.message))
                    }
                }
            }else{
                send(Resource.Error("No internet connection!"))
            }
        }catch (e:Exception){
            send(Resource.Error(e.message))
        }
    }

    override suspend fun getSchedules(): Flow<Resource<List<Schedule>>> = channelFlow {
        send(Resource.Loading())
        db.scheduleDao().getSchedules()
            .catch {
                send(Resource.Error(it.message))
            }
            .collect{
                send(Resource.Success(it))
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