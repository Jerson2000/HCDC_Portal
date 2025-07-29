package com.jerson.hcdc_portal.data.repository

import com.jerson.hcdc_portal.domain.repository.EvaluationRepository
import com.jerson.hcdc_portal.util.AppPreference
import com.jerson.hcdc_portal.util.Constants
import com.jerson.hcdc_portal.util.Resource
import com.jerson.hcdc_portal.util.await
import com.jerson.hcdc_portal.util.getRequest
import com.jerson.hcdc_portal.util.sessionParse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import javax.inject.Inject

class EvaluationRepositoryImpl @Inject constructor(
    private val client: OkHttpClient,
    private val pref: AppPreference
) : EvaluationRepository {
    override suspend fun fetchEvaluation(): Flow<Resource<String>> = channelFlow {
        try {
            withContext(Dispatchers.IO) {
                send(Resource.Loading())
                val response =
                    client.newCall(getRequest(Constants.baseUrl + Constants.evaluationsUrl))
                        .await()

                response.use{
                    if (it.isSuccessful) {
                        val bod = it.body.string()
                        val html = Jsoup.parse(bod)
                        if (sessionParse(pref, html))
                            send(Resource.Error("session end - ${it.code}"))
                        else {
                            val evalHtml = html.body().select("div.col-md-12 > div.tile").toString()
                            pref.setStringPreference(Constants.KEY_HTML_EVALUATION, evalHtml)
                            send(Resource.Success(evalHtml))
                        }
                    } else {
                        send(Resource.Error(it.message))
                    }
                }

            }
        } catch (e: Exception) {
            send(Resource.Error(e.message))
        }
    }

    override suspend fun fetchLacking(): Flow<Resource<String>> = channelFlow {
        try {
            withContext(Dispatchers.IO) {
                send(Resource.Loading())
                val response =
                    client.newCall(getRequest(Constants.baseUrl + Constants.lackingCredential))
                        .await()
                if (response.isSuccessful) {
                    val bod = response.body.string()
                    val html = Jsoup.parse(bod)
                    if (sessionParse(pref, html))
                        send(Resource.Error("session end - ${response.code}"))
                    else {
                        val res = StringBuilder()
                        val lacking: Elements = html.select("main.app-content > div.row")
                        val noLack: Boolean =
                            html.body().select("main.app-content div.row center h2").hasText()
                        if (noLack) {
                            res.append(
                                html.body().select("main.app-content div.row center h2").text()
                            )
                        } else {
                            for (s in lacking.select("div.info")) {
                                res.append(s.select("h4").text()).append(":")
                            }
                        }
                        send(Resource.Success(res.toString()))
                    }
                } else {
                    send(Resource.Error(response.message))
                }
                response.body.close()
            }
        } catch (e: Exception) {
            send(Resource.Error(e.message))
        }
    }
}