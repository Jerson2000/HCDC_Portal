package com.jerson.hcdc_portal.data.repository

import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.jerson.hcdc_portal.BuildConfig
import com.jerson.hcdc_portal.domain.model.Chat
import com.jerson.hcdc_portal.domain.model.Role
import com.jerson.hcdc_portal.domain.model.RoleSerializer
import com.jerson.hcdc_portal.domain.repository.ChatGPTRepository
import com.jerson.hcdc_portal.util.Resource
import com.jerson.hcdc_portal.util.await
import com.jerson.hcdc_portal.util.postRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import javax.inject.Inject

class ChatGPTRepositoryImpl @Inject constructor(
    private val client: OkHttpClient
) : ChatGPTRepository {
    override suspend fun chat(chatList:List<Chat>): Flow<Resource<String>> = channelFlow {
        try {

            withContext(Dispatchers.IO) {
                send(Resource.Loading())
                val gson = GsonBuilder()
                    .registerTypeAdapter(Role::class.java, RoleSerializer())
                    .setPrettyPrinting()
                    .create()
                val formData = FormBody.Builder()
                    .add("chat_style", "gpt-chat")
                    .add("chatHistory",gson.toJson(chatList).toString())
                    .add("model", "standard")
                    .add("hacker_is_stinky", "very_stinky")
                    .build()

                val response = client.newCall(postRequest(BuildConfig.ai_endpoint, formData)).await()

                response.use {
                    if (it.isSuccessful) {
                        send(Resource.Success(it.body.string()))
                    } else {
                        send(Resource.Error("Something went wrong."))
                    }
                }

            }
        } catch (e: Exception) {
            send(Resource.Error(e.message))
        }
    }
}