package com.jerson.hcdc_portal.data.repository

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.jerson.hcdc_portal.App
import com.jerson.hcdc_portal.domain.model.SubjectOffered
import com.jerson.hcdc_portal.domain.repository.SubjectOfferedRepository
import com.jerson.hcdc_portal.util.AppPreference
import com.jerson.hcdc_portal.util.Constants
import com.jerson.hcdc_portal.util.Constants.baseUrl
import com.jerson.hcdc_portal.util.Constants.subjectOffered
import com.jerson.hcdc_portal.util.Resource
import com.jerson.hcdc_portal.util.await
import com.jerson.hcdc_portal.util.isConnected
import com.jerson.hcdc_portal.util.postRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import okhttp3.FormBody
import okhttp3.OkHttpClient
import javax.inject.Inject

class SubjectOfferedRepositoryImpl@Inject constructor(
    private val client: OkHttpClient,
    private val pref:AppPreference
): SubjectOfferedRepository{
    override suspend fun fetchSubjectOffered(page:Int): Flow<Resource<List<SubjectOffered>>> = channelFlow {
        val formBody = FormBody.Builder()
            .add("_token", pref.getStringPreference(Constants.KEY_CSRF_TOKEN))
            .add("pn", page.toString())
            .build()

        try{
            if(isConnected(App.appContext)) {
                send(Resource.Loading())
                val response = client.newCall(postRequest("$baseUrl$subjectOffered",formBody)).await()
                if(response.isSuccessful){
                    val jsonArray = JsonParser.parseString(response.body.string()).asJsonArray
                    val offeredList = mutableListOf<SubjectOffered>()
                    for (item in jsonArray) {
                        val offered = Gson().fromJson(item,SubjectOffered::class.java)
                        offeredList.add(offered)
                    }
                    Log.e("HUHU", "fetchSubjectOffered: ${offeredList.size}\t ${offeredList[0].course}", )
                    send(Resource.Success(offeredList))
                }


            }else{
                send(Resource.Error("No internet connection!"))
            }
        }catch (ex:Exception){
            send(Resource.Error(ex.message))
        }


    }
}