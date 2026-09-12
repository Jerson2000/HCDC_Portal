package com.jerson.hcdc_portal.data.repository

import com.jerson.hcdc_portal.BuildConfig
import com.jerson.hcdc_portal.domain.repository.AppRepository
import com.jerson.hcdc_portal.util.Constants
import com.jerson.hcdc_portal.util.Resource
import com.jerson.hcdc_portal.util.await
import com.jerson.hcdc_portal.util.getRequest
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
            if (BuildConfig.key1.isBlank() || !BuildConfig.key1.startsWith("http")) {
                send(Resource.Error("Crash reporting endpoint is not configured."))
                return@channelFlow
            }
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

            response.use {
                if (it.isSuccessful) {
                    send(Resource.Success(true))
                } else {
                    send(Resource.Error(it.message))
                }
            }
        } catch (e: Exception) {
            send(Resource.Error("${e.message}"))
        }
    }

    override suspend fun checkForUpdate(): Flow<Resource<String>> = channelFlow {
        try {
            send(Resource.Loading())
            val response = client.newCall(getRequest(Constants.githubReleases)).await()
            response.use {
                if (it.isSuccessful) {
                    val bodyStr = it.body?.string().orEmpty()
                    if (bodyStr.isBlank()) {
                        send(Resource.Success(""))
                        return@channelFlow
                    }
                    val json = JSONObject(bodyStr)
                    val latestTag = json.optString("tag_name", "")
                    val isNewer = isVersionNewer(latestTag, BuildConfig.VERSION_NAME)
                    val assets = json.optJSONArray("assets")
                    if (isNewer && assets != null && assets.length() > 0) {
                        val assetObj = assets.optJSONObject(0)
                        val downloadUrl = assetObj?.optString("browser_download_url").orEmpty()
                        send(Resource.Success(downloadUrl))
                    } else {
                        send(Resource.Success(""))
                    }
                } else {
                    send(Resource.Error(it.message))
                }
            }
        } catch (e: Exception) {
            send(Resource.Error("${e.message}"))
        }
    }

    companion object {
        fun isVersionNewer(latestStr: String, currentStr: String): Boolean {
            val latestClean = latestStr.trim().removePrefix("v")
            val currentClean = currentStr.trim().removePrefix("v")
            val latestParts = latestClean.split(".").mapNotNull { it.toIntOrNull() }
            val currentParts = currentClean.split(".").mapNotNull { it.toIntOrNull() }
            val maxLen = maxOf(latestParts.size, currentParts.size)
            for (i in 0 until maxLen) {
                val l = latestParts.getOrElse(i) { 0 }
                val c = currentParts.getOrElse(i) { 0 }
                if (l > c) return true
                if (l < c) return false
            }
            return false
        }
    }
}