package com.jerson.hcdc_portal.ui.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;

import androidx.appcompat.app.ActionBar;

import com.jerson.hcdc_portal.PortalApp;
import com.jerson.hcdc_portal.databinding.ActivityTestBinding;
import com.jerson.hcdc_portal.listener.DynamicListener;
import com.jerson.hcdc_portal.listener.OnHttpResponseListener;
import com.jerson.hcdc_portal.network.HttpClient;
import com.jerson.hcdc_portal.util.BaseActivity;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import okhttp3.FormBody;

public class TestActivity extends BaseActivity<ActivityTestBinding> {
    private static final String TAG = "TestActivity";
    private Executor executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        executor = Executors.newSingleThreadExecutor();

        setSupportActionBar(getBinding().header.toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            getBinding().header.collapsingToolbar.setTitle("Testing");
            getBinding().header.collapsingToolbar.setSubtitle("Jerson Ray B. Desierto");

            getBinding().testBtn.setOnClickListener(v -> {
               /* executor.execute(() -> {
                    try {
                        String json = Jsoup.connect("https://jsonplaceholder.typicode.com/todos/1").ignoreContentType(true).execute().body();
                        System.out.println(json);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });*/

               /* getToken(new DynamicListener<String>() {
                    @Override
                    public void dynamicListener(String object) {
                        if(!object.isEmpty()){
                            FormBody formBody = new FormBody.Builder()
                                    .add("_token", object)
                                    .add("pn", "2")
                                    .build();

                            HttpClient.getInstance().POST_JSON("http://studentportal.hcdc.edu.ph/subject/p", formBody, new OnHttpResponseListener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    Log.e(TAG, "onResponse: "+response);
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    Log.e(TAG, "onFailure: ",e );
                                }

                                @Override
                                public void onResponseCode(int code) {
                                    Log.e(TAG, "onResponseCode: "+code );
                                }
                            });
                        }
                    }
                });*/
                getToken();

            });

        }
    }

    void getToken() {
        HttpClient.getInstance().GET(PortalApp.baseUrl + PortalApp.loginUrl, new OnHttpResponseListener<Document>() {

            @Override
            public void onResponse(Document response) {
                Log.e(TAG, "onResponse: "+response.select("meta[name=csrf-token]").attr("content"));
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "onFailure: ", e);
            }

            @Override
            public void onResponseCode(int code) {
                Log.e(TAG, "onResponseCode: " + code);
            }


        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected ActivityTestBinding createBinding(LayoutInflater layoutInflater) {
        return ActivityTestBinding.inflate(layoutInflater);
    }
}