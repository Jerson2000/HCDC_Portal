package com.jerson.hcdc_portal.data.repository

import com.jerson.hcdc_portal.App
import com.jerson.hcdc_portal.data.local.PortalDB
import com.jerson.hcdc_portal.domain.model.Account
import com.jerson.hcdc_portal.domain.repository.AccountsRepository
import com.jerson.hcdc_portal.util.AppPreference
import com.jerson.hcdc_portal.util.Constants
import com.jerson.hcdc_portal.util.Resource
import com.jerson.hcdc_portal.util.await
import com.jerson.hcdc_portal.util.getRequest
import com.jerson.hcdc_portal.util.isConnected
import com.jerson.hcdc_portal.util.sessionParse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
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
                            db.accountDao().deleteAllAccounts()
                            db.accountDao().upsertAccount(parseAccount(html))
                            send(Resource.Success(parseAccount(html)))
                        }
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

    private fun parseAccount(response:Document):List<Account>{
        val list = mutableListOf<Account>()
        val table = response.select("div.col-md-9 section.invoice tbody")
        for (tabData in table) {
            val rowsData = tabData.select("tr")
            val iDue = rowsData.size - 1
            var indexDue = 0
            val iData = rowsData.size - 2
            var indexData = 0
            var dueTex: String? = null
            var dueDat: String? = null

            // Get Due Payment
            for (row in rowsData) {
                indexDue++
                val dueText = row.select("td:eq(0)")
                val dueData = row.select("td:eq(1)")
                if (indexDue == iDue) {
                    dueTex = dueText.text()
                    dueDat = dueData.text()
                    break
                }
            }
            for (row in rowsData) {
                indexData++
                val date = row.select("td:eq(0)")
                val ref = row.select("td:eq(1)")
                val desc = row.select("td:eq(2)")
                val period = row.select("td:eq(3)")
                val added = row.select("td:eq(4)")
                val deducted = row.select("td:eq(5)")
                val runBal = row.select("td:eq(6)")
                val model = Account(
                    0,
                    date.text(),
                    ref.text(),
                    desc.text(),
                    period.text(),
                    added.text(),
                    deducted.text(),
                    runBal.text(),
                    dueTex,
                    dueDat
                )
                list.add(model)
                if (indexData == iData) {
                    break
                }
            }
        }


        return list
    }
}