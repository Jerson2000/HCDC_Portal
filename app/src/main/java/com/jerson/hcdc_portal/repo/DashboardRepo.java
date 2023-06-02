package com.jerson.hcdc_portal.repo;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.jerson.hcdc_portal.PortalApp;
import com.jerson.hcdc_portal.listener.OnHttpResponseListener;
import com.jerson.hcdc_portal.model.DashboardModel;
import com.jerson.hcdc_portal.network.HttpClient;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class DashboardRepo {

    public LiveData<List<DashboardModel>> getDashData(MutableLiveData<Integer> resCode,MutableLiveData<String> response){
        MutableLiveData<List<DashboardModel>> data = new MutableLiveData<>();
        HttpClient.getInstance().GET(PortalApp.baseUrl+PortalApp.dashboardUrl, new OnHttpResponseListener<Document>() {
            @Override
            public void onResponse(Document response) {
                data.setValue( parseDashboard(response));
            }

            @Override
            public void onFailure(Exception e) {
                response.setValue(e.getMessage());
            }

            @Override
            public void onResponseCode(int code) {
                resCode.setValue(code);
            }
        });
        return data;

    }

    public static List<DashboardModel> parseDashboard(Document response){
        List<DashboardModel> dashList = new ArrayList<>();

        Elements dashboardTable = response.select("div.col-sm-9 tbody");
        for (Element list : dashboardTable) {
            Elements tableData = list.select("tr");

            for (Element rowData : tableData) {

                Elements offerNo = rowData.select("td:eq(0)");
                Elements gClass = rowData.select("td:eq(1)");
                Elements subjCode = rowData.select("td:eq(2)");
                Elements desc = rowData.select("td:eq(3)");
                Elements unit = rowData.select("td:eq(4)");
                Elements days = rowData.select("td:eq(5)");
                Elements time = rowData.select("td:eq(6)");
                Elements room = rowData.select("td:eq(7)");
                Elements lec_lab = rowData.select("td:eq(8)");

                DashboardModel model = new DashboardModel
                        (
                                offerNo.text(),
                                gClass.text(),
                                subjCode.text(),
                                desc.text(),
                                unit.text(),
                                days.text(),
                                time.text(),
                                room.text(),
                                lec_lab.text()
                        );

                dashList.add(model);


            }
        }

        return dashList;
    }

}
