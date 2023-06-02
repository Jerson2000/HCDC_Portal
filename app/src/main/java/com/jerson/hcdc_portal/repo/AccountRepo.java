package com.jerson.hcdc_portal.repo;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.jerson.hcdc_portal.PortalApp;
import com.jerson.hcdc_portal.listener.OnHttpResponseListener;
import com.jerson.hcdc_portal.model.AccountLinksModel;
import com.jerson.hcdc_portal.model.AccountModel;
import com.jerson.hcdc_portal.network.HttpClient;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class AccountRepo {


    public LiveData<List<AccountModel>> getData(String link, MutableLiveData<String> response, MutableLiveData<Integer> resCode){
        MutableLiveData<List<AccountModel>> data = new MutableLiveData<>();
        HttpClient.getInstance().GET(PortalApp.baseUrl + link, new OnHttpResponseListener<Document>() {
            @Override
            public void onResponse(Document response) {

                System.out.println(response.body());
                List<AccountModel> accounts = new ArrayList<>();

                Elements table = response.select("div.col-md-9 section.invoice tbody");


                for (Element tabData : table) {
                    Elements rowsData = tabData.select("tr");

                    int iDue = rowsData.size() - 1;
                    int indexDue = 0;
                    int iData = rowsData.size() - 2;
                    int indexData = 0;
                    String dueTex = null;
                    String dueDat = null;

//                    System.out.println(rowsData.text());

                    // Get Due Payment
                    for (Element row : rowsData) {
                        indexDue++;
                        Elements dueText = row.select("td:eq(0)");
                        Elements dueData = row.select("td:eq(1)");

                        if (indexDue == iDue) {

                            dueTex = dueText.text();
                            dueDat = dueData.text();
                            break;
                        }
                    }

                    for (Element row : rowsData) {
                        indexData++;
                        Elements date = row.select("td:eq(0)");
                        Elements ref = row.select("td:eq(1)");
                        Elements desc = row.select("td:eq(2)");
                        Elements period = row.select("td:eq(3)");
                        Elements added = row.select("td:eq(4)");
                        Elements deducted = row.select("td:eq(5)");
                        Elements runBal = row.select("td:eq(6)");

                        AccountModel model = new AccountModel
                                (
                                        date.text(),
                                        ref.text(),
                                        desc.text(),
                                        period.text(),
                                        added.text(),
                                        deducted.text(),
                                        runBal.text(),
                                        dueTex,
                                        dueDat

                                );
                        accounts.add(model);

                        if (indexData == iData) {
                            break;
                        }

                    }

                }

                data.setValue(accounts);

            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
                response.setValue(e.getMessage());
            }

            @Override
            public void onResponseCode(int code) {
                resCode.setValue(code);
            }
        });

        return data;
    }

    public LiveData<List<AccountModel>> getData(){
        MutableLiveData<List<AccountModel>> data = new MutableLiveData<>();
        HttpClient.getInstance().GET(PortalApp.baseUrl + PortalApp.accountUrl, new OnHttpResponseListener<Document>() {
            @Override
            public void onResponse(Document response) {

                List<AccountModel> accounts = new ArrayList<>();

                Elements table = response.select("div.col-md-9 section.invoice tbody");


                for (Element tabData : table) {
                    Elements rowsData = tabData.select("tr");

                    int iDue = rowsData.size() - 1;
                    int indexDue = 0;
                    int iData = rowsData.size() - 2;
                    int indexData = 0;
                    String dueTex = null;
                    String dueDat = null;

//                    System.out.println(rowsData.text());

                    // Get Due Payment
                    for (Element row : rowsData) {
                        indexDue++;
                        Elements dueText = row.select("td:eq(0)");
                        Elements dueData = row.select("td:eq(1)");

                        if (indexDue == iDue) {

                            dueTex = dueText.text();
                            dueDat = dueData.text();
                            break;
                        }
                    }

                    for (Element row : rowsData) {
                        indexData++;
                        Elements date = row.select("td:eq(0)");
                        Elements ref = row.select("td:eq(1)");
                        Elements desc = row.select("td:eq(2)");
                        Elements period = row.select("td:eq(3)");
                        Elements added = row.select("td:eq(4)");
                        Elements deducted = row.select("td:eq(5)");
                        Elements runBal = row.select("td:eq(6)");

                        AccountModel model = new AccountModel
                                (
                                        date.text(),
                                        ref.text(),
                                        desc.text(),
                                        period.text(),
                                        added.text(),
                                        deducted.text(),
                                        runBal.text(),
                                        dueTex,
                                        dueDat

                                );
                        accounts.add(model);

                        if (indexData == iData) {
                            break;
                        }

                    }

                }

                data.setValue(accounts);

            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();

            }

            @Override
            public void onResponseCode(int code) {

            }
        });

        return data;
    }
}
