package com.jerson.hcdc_portal.data.repository

import android.util.Log
import com.jerson.hcdc_portal.App
import com.jerson.hcdc_portal.data.local.PortalDB
import com.jerson.hcdc_portal.domain.model.Account
import com.jerson.hcdc_portal.domain.model.Term
import com.jerson.hcdc_portal.domain.repository.AccountsRepository
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

class AccountsRepositoryImpl @Inject constructor(
    private val db: PortalDB,
    private val client: OkHttpClient,
    private val preference: AppPreference
):AccountsRepository {

    override suspend fun fetchAccounts(): Flow<Resource<List<Account>>> = channelFlow{
        try{
            if(isConnected(App.appContext)){
                withContext(Dispatchers.IO){
                    send(Resource.Loading())
                    val response = client.newCall(getRequest(Constants.baseUrl+Constants.accountUrl)).await()
                    if(response.isSuccessful){
                        val bod = response.body.string()
                        val html = Jsoup.parse(bod)
                        if(sessionParse(preference,html))
                            send(Resource.Error("session end - ${response.code}"))
                        else{
                            db.termDao().deleteAllTerm(2)
                            db.termDao().upsertTerm(termLinksParse(html, 2))
                            if (parseAccount(html, 0).isNotEmpty()) {
                                db.termDao().getTerms(2).collect {
                                    for (x in it) {
                                        if (x.term == parseAccount(html, 0)[0].term) {
                                            db.accountDao().deleteAccount(x.id)
                                            db.accountDao().upsertAccount(parseAccount(html, x.id))
                                            preference.setIntPreference(Constants.KEY_SELECTED_ACCOUNT_TERM,x.id)
                                            send(Resource.Success(parseAccount(html,x.id)))
                                            break
                                        }
                                    }
                                }
                            }
                            send(Resource.Success(parseAccount(html,0)))

                        }
                        response.body.close()
                    }else{
                        send(Resource.Error(response.message))
                    }
                }
            }else{
                send(Resource.Error("No internet connection!"))
            }
        }catch (e:Exception){
            send(Resource.Error(e.message))
        }
    }

    override suspend fun fetchAccounts(term: Term): Flow<Resource<List<Account>>> = channelFlow{
        try{
            if(isConnected(App.appContext)){
                withContext(Dispatchers.IO){
                    send(Resource.Loading())
                    val response = client.newCall(getRequest(Constants.baseUrl+Constants.accountUrl+term.urlPath)).await()
                    if(response.isSuccessful){
                        val bod = response.body.string()
                        val html = Jsoup.parse(bod)
                        if(sessionParse(preference,html))
                            send(Resource.Error("session end - ${response.code}"))
                        else{
                            db.accountDao().deleteAccount(term.id)
                            db.accountDao().upsertAccount(parseAccount(html, term.id))
                            preference.setIntPreference(Constants.KEY_SELECTED_ACCOUNT_TERM,term.id)
                            send(Resource.Success(parseAccount(html,term.id)))

                        }
                        response.body.close()
                    }else{
                        send(Resource.Error(response.message))
                    }
                }
            }else{
                send(Resource.Error("No internet connection!"))
            }
        }catch (e:Exception){
            send(Resource.Error(e.message))
        }
    }

    override suspend fun getAccounts(): Flow<Resource<List<Account>>> = channelFlow{
        send(Resource.Loading())
        db.accountDao().getAccounts()
            .catch {
                send(Resource.Error(it.message))
            }
            .collect{
                send(Resource.Success(it))
            }
    }

    override suspend fun getAccounts(termId: Int): Flow<Resource<List<Account>>> = channelFlow{
        send(Resource.Loading())
        db.accountDao().getAccounts(termId)
            .catch {
                send(Resource.Error(it.message))
            }
            .collect{
                Log.e("HUHU", "getAccounts: Term-ID: ${preference.getIntPreference(Constants.KEY_SELECTED_ACCOUNT_TERM)}", )
                send(Resource.Success(it))
            }
    }

    private fun parseAccount(response:Document,termId:Int):List<Account>{
        val list = mutableListOf<Account>()
        val table = response.select("div.col-md-9 section.invoice table > tbody")
        val term = response.select("li.nav-item a.nav-link.active").text()
        val dueText = response.select("tbody tr:nth-last-child(2) > td:eq(0)").text()
        val dueAmount = response.select("tbody tr:nth-last-child(2) > td:eq(1)").text()

        val rows = table.select("tr")
        val excludedRows = rows.subList(0, rows.size - 2)

            for (row in excludedRows) {
                val date = row.select("td:eq(0)")
                val ref = row.select("td:eq(1)")
                val desc = row.select("td:eq(2)")
                val period = row.select("td:eq(3)")
                val added = row.select("td:eq(4)")
                val deducted = row.select("td:eq(5)")
                val runBal = row.select("td:eq(6)")
                val model = Account(
                    0,
                    termId,
                    term,
                    date.text(),
                    ref.text(),
                    desc.text(),
                    period.text(),
                    added.text(),
                    deducted.text(),
                    runBal.text(),
                    dueText,
                    dueAmount
                )
                list.add(model)
            }
        if(list.size == 0 && response.select("li.nav-item a").hasClass("active")){
            list.add(
                Account(
                    0,
                    termId,
                    term,
                   "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    dueText,
                    dueAmount
            )
            )
        }


        return list
    }
}