package com.jerson.hcdc_portal.data.repository

import android.util.Log
import com.jerson.hcdc_portal.App
import com.jerson.hcdc_portal.domain.repository.EvaluationRepository
import com.jerson.hcdc_portal.util.AppPreference
import com.jerson.hcdc_portal.util.Constants
import com.jerson.hcdc_portal.util.Resource
import com.jerson.hcdc_portal.util.await
import com.jerson.hcdc_portal.util.getRequest
import com.jerson.hcdc_portal.util.isConnected
import com.jerson.hcdc_portal.util.sessionParse
import com.jerson.hcdc_portal.util.termLinksParse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import org.jsoup.Jsoup
import javax.inject.Inject

class EvaluationRepositoryImpl @Inject constructor(
    private val client: OkHttpClient,
    private val pref: AppPreference
) : EvaluationRepository {
    override suspend fun fetchEvaluation(): Flow<Resource<String>> = channelFlow {
        try {
            if (isConnected(App.appContext)) {
                withContext(Dispatchers.IO) {
                    send(Resource.Loading())
                    val response =
                        client.newCall(getRequest(Constants.baseUrl + Constants.evaluationsUrl))
                            .await()
                    if (response.isSuccessful) {
                        val bod = response.body.string()
                        val html = Jsoup.parse(bod)
                        if (sessionParse(pref, html))
                            send(Resource.Error("session end - ${response.code}"))
                        else {
                            val evalHtml = html.body().select("div.col-md-12 > div.tile").toString()
                            Log.e("HUHU", "fetchEvaluation: $evalHtml", )
                            pref.setStringPreference(Constants.KEY_HTML_EVALUATION, evalHtml)
                            send(Resource.Success(evalHtml))
                        }
                    } else {
                        send(Resource.Error(response.message))
                    }
                    response.body.close()
                }
            } else {
                send(Resource.Error("No internet connection!"))
            }
        } catch (e: Exception) {
            send(Resource.Error(e.message))
        }
    }
}