package com.jerson.hcdc_portal.data.repository

import com.jerson.hcdc_portal.App
import com.jerson.hcdc_portal.data.local.PortalDB
import com.jerson.hcdc_portal.domain.model.EnrollHistory
import com.jerson.hcdc_portal.domain.model.Term
import com.jerson.hcdc_portal.domain.repository.EnrollHistoryRepository
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
import org.jsoup.nodes.Document
import javax.inject.Inject

class EnrollHistoryRepositoryImpl @Inject constructor(
    private val client: OkHttpClient,
    private val db: PortalDB,
    private val preference: AppPreference
):EnrollHistoryRepository {
    override suspend fun fetchEnrollHistory(): Flow<Resource<List<EnrollHistory>>> = channelFlow{
        try {
            if (isConnected(App.appContext)) {
                withContext(Dispatchers.IO) {
                    send(Resource.Loading())
                    val response =
                        client.newCall(getRequest(Constants.baseUrl + Constants.gradesUrl)).await()
                    if (response.isSuccessful) {
                        val bod = response.body.string()
                        val html = Jsoup.parse(bod)
                        if (sessionParse(preference, html))
                            send(Resource.Error("session end - ${response.code}"))
                        else {
                            db.termDao().deleteAllTerm(0);
                            db.termDao().upsertTerm(termLinksParse(html,0))
                            send(Resource.Success(parseEnrollHistory(html,0)))
                        }
                    } else {
                        send(Resource.Error(response.message))
                    }
                }
            } else {
                send(Resource.Error("No internet connection!"))
            }
        } catch (e: Exception) {
            send(Resource.Error(e.message))
        }
    }

    override suspend fun fetchEnrollHistory(term: Term): Flow<Resource<List<EnrollHistory>>> {
        TODO("Not yet implemented")
    }

    override suspend fun fetchEnrollHistoryTerm(): Flow<Resource<List<Term>>> {
        TODO("Not yet implemented")
    }

    override suspend fun getEnrollHistory(term: Term): Flow<Resource<List<EnrollHistory>>> {
        TODO("Not yet implemented")
    }

    override suspend fun getEnrollHistoryTerms(): Flow<Resource<List<Term>>> {
        TODO("Not yet implemented")
    }

    private fun parseEnrollHistory(doc: Document,termId: Int):List<EnrollHistory>{
        return mutableListOf()
    }
}