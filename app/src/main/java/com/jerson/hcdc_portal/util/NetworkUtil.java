package com.jerson.hcdc_portal.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;

import com.jerson.hcdc_portal.PortalApp;
import com.jerson.hcdc_portal.listener.DynamicListener;
import com.jerson.hcdc_portal.listener.OnClickListener;
import com.jerson.hcdc_portal.listener.OnHttpResponseListener;
import com.jerson.hcdc_portal.network.HttpClient;
import com.jerson.hcdc_portal.repo.DashboardRepo;

import org.jsoup.nodes.Document;

import okhttp3.FormBody;

public class NetworkUtil {

    public static boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) PortalApp.getAppContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
        return networkCapabilities != null && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }

    public static void checkSession(DynamicListener<Boolean> listener) {
        HttpClient.getInstance().GET_Redirection(PortalApp.baseUrl + PortalApp.gradesUrl, new OnHttpResponseListener<Document>() {
            @Override
            public void onResponse(Document response) {
                boolean isLoginPage = response.body().text().contains("CROSSIAN LOG-IN");
                listener.dynamicListener(isLoginPage);
                PortalApp.getPreferenceManager().putString(PortalApp.KEY_CSRF_TOKEN, response.select("meta[name=csrf-token]").attr("content"));
            }

            @Override
            public void onFailure(Exception e) {
                PortalApp.showToast(e.getMessage());
            }

            @Override
            public void onResponseCode(int code) {
                if (code > 200)
                    PortalApp.showToast("Response code: " + code);
            }
        });
    }

    public static void reLogin(OnClickListener<Boolean> logged) {
        FormBody formBody = new FormBody.Builder()
                .add("_token", PortalApp.getPreferenceManager().getString(PortalApp.KEY_CSRF_TOKEN))
                .add("email", PortalApp.getPreferenceManager().getString(PortalApp.KEY_EMAIL))
                .add("password", PortalApp.getPreferenceManager().getString(PortalApp.KEY_PASSWORD))
                .build();

        HttpClient.getInstance().POST(PortalApp.baseUrl + PortalApp.loginPostUrl, formBody, new OnHttpResponseListener<Document>() {
            @Override
            public void onResponse(Document response) {
                boolean wrongPass = response.body().text().contains("CROSSIAN LOG-IN");

                DashboardRepo.parseDashboard(response);

                if (wrongPass) {
                    logged.onItemClick(false);
                }
                if (!wrongPass) {
                    logged.onItemClick(true);
                    PortalApp.parseUser(response);
                }
            }

            @Override
            public void onFailure(Exception e) {
                PortalApp.showToast(e.getMessage());
            }

            @Override
            public void onResponseCode(int code) {
                if (code > 200)
                    PortalApp.showToast("Response code: " + code);
            }

        });
    }

}
