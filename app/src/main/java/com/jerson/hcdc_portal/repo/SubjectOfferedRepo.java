package com.jerson.hcdc_portal.repo;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.gson.Gson;
import com.jerson.hcdc_portal.PortalApp;
import com.jerson.hcdc_portal.listener.OnHttpResponseListener;
import com.jerson.hcdc_portal.model.RoomModel;
import com.jerson.hcdc_portal.model.SubjectOfferedModel;
import com.jerson.hcdc_portal.network.HttpClient;

import org.json.JSONObject;

import okhttp3.FormBody;

public class SubjectOfferedRepo {
    private static final String TAG = "SubjectOfferedRepo";

    public LiveData<SubjectOfferedModel> getSubjectOffered(int page,MutableLiveData<Throwable> err) {
        MutableLiveData<SubjectOfferedModel> data = new MutableLiveData<>();

        FormBody formBody = new FormBody.Builder()
                .add("_token", PortalApp.getPreferenceManager().getString(PortalApp.KEY_CSRF_TOKEN))
                .add("pn", String.valueOf(page))
                .build();
        HttpClient.getInstance().POST_JSON(PortalApp.baseUrl+PortalApp.subjectOffered, formBody, new OnHttpResponseListener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                SubjectOfferedModel model = new Gson().fromJson(response.toString(), SubjectOfferedModel.class);
                data.setValue(model);
            }

            @Override
            public void onFailure(Exception e) {
                err.setValue(e);
            }

            @Override
            public void onResponseCode(int code) {
                /*Log.e(TAG, "onResponseCode: "+code );*/
            }
        });
        return data;
    }


    public LiveData<SubjectOfferedModel> searchSubjectOffered(String query,MutableLiveData<Throwable> err) {
        MutableLiveData<SubjectOfferedModel> data = new MutableLiveData<>();

        FormBody formBody = new FormBody.Builder()
                .add("query", query)
                .add("_token", PortalApp.getPreferenceManager().getString(PortalApp.KEY_CSRF_TOKEN))
                .add("arnel", "arnel")
                .build();
        HttpClient.getInstance().POST_JSON(PortalApp.baseUrl+PortalApp.subjectOfferedSearch, formBody, new OnHttpResponseListener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                SubjectOfferedModel model = new Gson().fromJson(response.toString(), SubjectOfferedModel.class);
                data.setValue(model);
            }

            @Override
            public void onFailure(Exception e) {
                err.setValue(e);
            }

            @Override
            public void onResponseCode(int code) {
                /*Log.e(TAG, "onResponseCode: "+code );*/
            }
        });
        return data;
    }
}
