package com.jerson.hcdc_portal.data.repository

import com.jerson.hcdc_portal.App.Companion.appContext
import com.jerson.hcdc_portal.BuildConfig
import com.jerson.hcdc_portal.domain.repository.AppRepository
import com.jerson.hcdc_portal.util.Constants
import com.jerson.hcdc_portal.util.Resource
import com.jerson.hcdc_portal.util.await
import com.jerson.hcdc_portal.util.getRequest
import com.jerson.hcdc_portal.util.isConnected
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import javax.inject.Inject

class AppRepositoryImpl @Inject constructor(
    private val client: OkHttpClient
) : AppRepository {
    override suspend fun sendCrashReport(report: String): Flow<Resource<Boolean>> = channelFlow {
        try {
            if (isConnected(appContext)) {
                send(Resource.Loading())

                val json = JSONObject()
                    .put("content", report)
                    .put("avatar_url", "https://avatars.githubusercontent.com/u/112932949?v=4")
                    .put("username", "Portal-Reporter")
                val requestBody =
                    json.toString().toRequestBody("application/json".toMediaTypeOrNull())

                val request = Request.Builder()
                    .url(BuildConfig.key1)
                    .post(requestBody)
                    .build()
                val response = client.newCall(request).await()

                if (response.isSuccessful) {
                    send(Resource.Success(true))
                } else {
                    send(Resource.Error(response.message))
                }
                response.body.close()
            } else {
                send(Resource.Error("No internet connection!"))
            }


        } catch (e: Exception) {
            send(Resource.Error("${e.message}"))
        }
    }

    override suspend fun checkForUpdate(): Flow<Resource<String>> = channelFlow{
        try {
            if (isConnected(appContext)) {
                send(Resource.Loading())
                val response = client.newCall(getRequest(Constants.githubReleases)).await()
                if (response.isSuccessful) {
                    val json = JSONObject(response.body.string())
                    val latest = extractVersion(json.getString("tag_name"))
                    val current = extractVersion(BuildConfig.VERSION_NAME)
                    if(latest > current)
                        send(Resource.Success(json.getString("html_url")))
                    else
                        send(Resource.Success(""))


                } else {
                    send(Resource.Error(response.message))
                }
                response.body.close()
            } else {
                send(Resource.Error("No internet connection!"))
            }


        } catch (e: Exception) {
            send(Resource.Error("${e.message}"))
        }
    }

    private fun extractVersion(str: String): Int {
        val y = str.substringAfter("v")
        val x = y.split(".").map { it.toInt()}
        var c=0
            for(v in x) c+=v
        return c
    }
}