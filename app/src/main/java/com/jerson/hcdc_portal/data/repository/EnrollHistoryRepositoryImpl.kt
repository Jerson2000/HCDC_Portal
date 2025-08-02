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
import com.jerson.hcdc_portal.util.parseTerm
import com.jerson.hcdc_portal.util.sessionParse
import com.jerson.hcdc_portal.util.termLinksParse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
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
) : EnrollHistoryRepository {
    override suspend fun fetchEnrollHistory(): Flow<Resource<List<EnrollHistory>>> = channelFlow {
        try {
            withContext(Dispatchers.IO) {
                send(Resource.Loading())
                val response =
                    client.newCall(getRequest(Constants.baseUrl + Constants.enrollHistory)).await()

                response.use {
                    if (it.isSuccessful) {
                        val bod = it.body.string()
                        val html = Jsoup.parse(bod)
                        if (sessionParse(preference, html))
                            send(Resource.Error("session end - ${it.code}"))
                        else {
                            db.termDao().deleteAllTerm(0)
                            db.termDao().upsertTerm(termLinksParse(html, 0))
                            if (parseEnrollHistory(html, 0).isNotEmpty()) {
                                db.termDao().getTerms(0).collect { terms ->
                                    terms.firstOrNull { term ->
                                        term.term == parseEnrollHistory(
                                            html,
                                            0
                                        )[0].term
                                    }?.let { matchedTerm ->
                                        db.enrollHistoryDao().deleteAllHistory(matchedTerm.id)
                                        db.enrollHistoryDao()
                                            .upsertHistory(parseEnrollHistory(html, matchedTerm.id))
                                        preference.setIntPreference(
                                            Constants.KEY_SELECTED_ENROLL_HISTORY_TERM,
                                            matchedTerm.id
                                        )
                                        send(
                                            Resource.Success(
                                                parseEnrollHistory(
                                                    html,
                                                    matchedTerm.id
                                                )
                                            )
                                        )
                                    }
                                }
                                return@withContext
                            }
                            send(Resource.Success(parseEnrollHistory(html, 0)))
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

    override suspend fun fetchEnrollHistory(term: Term): Flow<Resource<List<EnrollHistory>>> =
        channelFlow {
            try {
                if (isConnected(App.appContext)) {
                    withContext(Dispatchers.IO) {
                        send(Resource.Loading())
                        val response =
                            client.newCall(getRequest(Constants.baseUrl + term.urlPath)).await()
                        if (response.isSuccessful) {
                            val bod = response.body.string()
                            val html = Jsoup.parse(bod)
                            if (sessionParse(preference, html))
                                send(Resource.Error("session end - ${response.code}"))
                            else {

                                db.enrollHistoryDao().deleteAllHistory(term.id)
                                db.enrollHistoryDao()
                                    .upsertHistory(parseEnrollHistory(html, term.id))
                                preference.setIntPreference(
                                    Constants.KEY_SELECTED_ENROLL_HISTORY_TERM,
                                    term.id
                                )
                                send(Resource.Success(parseEnrollHistory(html, term.id)))
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

    override suspend fun fetchEnrollHistoryTerm(): Flow<Resource<List<Term>>> {
        TODO("Not yet implemented")
    }

    override suspend fun getEnrollHistory(termId: Int): Flow<Resource<List<EnrollHistory>>> =
        channelFlow {
            send(Resource.Loading())
            db.enrollHistoryDao().getHistory(termId)
                .catch {
                    send(Resource.Error(it.message))
                }
                .collect {
                    send(Resource.Success(it))
                }
        }

    override suspend fun getEnrollHistoryTerms(): Flow<Resource<List<Term>>> = channelFlow {
        send(Resource.Loading())
        db.termDao().getTerms(0)
            .catch {
                send(Resource.Error(it.message))
            }
            .collect {list->
                val sortedList = list.sortedWith(compareByDescending<Term> {
                    parseTerm(it.term!!).second  // Sort by year descending
                }.thenBy {
                    parseTerm(it.term!!).first   // Then by semester ascending
                })
                send(Resource.Success(sortedList))
            }
    }

    override suspend fun hasData(term: Term, hasData: (Boolean) -> Unit) {
        db.enrollHistoryDao().getHistory(term.id)
            .catch {
                hasData(false)
            }.collect {
                hasData(it.isNotEmpty())
            }
    }

    private fun parseEnrollHistory(doc: Document, termId: Int): List<EnrollHistory> {
        val list = mutableListOf<EnrollHistory>()
        val tableBody = doc.select("div.col-md-9 table > tbody")
        val term = doc.select("li.nav-item a.nav-link.active").text()

        for (row in tableBody.select("tr")) {

            val offeredNo = row.select("td:eq(0)").text()
            val subjectCode = row.select("td:eq(1)").text()
            val description = row.select("td:eq(2)").text()
            val unit = row.select("td:eq(3)").text()

            val item = EnrollHistory(
                0,
                termId,
                term,
                offeredNo,
                subjectCode,
                description,
                unit
            )
            list.add(item)
        }


        return list
    }
}