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
                    val existingTerms = db.termDao().getTerms(1).firstOrNull().orEmpty()
                    val termsToSave = terms.map { parsed ->
                        val existing = existingTerms.firstOrNull { it.term == parsed.term }
                        if (existing != null) parsed.copy(id = existing.id) else parsed
                    }
                    db.termDao().replaceTerms(1, termsToSave)
                }
                if (initialGrades.isNotEmpty()) {
                    val targetTermName = initialGrades.firstOrNull()?.term
                    val savedTerms = db.termDao().getTerms(1).firstOrNull().orEmpty()
                    val matchedTerm = savedTerms.firstOrNull { it.term == targetTermName }
                    val finalTermId = matchedTerm?.id ?: if (selectedTermId != 0) selectedTermId else (savedTerms.firstOrNull()?.id ?: 0)
                    val finalGrades = initialGrades.map { it.copy(termId = finalTermId) }
                    db.gradeDao().replaceGrades(finalTermId, finalGrades)
                    if (finalTermId != 0) {
                        preference.setIntPreference(Constants.KEY_SELECT_GRADE_TERM, finalTermId)
                    }
                }
            }
        )
    }

    override suspend fun fetchGrades(term: Term): Flow<Resource<List<Grade>>> {
        if (!term.term.isNullOrBlank()) {
            val legacy = runCatching { db.gradeDao().getGradesByTermName(term.term).firstOrNull().orEmpty() }.getOrNull().orEmpty()
            if (legacy.isNotEmpty()) {
                db.gradeDao().updateTermId(term.id, term.term)
            }
        }
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
                            val existingTerms = db.termDao().getTerms(1).firstOrNull().orEmpty()
                            val termsToSave = terms.map { parsed ->
                                val existing = existingTerms.firstOrNull { it.term == parsed.term }
                                if (existing != null) parsed.copy(id = existing.id) else parsed
                            }
                            db.termDao().replaceTerms(1, termsToSave)
                            send(Resource.Success(termsToSave))
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
            .collect { list ->
                if (list.isEmpty()) {
                    val termObj = db.termDao().getTerms(1).firstOrNull()?.firstOrNull { it.id == termId }
                    if (!termObj?.term.isNullOrBlank()) {
                        val legacy = db.gradeDao().getGradesByTermName(termObj!!.term!!).firstOrNull().orEmpty()
                        if (legacy.isNotEmpty()) {
                            db.gradeDao().updateTermId(termId, termObj.term!!)
                            send(Resource.Success(legacy))
                            return@collect
                        }
                    }
                }
                send(Resource.Success(list))
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
        val existing = runCatching {
            val list = db.gradeDao().getGrades(term.id).firstOrNull()
            if (!list.isNullOrEmpty()) {
                list
            } else if (!term.term.isNullOrBlank()) {
                val legacy = db.gradeDao().getGradesByTermName(term.term).firstOrNull()
                if (!legacy.isNullOrEmpty()) {
                    db.gradeDao().updateTermId(term.id, term.term)
                    legacy
                } else null
            } else null
        }.getOrNull()
        hasData(!existing.isNullOrEmpty())
    }
}