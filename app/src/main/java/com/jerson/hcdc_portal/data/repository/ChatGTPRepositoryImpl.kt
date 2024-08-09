package com.jerson.hcdc_portal.data.repository

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.jerson.hcdc_portal.App
import com.jerson.hcdc_portal.domain.model.ChatGPT
import com.jerson.hcdc_portal.domain.repository.ChatGPTRepository
import com.jerson.hcdc_portal.util.Resource
import com.jerson.hcdc_portal.util.await
import com.jerson.hcdc_portal.util.getRequest
import com.jerson.hcdc_portal.util.isConnected
import com.jerson.hcdc_portal.util.postRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import org.jsoup.Jsoup
import javax.inject.Inject

class ChatGTPRepositoryImpl @Inject constructor(
    private val client:OkHttpClient
):ChatGPTRepository {
    private val endpoint = "https://chatbotai.one/wp-admin/admin-ajax.php"
    override suspend fun chat(formBody: FormBody): Flow<Resource<ChatGPT>> = channelFlow{
        try{
            if(isConnected(App.appContext)) {
                withContext(Dispatchers.IO){
                    send(Resource.Loading())
                    val response = client.newCall(postRequest(endpoint,formBody)).await()
                    val responseBody = response.body.string()
                    if(response.isSuccessful){
                        response.body.use {
                            val jsonResp = JsonParser.parseString(responseBody)
                            val data = Gson().fromJson(jsonResp,ChatGPT::class.java)
                            send(Resource.Success(data))
                        }
                    }
                    else{
                        send(Resource.Error("${response.message}-${response.code}"))
                    }
                }
            }else{
                send(Resource.Error("No internet connection!"))
            }
        }catch (e:Exception){
            send(Resource.Error(e.message))
        }
    }

    override suspend fun chatDataValue(): Flow<Resource<FormBody.Builder>> = channelFlow{

        try{
            if(isConnected(App.appContext)) {
                withContext(Dispatchers.IO){
                    send(Resource.Loading())
                    val response = client.newCall(getRequest(endpoint.substring(0, endpoint.indexOf("/", 8)))).await()
                    val responseBody = response.body.string()
                    if(response.isSuccessful){
                        val html = Jsoup.parse(responseBody)
                        val bodyBuilder = FormBody.Builder()
                            .add("_wpnonce", html.select("div.wpaicg-chat-shortcode").attr("data-nonce").trim())
                            .add("post_id", html.select("div.wpaicg-chat-shortcode").attr("data-post-id").trim())
                            .add("url", html.select("div.wpaicg-chat-shortcode").attr("data-url").trim())
                            .add("action", "wpaicg_chat_shortcode_message")
                            .add("bot_id", html.select("div.wpaicg-chat-shortcode").attr("data-bot-id").trim())

                        send(Resource.Success(bodyBuilder))
                    }
                    else{
                        send(Resource.Error("${response.message}-${response.code}"))
                    }
                }
            }else{
                send(Resource.Error("No internet connection!"))
            }
        }catch (e:Exception){
            send(Resource.Error(e.message))
        }

    }


}