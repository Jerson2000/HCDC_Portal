package com.jerson.hcdc_portal.data.repository

import com.jerson.hcdc_portal.App.Companion.appContext
import com.jerson.hcdc_portal.domain.repository.LoginRepository
import com.jerson.hcdc_portal.util.AppPreference
import com.jerson.hcdc_portal.util.Constants
import com.jerson.hcdc_portal.util.Constants.KEY_CSRF_TOKEN
import com.jerson.hcdc_portal.util.Constants.KEY_IS_SESSION
import com.jerson.hcdc_portal.util.Constants.baseUrl
import com.jerson.hcdc_portal.util.Constants.gradesUrl
import com.jerson.hcdc_portal.util.Constants.loginPostUrl
import com.jerson.hcdc_portal.util.Resource
import com.jerson.hcdc_portal.util.await
import com.jerson.hcdc_portal.util.getRequest
import com.jerson.hcdc_portal.util.isConnected
import com.jerson.hcdc_portal.util.postRequest
import com.jerson.hcdc_portal.util.sessionParse
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
            if(isConnected(appContext)){
                emit(Resource.Loading())
                val response = client.newCall(postRequest(
                    "$baseUrl$loginPostUrl",
                    formBody
                )).await()

                if(response.isSuccessful){
                    emit(Resource.Success(response.code.toString()))
                }else{
                    emit(Resource.Error(response.message))
                }
            }else{
                emit(Resource.Error("No internet connection!"))
            }


        }catch(e:Exception){
            emit(Resource.Error("${e.message}"))
        }
    }

    override suspend fun checkSession(): Flow<Resource<Boolean>> = channelFlow {
        try{
            if(isConnected(appContext)){
                withContext(Dispatchers.IO){
                    send(Resource.Loading())
                    val response = client.newCall(getRequest("$baseUrl$gradesUrl")).await()
                    if(response.isSuccessful){
                        val bod = response.body.string()
                        val html = Jsoup.parse(bod)
                        send(Resource.Success(sessionParse(preference,html)))
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

}