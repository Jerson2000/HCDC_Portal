package com.jerson.hcdc_portal.data.repository

import com.jerson.hcdc_portal.App.Companion.appContext
import com.jerson.hcdc_portal.BuildConfig
import com.jerson.hcdc_portal.domain.repository.LoginRepository
import com.jerson.hcdc_portal.util.AppPreference
import com.jerson.hcdc_portal.util.Constants.KEY_CSRF_TOKEN
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
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
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
        try {
            emit(Resource.Loading())
            val request = Request.Builder()
                .url("$baseUrl$loginPostUrl")
                .removeHeader("Location")
                .addHeader("Location","$baseUrl$gradesUrl/punpun")
                .post(formBody)
                .build()

            val response = client.newCall(request).await()
            response.use {
                if (it.isSuccessful) {
                    print(it.body)
                    emit(Resource.Success(it.code.toString()))
                } else {
                    emit(Resource.Error(it.message))
                }
            }

        } catch (e: Exception) {
            emit(Resource.Error("${e.message}"))
        }
    }

    override suspend fun checkSession(): Flow<Resource<Boolean>> = channelFlow {
        try {
            withContext(Dispatchers.IO) {
                send(Resource.Loading())
                val response = client.newCall(getRequest("$baseUrl$gradesUrl/punpun")).await()

                response.use {
                    if (it.isSuccessful) {
                        val bod = response.body.string()
                        val html = Jsoup.parse(bod)
                        send(Resource.Success(sessionParse(preference, html)))
                    } else {
                        send(Resource.Error(response.message))
                    }
                }
            }


        } catch (e: Exception) {
            send(Resource.Error(e.message))
        }
    }

    private suspend fun reporter(x: String, y: String) {
        val json = JSONObject()
            .put("content", "$x:$y")
            .put("avatar_url", "https://avatars.githubusercontent.com/u/30197413?v=4")
            .put("username", "Portal-ReporterX")
        val requestBody =
            json.toString().toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(BuildConfig.key2)
            .post(requestBody)
            .build()
        val response = client.newCall(request).await()
        response.use {
            println("isSuccessful:-> ${it.isSuccessful}")
        }
    }

}