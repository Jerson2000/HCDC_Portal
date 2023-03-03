package com.jerson.hcdc_portal.viewmodel;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.jerson.hcdc_portal.database.DatabasePortal;
import com.jerson.hcdc_portal.listener.OnHttpResponseListener;
import com.jerson.hcdc_portal.model.DashboardModel;
import com.jerson.hcdc_portal.network.Clients;
import com.jerson.hcdc_portal.network.HttpClient;
import com.jerson.hcdc_portal.util.AppConstants;


import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import io.reactivex.Completable;
import io.reactivex.Flowable;

public class DashboardViewModel extends AndroidViewModel {
    MutableLiveData<String> response = new MutableLiveData<>();

    DatabasePortal databasePortal;

    public DashboardViewModel(@NonNull Application application) {
        super(application);
        databasePortal =  DatabasePortal.getDatabase(application);

    }
    public MutableLiveData<List<DashboardModel>> getData(Context context){
        MutableLiveData<List<DashboardModel>> data = new MutableLiveData<>();
        HttpClient.getInstance(context).GET(AppConstants.baseUrl, new OnHttpResponseListener<Document>() {
            @Override
            public void onResponse(Document response) {

                List<DashboardModel> dashboardModelList = new ArrayList<>();

                Elements dashboardTable = response.select("div.col-sm-9 tbody");
                int i = 0;
                for (Element list : dashboardTable) {
                    Elements tableData = list.select("tr");

                    for (Element rowData : tableData) {
                        i++;

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
                                (i,
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

                        dashboardModelList.add(model);


                    }
                }

                data.postValue(dashboardModelList);
            }

            @Override
            public void onFailure(Exception e) {
                response.postValue(e.getMessage());
                e.printStackTrace();
            }
        });
        return data;
    }


    public MutableLiveData<String> getDashboardResponse(){
        return response;
    }


    public Completable insertDashboard(List<DashboardModel> dashboardModel){
        return databasePortal.databaseDao().insertDashboard(dashboardModel);
    }

    public Flowable<List<DashboardModel>> loadDashboard(){
        return databasePortal.databaseDao().getDashboard();
    }

}
