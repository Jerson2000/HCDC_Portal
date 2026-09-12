package com.jerson.hcdc_portal.data.repository

import com.jerson.hcdc_portal.data.parser.UserSessionParser
import com.jerson.hcdc_portal.domain.repository.LoginRepository
import com.jerson.hcdc_portal.util.AppPreference
import com.jerson.hcdc_portal.util.Constants.KEY_CSRF_TOKEN
import com.jerson.hcdc_portal.util.Constants.KEY_IS_SESSION
import com.jerson.hcdc_portal.util.Constants.baseUrl
import com.jerson.hcdc_portal.util.Constants.gradesUrl
import com.jerson.hcdc_portal.util.Constants.loginPostUrl
import com.jerson.hcdc_portal.util.Resource
import com.jerson.hcdc_portal.util.await
import com.jerson.hcdc_portal.util.getRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import javax.inject.Inject

class LoginRepositoryImpl @Inject constructor(
    private val client: OkHttpClient,
    private val preference: AppPreference,
    private val sessionParser: UserSessionParser
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
                        val bod = it.body?.string().orEmpty()
                        val html = Jsoup.parse(bod)
                        val status = sessionParser.parseSession(html)
                        preference.setStringPreference(KEY_CSRF_TOKEN, status.csrfToken)
                        preference.setBooleanPreference(KEY_IS_SESSION, status.isSessionExpired)
                        send(Resource.Success(status.isSessionExpired))
                    } else {
                        send(Resource.Error(it.message))
                    }
                }
            }


        } catch (e: Exception) {
            send(Resource.Error(e.message))
        }
    }
}