package com.jerson.hcdc_portal.repo;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.jerson.hcdc_portal.PortalApp;
import com.jerson.hcdc_portal.listener.OnHttpResponseListener;
import com.jerson.hcdc_portal.model.GradeLinksModel;
import com.jerson.hcdc_portal.model.GradeModel;
import com.jerson.hcdc_portal.network.HttpClient;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class GradeRepo {

    public LiveData<List<GradeLinksModel>> getLinks( MutableLiveData<String> response,MutableLiveData<Integer> resCode){
        MutableLiveData<List<GradeLinksModel>> data = new MutableLiveData<>();

        HttpClient.getInstance().GET(PortalApp.baseUrl + PortalApp.gradesUrl, new OnHttpResponseListener<Document>() {
            @Override
            public void onResponse(Document response) {
                List<GradeLinksModel> gradesLinks = new ArrayList<>();
                Elements semList = response.select("main.app-content ul li.nav-item");

                for (Element list : semList) {
                    String link = list.select("a.nav-link").attr("href");
                    String text = list.select("a.nav-link").text();
                    GradeLinksModel model = new GradeLinksModel(link, text);
                    gradesLinks.add(model);
                }

                data.setValue(gradesLinks);
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

    public LiveData<List<GradeModel>> gradeData (String link,MutableLiveData<String> response,MutableLiveData<Integer> resCode){
        MutableLiveData<List<GradeModel>> dat = new MutableLiveData<>();

        HttpClient.getInstance().GET(PortalApp.baseUrl+ link, new OnHttpResponseListener<Document>() {
            @Override
            public void onResponse(Document response) {
                List<GradeModel> gradeModelList = new ArrayList<>();
                Elements table = response.select("div.col-md-9 tbody");


                int i = 0;
                for (Element list : table) {
                    Elements tableData = list.select("tr");
                    Elements spans = list.select("span");


                    int rows = tableData.size() - 3;
                    int rows2 = tableData.size() - 1;
                    int i2 = 0;
                    String earnText, earn = null, aveText, ave = null;


                    // GET AVERAGE and EARNED UNITS
                    for (Element rowData : tableData) {

                        Elements earnUnitsText = rowData.select("td:eq(0)");
                        Elements earnUnits = rowData.select("td:eq(1)");
                        Elements averageText = rowData.select("td:eq(2)");
                        Elements average = rowData.select("td:eq(3)");

                        earnText = earnUnitsText.text();
                        earn = earnUnits.text();
                        aveText = averageText.text();
                        ave = average.text();

                        i2++;

                        if (i2 == rows2) {
//                            System.out.println(earnText);
//                            System.out.println(earn);
//                            System.out.println(aveText);
//                            System.out.println(ave);

                            break;
                        }

                    }


                    // GET ALL SUBJECTS GRADES
                    for (Element rowData : tableData) {
                        i++;

                        Elements code = rowData.select("td:eq(0)");
                        Elements subject = rowData.select("td:eq(1)");
                        Elements desc = rowData.select("td:eq(2)");
                        Elements unit = rowData.select("td:eq(3)");
                        Elements midgrade = rowData.select("td:eq(4)");
                        Elements mideremark = rowData.select("td:eq(5)");
                        Elements finalgrade = rowData.select("td:eq(6)");
                        Elements finalremark = rowData.select("td:eq(7)");
                        Elements teacher = rowData.select("td:eq(8)");

                        GradeModel model = new GradeModel
                                (
                                        code.text(),
                                        subject.text(),
                                        desc.text(),
                                        unit.text(),
                                        midgrade.text(),
                                        mideremark.text(),
                                        finalgrade.text(),
                                        finalremark.text(),
                                        teacher.text(),
                                        earn != null?earn:"null",
                                        ave != null?ave:"null"
                                );
                        gradeModelList.add(model);

                        if (i == rows) {

                            break;
                        }

                    }

                }
                dat.setValue(gradeModelList);
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

        return dat;
    }
}
