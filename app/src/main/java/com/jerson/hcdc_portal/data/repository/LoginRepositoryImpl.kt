package com.jerson.hcdc_portal.data.repository

import com.jerson.hcdc_portal.data.remote.HttpClients
import com.jerson.hcdc_portal.domain.repository.LoginRepository
import com.jerson.hcdc_portal.util.AppPreference
import com.jerson.hcdc_portal.util.Constants.KEY_CSRF_TOKEN
import com.jerson.hcdc_portal.util.Constants.baseUrl
import com.jerson.hcdc_portal.util.Constants.gradesUrl
import com.jerson.hcdc_portal.util.Constants.loginPostUrl
import com.jerson.hcdc_portal.util.Resource
import com.jerson.hcdc_portal.util.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import org.jsoup.Jsoup
import javax.inject.Inject

class LoginRepositoryImpl @Inject constructor(
    private val client: OkHttpClient,
    private val preference: AppPreference
) : LoginRepository {
    private val tag = "LoginRepositoryImpl"
    override suspend fun login(email: String, pass: String): Flow<Resource<String>> = flow {
        val formBody = FormBody.Builder()
            .add("_token", preference.getStringPreference(KEY_CSRF_TOKEN))
            .add("email", email)
            .add("password", pass)
            .build()
        try{
            emit(Resource.Loading())
            val response = client.newCall(HttpClients.postRequest(
                "$baseUrl$loginPostUrl",
                formBody
            )).await()

            if(response.isSuccessful){
                emit(Resource.Success(response.code.toString()))
            }else{
                emit(Resource.Error(response.message))
            }

        }catch(e:Exception){
            emit(Resource.Error("${e.message}"))
        }
    }

    override suspend fun checkSession(): Flow<Resource<Boolean>> = channelFlow {
        try{
            withContext(Dispatchers.IO){
                val response = client.newCall(HttpClients.getRequest("$baseUrl$gradesUrl")).await()
                if(response.isSuccessful){
                    val bod = response.body.string()
                    val html = Jsoup.parse(bod)
                    val token = html.select("meta[name=csrf-token]").attr("content")
                    val isLogin = html.body().text().contains("CROSSIAN LOG-IN");
                    preference.setStringPreference(KEY_CSRF_TOKEN, token)
                    send(Resource.Success(isLogin))
                }else{
                    send(Resource.Error(response.message))
                }
            }
        }catch (e:Exception){
            send(Resource.Error(e.message))
        }
    }

}