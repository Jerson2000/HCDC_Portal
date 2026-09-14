package com.jerson.hcdc_portal.data.repository

import com.jerson.hcdc_portal.data.local.PortalDB
import com.jerson.hcdc_portal.data.parser.AccountParser
import com.jerson.hcdc_portal.data.parser.UserSessionParser
import com.jerson.hcdc_portal.domain.model.Account
import com.jerson.hcdc_portal.domain.model.Term
import com.jerson.hcdc_portal.domain.repository.AccountsRepository
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

class AccountsRepositoryImpl @Inject constructor(
    private val db: PortalDB,
    private val client: OkHttpClient,
    private val preference: AppPreference,
    private val accountParser: AccountParser,
    private val sessionParser: UserSessionParser
) : AccountsRepository {

    override suspend fun fetchAccounts(): Flow<Resource<List<Account>>> {
        val selectedTermId = preference.getIntPreference(Constants.KEY_SELECTED_ACCOUNT_TERM)
        return networkBoundResource(
            query = {
                if (selectedTermId != 0) db.accountDao().getAccounts(selectedTermId)
                else db.accountDao().getAccounts()
            },
            fetch = {
                withContext(Dispatchers.IO) {
                    val response = client.newCall(getRequest(Constants.baseUrl + Constants.accountUrl)).await()
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

                        val terms = accountParser.parseTerms(doc)
                        val initialAccounts = accountParser.parseAccounts(doc, 0)
                        Pair(terms, initialAccounts)
                    }
                }
            },
            saveFetchResult = { (terms, initialAccounts) ->
                if (terms.isNotEmpty()) {
                    val existingTerms = db.termDao().getTerms(2).firstOrNull().orEmpty()
                    val termsToSave = terms.map { parsed ->
                        val existing = existingTerms.firstOrNull { it.term == parsed.term }
                        if (existing != null) parsed.copy(id = existing.id) else parsed
                    }
                    db.termDao().replaceTerms(2, termsToSave)
                }
                if (initialAccounts.isNotEmpty()) {
                    val targetTermName = initialAccounts.firstOrNull()?.term
                    val savedTerms = db.termDao().getTerms(2).firstOrNull().orEmpty()
                    val matchedTerm = savedTerms.firstOrNull { it.term == targetTermName }
                    val finalTermId = matchedTerm?.id ?: if (selectedTermId != 0) selectedTermId else (savedTerms.firstOrNull()?.id ?: 0)
                    val finalAccounts = initialAccounts.map { it.copy(termId = finalTermId) }
                    db.accountDao().replaceAccounts(finalTermId, finalAccounts)
                    if (finalTermId != 0) {
                        preference.setIntPreference(Constants.KEY_SELECTED_ACCOUNT_TERM, finalTermId)
                    }
                }
            }
        )
    }

    override suspend fun fetchAccounts(term: Term): Flow<Resource<List<Account>>> {
        if (!term.term.isNullOrBlank()) {
            val legacy = runCatching { db.accountDao().getAccountsByTermName(term.term).firstOrNull().orEmpty() }.getOrNull().orEmpty()
            if (legacy.isNotEmpty()) {
                db.accountDao().updateTermId(term.id, term.term)
            }
        }
        return networkBoundResource(
            query = { db.accountDao().getAccounts(term.id) },
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

                        accountParser.parseAccounts(doc, term.id)
                    }
                }
            },
            saveFetchResult = { accounts ->
                db.accountDao().replaceAccounts(term.id, accounts)
                preference.setIntPreference(Constants.KEY_SELECTED_ACCOUNT_TERM, term.id)
            }
        )
    }

    override suspend fun getAccounts(): Flow<Resource<List<Account>>> = channelFlow {
        send(Resource.Loading())
        db.accountDao().getAccounts()
            .catch {
                send(Resource.Error(it.message))
            }
            .collect {
                send(Resource.Success(it))
            }
    }

    override suspend fun getAccounts(termId: Int): Flow<Resource<List<Account>>> = channelFlow {
        send(Resource.Loading())
        db.accountDao().getAccounts(termId)
            .catch {
                send(Resource.Error(it.message))
            }
            .collect { list ->
                if (list.isEmpty()) {
                    val termObj = db.termDao().getTerms(2).firstOrNull()?.firstOrNull { it.id == termId }
                    if (!termObj?.term.isNullOrBlank()) {
                        val legacy = db.accountDao().getAccountsByTermName(termObj!!.term!!).firstOrNull().orEmpty()
                        if (legacy.isNotEmpty()) {
                            db.accountDao().updateTermId(termId, termObj.term!!)
                            send(Resource.Success(legacy))
                            return@collect
                        }
                    }
                }
                send(Resource.Success(list))
            }
    }

    override suspend fun getAccountTerm(): Flow<Resource<List<Term>>> = channelFlow {
        send(Resource.Loading())
        db.termDao().getTerms(2)
            .catch {
                send(Resource.Error(it.message))
            }
            .collect {
                send(Resource.Success(it))
            }
    }

    override suspend fun hasData(term: Term, hasData: (Boolean) -> Unit) {
        val existing = runCatching {
            val list = db.accountDao().getAccounts(term.id).firstOrNull()
            if (!list.isNullOrEmpty()) {
                list
            } else if (!term.term.isNullOrBlank()) {
                val legacy = db.accountDao().getAccountsByTermName(term.term).firstOrNull()
                if (!legacy.isNullOrEmpty()) {
                    db.accountDao().updateTermId(term.id, term.term)
                    legacy
                } else null
            } else null
        }.getOrNull()
        hasData(!existing.isNullOrEmpty())
    }
}