package com.jerson.hcdc_portal.data.repository

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.jerson.hcdc_portal.App
import com.jerson.hcdc_portal.domain.model.SubjectOffered
import com.jerson.hcdc_portal.domain.repository.SubjectOfferedRepository
import com.jerson.hcdc_portal.util.AppPreference
import com.jerson.hcdc_portal.util.Constants
import com.jerson.hcdc_portal.util.Constants.baseUrl
import com.jerson.hcdc_portal.util.Constants.subjectOffered
import com.jerson.hcdc_portal.util.Constants.subjectOfferedSearch
import com.jerson.hcdc_portal.util.Resource
import com.jerson.hcdc_portal.util.await
import com.jerson.hcdc_portal.util.isConnected
import com.jerson.hcdc_portal.util.postRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.executeAsync
import okhttp3.internal.http.HTTP_INSUFFICIENT_SPACE_ON_RESOURCE
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
                withContext(Dispatchers.IO){
                    send(Resource.Loading())
                    val response = client.newCall(postRequest("$baseUrl$subjectOffered",formBody)).await()
                    val responseBody = response.body.string()
                    if(response.isSuccessful){
                        response.body.use{
                            val jsonArray = JsonParser.parseString(responseBody).asJsonArray
                            val offeredList = mutableListOf<SubjectOffered>()
                            for (item in jsonArray) {
                                val offered = Gson().fromJson(item,SubjectOffered::class.java)
                                offeredList.add(offered)
                            }
                            send(Resource.Success(offeredList))
                        }
                    }else if(response.code == HTTP_INSUFFICIENT_SPACE_ON_RESOURCE){
                        send(Resource.Error("session end"))
                    }
                    else{
                        send(Resource.Error("${response.message}-${response.code}"))
                    }
                }
        }catch (ex:Exception){
            println("ERROR: ${ex.stackTraceToString()}")
            send(Resource.Error(ex.message))
        }


    }

    override suspend fun fetchSubjectOffered(query: String?): Flow<Resource<List<SubjectOffered>>> = channelFlow{
        val formBody = FormBody.Builder()
            .add("query",query?:"")
            .add("_token", pref.getStringPreference(Constants.KEY_CSRF_TOKEN))
            .add("arnel", "arnel")
            .build()

        try{
            if(isConnected(App.appContext)) {
                withContext(Dispatchers.IO) {
                    send(Resource.Loading())
                    val response = client.newCall(postRequest("$baseUrl$subjectOfferedSearch",formBody)).await()
                    if(response.isSuccessful){
                        response.body.use {
                            val jsonArray = JsonParser.parseString(response.body.string()).asJsonArray
                            val offeredList = mutableListOf<SubjectOffered>()
                            for (item in jsonArray) {
                                val offered = Gson().fromJson(item,SubjectOffered::class.java)
                                offeredList.add(offered)
                            }
                            send(Resource.Success(offeredList))
                        }
                    }else if(response.code == HTTP_INSUFFICIENT_SPACE_ON_RESOURCE){
                        send(Resource.Error("session end"))
                    }
                    else{
                        send(Resource.Error("${response.message}-${response.code}"))
                    }
                }
            }else{
                send(Resource.Error("No internet connection!"))
            }
        }catch (ex:Exception){
            println("ERROR: ${ex.stackTraceToString()}")
            send(Resource.Error(ex.message))
        }
    }
}