package com.jerson.hcdc_portal.data.repository

import com.jerson.hcdc_portal.data.local.PortalDB
import com.jerson.hcdc_portal.data.parser.GradeParser
import com.jerson.hcdc_portal.data.parser.UserSessionParser
import com.jerson.hcdc_portal.domain.model.Grade
import com.jerson.hcdc_portal.domain.model.Term
import com.jerson.hcdc_portal.domain.repository.GradesRepository
import com.jerson.hcdc_portal.util.AppPreference
import com.jerson.hcdc_portal.util.Constants
import com.jerson.hcdc_portal.util.Constants.KEY_CSRF_TOKEN
import com.jerson.hcdc_portal.util.Constants.KEY_IS_SESSION
import com.jerson.hcdc_portal.util.Resource
import com.jerson.hcdc_portal.util.await
import com.jerson.hcdc_portal.util.getRequest
import com.jerson.hcdc_portal.util.networkBoundResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import org.jsoup.Jsoup
import java.io.IOException
import javax.inject.Inject

class GradesRepositoryImpl @Inject constructor(
    private val client: OkHttpClient,
    private val db: PortalDB,
    private val preference: AppPreference,
    private val gradeParser: GradeParser,
    private val sessionParser: UserSessionParser
) : GradesRepository {

    override suspend fun fetchGrades(): Flow<Resource<List<Grade>>> {
        val selectedTermId = preference.getIntPreference(Constants.KEY_SELECT_GRADE_TERM)
        return networkBoundResource(
            query = { db.gradeDao().getGrades(selectedTermId) },
            fetch = {
                withContext(Dispatchers.IO) {
                    val response = client.newCall(getRequest(Constants.baseUrl + Constants.gradesUrl)).await()
                    response.use { res ->
                        if (!res.isSuccessful) {
                            throw IOException("HTTP error ${res.code}: ${res.message}")
                        }
                        val htmlString = res.body?.string().orEmpty()
                        val doc = Jsoup.parse(htmlString)

                        val sessionStatus = sessionParser.parseSession(doc)
                        preference.setStringPreference(KEY_CSRF_TOKEN, sessionStatus.csrfToken)
                        preference.setBooleanPreference(KEY_IS_SESSION, sessionStatus.isSessionExpired)
                        if (sessionStatus.isSessionExpired) {
                            throw IOException("session end - ${res.code}")
                        }

                        val terms = gradeParser.parseTerms(doc)
                        val initialGrades = gradeParser.parseGrades(doc, 0)
                        Pair(terms, initialGrades)
                    }
                }
            },
            saveFetchResult = { (terms, initialGrades) ->
                if (terms.isNotEmpty()) {
                    db.termDao().replaceTerms(1, terms)
                }
                if (initialGrades.isNotEmpty()) {
                    val targetTermName = initialGrades.firstOrNull()?.term
                    val matchedTerm = terms.firstOrNull { it.term == targetTermName }
                        ?: db.termDao().getTerms(1).firstOrNull()?.firstOrNull { it.term == targetTermName }
                    val finalTermId = matchedTerm?.id ?: selectedTermId
                    val finalGrades = initialGrades.map { it.copy(termId = finalTermId) }
                    db.gradeDao().replaceGrades(finalTermId, finalGrades)
                    if (matchedTerm != null) {
                        preference.setIntPreference(Constants.KEY_SELECT_GRADE_TERM, matchedTerm.id)
                    }
                }
            }
        )
    }

    override suspend fun fetchGrades(term: Term): Flow<Resource<List<Grade>>> {
        return networkBoundResource(
            query = { db.gradeDao().getGrades(term.id) },
            fetch = {
                withContext(Dispatchers.IO) {
                    val response = client.newCall(getRequest(Constants.baseUrl + term.urlPath)).await()
                    response.use { res ->
                        if (!res.isSuccessful) {
                            throw IOException("HTTP error ${res.code}: ${res.message}")
                        }
                        val htmlString = res.body?.string().orEmpty()
                        val doc = Jsoup.parse(htmlString)

                        val sessionStatus = sessionParser.parseSession(doc)
                        preference.setStringPreference(KEY_CSRF_TOKEN, sessionStatus.csrfToken)
                        preference.setBooleanPreference(KEY_IS_SESSION, sessionStatus.isSessionExpired)
                        if (sessionStatus.isSessionExpired) {
                            throw IOException("session end - ${res.code}")
                        }

                        gradeParser.parseGrades(doc, term.id)
                    }
                }
            },
            saveFetchResult = { grades ->
                db.gradeDao().replaceGrades(term.id, grades)
                preference.setIntPreference(Constants.KEY_SELECT_GRADE_TERM, term.id)
            }
        )
    }

    override suspend fun fetchGradesTerm(): Flow<Resource<List<Term>>> = channelFlow {
        try {
            withContext(Dispatchers.IO) {
                send(Resource.Loading())
                val response = client.newCall(getRequest(Constants.baseUrl + Constants.gradesUrl)).await()
                response.use { res ->
                    if (res.isSuccessful) {
                        val bod = res.body?.string().orEmpty()
                        val html = Jsoup.parse(bod)
                        val sessionStatus = sessionParser.parseSession(html)
                        preference.setStringPreference(KEY_CSRF_TOKEN, sessionStatus.csrfToken)
                        preference.setBooleanPreference(KEY_IS_SESSION, sessionStatus.isSessionExpired)
                        if (sessionStatus.isSessionExpired) {
                            send(Resource.Error("session end - ${res.code}"))
                        } else {
                            val terms = gradeParser.parseTerms(html)
                            db.termDao().replaceTerms(1, terms)
                            send(Resource.Success(terms))
                        }
                    } else {
                        send(Resource.Error(res.message))
                    }
                }
            }
        } catch (e: Exception) {
            send(Resource.Error(e.message))
        }
    }

    override suspend fun getGrades(termId: Int): Flow<Resource<List<Grade>>> = channelFlow {
        send(Resource.Loading())
        db.gradeDao().getGrades(termId)
            .catch {
                send(Resource.Error(it.message))
            }
            .collect {
                send(Resource.Success(it))
            }
    }

    override suspend fun getGradeTerms(): Flow<Resource<List<Term>>> = channelFlow {
        send(Resource.Loading())
        db.termDao().getTerms(1)
            .catch {
                send(Resource.Error(it.message))
            }
            .collect {
                send(Resource.Success(it))
            }
    }

    override suspend fun hasData(term: Term, hasData: (Boolean) -> Unit) {
        val existing = runCatching { db.gradeDao().getGrades(term.id).firstOrNull() }.getOrNull()
        hasData(!existing.isNullOrEmpty())
    }
}