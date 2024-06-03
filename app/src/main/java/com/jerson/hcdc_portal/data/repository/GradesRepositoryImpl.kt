package com.jerson.hcdc_portal.data.repository

import com.jerson.hcdc_portal.App
import com.jerson.hcdc_portal.data.local.PortalDB
import com.jerson.hcdc_portal.domain.model.Grade
import com.jerson.hcdc_portal.domain.model.Term
import com.jerson.hcdc_portal.domain.repository.GradesRepository
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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import javax.inject.Inject

class GradesRepositoryImpl @Inject constructor(
    private val client: OkHttpClient,
    private val db: PortalDB,
    private val preference: AppPreference
) : GradesRepository {
    override suspend fun fetchGrades(): Flow<Resource<List<Grade>>> = channelFlow{
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
                            db.termDao().deleteAllTerm(1);
                            db.termDao().upsertTerm(termLinksParse(html,1))
                            send(Resource.Success(parseGrades(html,0)))
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


    override suspend fun fetchGrades(term: Term): Flow<Resource<List<Grade>>> = channelFlow {
        try {
            if (isConnected(App.appContext)) {
                withContext(Dispatchers.IO) {
                    send(Resource.Loading())
                    val response =
                        client.newCall(getRequest(Constants.baseUrl + Constants.gradesUrl+term.urlPath)).await()
                    if (response.isSuccessful) {
                        val bod = response.body.string()
                        val html = Jsoup.parse(bod)
                        if (sessionParse(preference, html))
                            send(Resource.Error("session end - ${response.code}"))
                        else {
                            db.gradeDao().deleteAllGrades(term.id)
                            db.gradeDao().upsertGrade(parseGrades(html,term.id))
                            send(Resource.Success(parseGrades(html,term.id)))
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

    override suspend fun fetchGradesTerm(): Flow<Resource<List<Term>>> = channelFlow{
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
                            db.termDao().deleteAllTerm(1)
                            db.termDao().upsertTerm(termLinksParse(html,1))
                            send(Resource.Success(termLinksParse(html,1)))
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

    override suspend fun getGrades(term: Term): Flow<Resource<List<Grade>>> = channelFlow{
        send(Resource.Loading())
        db.gradeDao().getGrades(term.id)
            .catch {
                send(Resource.Error(it.message))
            }
            .collect{
                send(Resource.Success(it))
            }
    }

    override suspend fun getGradeTerms(): Flow<Resource<List<Term>>> = channelFlow{
        send(Resource.Loading())
        db.termDao().getTerms(1)
            .catch {
                send(Resource.Error(it.message))
            }
            .collect{
                send(Resource.Success(it))
            }
    }

    private fun parseGrades(doc:Document,termId:Int):List<Grade>{
        val list = mutableListOf<Grade>()

        val table = doc.select("div.col-md-9 tbody")
        val weightedAve = table.select("tr:nth-last-child(2) > td:eq(3)").text()
        val earnedUnits = table.select("tr:nth-last-child(2) > td:eq(1)").text()
        val term = doc.select("li.nav-item a.nav-link.active").text()

        /*
        *
        *  Elements code = rowData.select("td:eq(0)");
                Elements subject = rowData.select("td:eq(1)");
                Elements desc = rowData.select("td:eq(2)");
                Elements unit = rowData.select("td:eq(3)");
                Elements midgrade = rowData.select("td:eq(4)");
                Elements mideremark = rowData.select("td:eq(5)");
                Elements finalgrade = rowData.select("td:eq(6)");
                Elements finalremark = rowData.select("td:eq(7)");
                Elements teacher = rowData.select("td:eq(8)");
        *
        * */

        val rows = table.select("tr")
        val excludedRows = rows.subList(0, rows.size - 3)
        for (x in excludedRows){
            list.add(
                Grade(
                    0,
                    termId,
                    term,
                    x.select("td:eq(0)").text(),
                    x.select("td:eq(1)").text(),
                    x.select("td:eq(2)").text(),
                    x.select("td:eq(3)").text(),
                    x.select("td:eq(4)").text(),
                    x.select("td:eq(5)").text(),
                    x.select("td:eq(6)").text(),
                    x.select("td:eq(7)").text(),
                    earnedUnits,
                    weightedAve,
                    x.select("td:eq(8)").text(),
                )
            )
        }

        return list
    }
}