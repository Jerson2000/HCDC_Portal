package com.jerson.hcdc_portal.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Handler;
import android.os.Looper;

import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;
import com.jerson.hcdc_portal.PortalApp;
import com.jerson.hcdc_portal.listener.DynamicListener;
import com.jerson.hcdc_portal.listener.OnHttpResponseListener;
import com.jerson.hcdc_portal.util.NetworkUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class HttpClient {
    private static HttpClient instance;
    private OkHttpClient client;
    private Call call;
    private Handler handler;
    private Executor executor;
    private CacheControl cacheControl;

    private HttpClient() {
        File cacheDirectory = new File(PortalApp.getAppContext().getCacheDir(), "http-cache");
        int cacheSize = 100 * 1024 * 1024; // 10 MB
        Cache cache = new Cache(cacheDirectory, cacheSize);
        client = new OkHttpClient.Builder()
                .addInterceptor(new TimingInterceptor())
                .cookieJar(new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(PortalApp.getAppContext())))
                .cache(cache)
                .build();
        cacheControl = new CacheControl.Builder()
                .maxAge(5, TimeUnit.HOURS)
                .maxStale(1, TimeUnit.DAYS)
                .build();
        handler = new Handler(Looper.getMainLooper());
        executor = Executors.newSingleThreadExecutor();
    }


    public static synchronized HttpClient getInstance() {
        if (instance == null) {
            instance = new HttpClient();
        }
        return instance;
    }

    public OkHttpClient getClient() {
        return client;
    }

    public void GET(String url, final OnHttpResponseListener<Document> listener) {
        if (NetworkUtil.isConnected()) {
            Request request = new Request.Builder()
                    .url(url)
                    .cacheControl(cacheControl)
                    .build();
            call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        try (ResponseBody body = response.body()) {
                            String html = body.string();
                            handler.post(() -> listener.onResponseCode(response.code()));
                            handler.post(() -> listener.onResponse(Jsoup.parse(html)));
                        }
                    } else {
                        handler.post(() -> listener.onResponseCode(response.code()));
                    }
                }

                @Override
                public void onFailure(Call call, IOException e) {
                    handler.post(() -> listener.onFailure(e));
                }
            });
        } else {
            listener.onFailure(new IOException("No internet connection"));
        }
    }

    public void cancelRequest() {
        if (call != null) {
            call.cancel();
        }
    }


    /*public void POST(String url, FormBody formBody, OnHttpResponseListener<Document> listener) {
        if (PortalApp.isConnected()) {
            executor.execute(() -> {
                try {
                    Request request = new Request.Builder()
                            .url(url)
                            .post(formBody)
                            .build();

                    Response response = client.newCall(request).execute();

                    System.out.println(response.code() + " - " + response.message());

                    while (response.isRedirect()) {
                        if (response.code() == 302 || response.code() == 307) {
                            request = request.newBuilder()
                                    *//*.url(response.header("Location"))*//*
                                    .url(PortalApp.baseUrl)
                                    .post(formBody)
                                    .build();

                            response = client.newCall(request).execute();
                        }
                        // Handle other redirects
                        else {
                            request = request.newBuilder()
                                    *//*.url(response.header("Location"))*//*
                                    .url(PortalApp.baseUrl)
                                    .build();

                            response = client.newCall(request).execute();
                        }
                    }

                    Response finalResponse = response;
                    // Handle the final response
                    if (response.isSuccessful()) {
                        // Success
                        handler.post(() -> listener.onResponseCode(finalResponse.code()));
                        ResponseBody body = response.body();
                        if (body != null) {
                            String responseData = body.string();
                            Document document = Jsoup.parse(responseData);
                            handler.post(() -> listener.onResponse(document));
                        }
                    } else {
                        // Handle error
                        handler.post(() -> listener.onResponseCode(finalResponse.code()));
                    }
                } catch (IOException e) {
                    handler.post(() -> listener.onFailure(e));
                }
            });
        } else {
            listener.onFailure(new IOException("No internet connection"));
        }

    }*/



    public void POST(String url, FormBody formBody, OnHttpResponseListener<Document> listener) {
        if (NetworkUtil.isConnected()) {
            executor.execute(() -> {
                try {
                    Request request = new Request.Builder()
                            .url(url)
                            .post(formBody)
                            .build();

                    call = client.newCall(request); // Assign the Call object

                    Response response = call.execute();

                    while (response.isRedirect()) {
                        if (response.code() == 302 || response.code() == 307) {
                            request = request.newBuilder()
                                    /*.url(response.header("Location"))*/
                                    .url(PortalApp.baseUrl)
                                    .post(formBody)
                                    .build();

                            call = client.newCall(request); // Update the Call object

                            response = call.execute();
                        }
                        // Handle other redirects
                        else {
                            request = request.newBuilder()
                                    /*.url(response.header("Location"))*/
                                    .url(PortalApp.baseUrl)
                                    .build();

                            call = client.newCall(request); // Update the Call object

                            response = call.execute();
                        }
                    }

                    Response finalResponse = response;
                    // Handle the final response
                    if (response.isSuccessful()) {
                        // Success
                        handler.post(() -> listener.onResponseCode(finalResponse.code()));
                        ResponseBody body = response.body();
                        if (body != null) {
                            String responseData = body.string();
                            Document document = Jsoup.parse(responseData);
                            handler.post(() -> listener.onResponse(document));
                        }
                    } else {
                        // Handle error
                        handler.post(() -> listener.onResponseCode(finalResponse.code()));
                    }
                } catch (IOException e) {
                    handler.post(() -> listener.onFailure(e));
                } finally {
                    if (call != null) {
                        call.cancel(); // Cancel the Call object if it exists
                    }
                }
            });
        } else {
            listener.onFailure(new IOException("No internet connection"));
        }
    }


    public void POST_JSON(String url, FormBody formBody, OnHttpResponseListener<JSONObject> listener) {
        if (NetworkUtil.isConnected()) {
            executor.execute(() -> {
                try {
                    Request request = new Request.Builder()
                            .url(url)
                            .post(formBody)
                            .build();

                    call = client.newCall(request); // Assign the Call object

                    Response response = call.execute();

                    while (response.isRedirect()) {
                        if (response.code() == 302 || response.code() == 307) {
                            request = request.newBuilder()
                                    .url(response.header("Location"))
                                    .url(PortalApp.baseUrl)
                                    .post(formBody)
                                    .build();

                            call = client.newCall(request); // Update the Call object

                            response = call.execute();
                        }
                        // Handle other redirects
                        else {
                            request = request.newBuilder()
                                    .url(response.header("Location"))
                                    .url(PortalApp.baseUrl)
                                    .build();

                            call = client.newCall(request); // Update the Call object

                            response = call.execute();
                        }
                    }

                    Response finalResponse = response;
                    // Handle the final response
                    if (response.isSuccessful()) {
                        // Success
                        handler.post(() -> listener.onResponseCode(finalResponse.code()));
                        ResponseBody body = response.body();
                        if (body != null) {
                            String responseData = body.string();
                            JSONObject jsonResponse = new JSONObject("{data:"+responseData+"}"); // Parse JSON data
                            handler.post(() -> listener.onResponse(jsonResponse));
                        }
                    } else {
                        // Handle error
                        handler.post(() -> listener.onResponseCode(finalResponse.code()));
                    }
                } catch (IOException | JSONException e) {
                    handler.post(() -> listener.onFailure(e));
                } finally {
                    if (call != null) {
                        call.cancel(); // Cancel the Call object if it exists
                    }
                }
            });
        } else {
            listener.onFailure(new IOException("No internet connection"));
        }
    }


    public void POST_STRING(String url, RequestBody formBody, OnHttpResponseListener<String> listener) {
        if (NetworkUtil.isConnected()) {
            executor.execute(() -> {
                try {
                    Request request = new Request.Builder()
                            .url(url)
                            .post(formBody)
                            .build();

                    call = client.newCall(request); // Assign the Call object

                    Response response = call.execute();

                    while (response.isRedirect()) {
                        if (response.code() == 302 || response.code() == 307) {
                            request = request.newBuilder()
                                    .url(response.header("Location"))
                                    .url(PortalApp.baseUrl)
                                    .post(formBody)
                                    .build();

                            call = client.newCall(request); // Update the Call object

                            response = call.execute();
                        }
                        // Handle other redirects
                        else {
                            request = request.newBuilder()
                                    .url(response.header("Location"))
                                    .url(PortalApp.baseUrl)
                                    .build();

                            call = client.newCall(request); // Update the Call object

                            response = call.execute();
                        }
                    }

                    Response finalResponse = response;
                    // Handle the final response
                    if (response.isSuccessful()) {
                        // Success
                        handler.post(() -> listener.onResponseCode(finalResponse.code()));
                        ResponseBody body = response.body();
                        if (body != null) {
                            String responseData = body.string();
                            handler.post(() -> listener.onResponse(responseData));
                        }
                    } else {
                        // Handle error
                        handler.post(() -> listener.onResponseCode(finalResponse.code()));
                    }
                } catch (IOException e) {
                    handler.post(() -> listener.onFailure(e));
                } finally {
                    if (call != null) {
                        call.cancel(); // Cancel the Call object if it exists
                    }
                }
            });
        } else {
            listener.onFailure(new IOException("No internet connection"));
        }
    }

    public void reLogin(String url, FormBody formBody, OnHttpResponseListener<Document> listener) {
        if (NetworkUtil.isConnected()) {
            executor.execute(() -> {
                try {
                    Request request = new Request.Builder()
                            .url(url)
                            .post(formBody)
                            .build();

                    call = client.newCall(request); // Assign the Call object

                    Response response = call.execute();

                    while (response.isRedirect()) {
                        if (response.code() == 302 || response.code() == 307) {
                            request = request.newBuilder()
                                    .url(PortalApp.baseUrl+PortalApp.gradesUrl)
                                    .post(formBody)
                                    .build();

                            call = client.newCall(request); // Update the Call object

                            response = call.execute();
                        }
                        // Handle other redirects
                        else {
                            request = request.newBuilder()
                                    .url(PortalApp.baseUrl+PortalApp.gradesUrl)
                                    .build();

                            call = client.newCall(request); // Update the Call object

                            response = call.execute();
                        }
                    }

                    Response finalResponse = response;
                    // Handle the final response
                    if (response.isSuccessful()) {
                        // Success
                        handler.post(() -> listener.onResponseCode(finalResponse.code()));
                        ResponseBody body = response.body();
                        if (body != null) {
                            String responseData = body.string();
                            Document document = Jsoup.parse(responseData);
                            handler.post(() -> listener.onResponse(document));
                        }
                    } else {
                        // Handle error
                        handler.post(() -> listener.onResponseCode(finalResponse.code()));
                    }
                } catch (IOException e) {
                    handler.post(() -> listener.onFailure(e));
                } finally {
                    if (call != null) {
                        call.cancel(); // Cancel the Call object if it exists
                    }
                }
            });
        } else {
            listener.onFailure(new IOException("No internet connection"));
        }
    }


    /*public void GET_Redirection(String url, final OnHttpResponseListener<Document> listener) {
        executor.execute(() -> {
            try {
                Request request = new Request.Builder()
                        .url(url)
                        .cacheControl(cacheControl)
                        .build();

                Response response = client.newCall(request).execute();

                System.out.println(response.code() + " - " + response.message());

                while (response.isRedirect()) {
                    if (response.code() == 302) {
                        request = request.newBuilder()
                                .url(response.header("Location"))
                                .build();

                        response = client.newCall(request).execute();
                    }
                }

                Response finalResponse = response;
                // Handle the final response
                if (response.isSuccessful()) {
                    ResponseBody body = response.body();
                    if (body != null) {
                        String responseData = body.string();
                        Document document = Jsoup.parse(responseData);
                        handler.post(() -> listener.onResponse(document));
                        handler.post(() -> listener.onResponseCode(finalResponse.code()));
                    }
                } else {
                    handler.post(() -> listener.onResponseCode(finalResponse.code()));
                }
            } catch (IOException e) {
                handler.post(() -> listener.onFailure(e));
            }
        });

    }*/




    public void GET_Redirection(String url, final OnHttpResponseListener<Document> listener) {
        if (NetworkUtil.isConnected()) {
            final Request[] request = {new Request.Builder()
                    .url(url)
                    .cacheControl(cacheControl)
                    .build()};
            call = client.newCall(request[0]);
            call.enqueue(new Callback() {
                @Override
                public void onResponse(Call call, Response response) {
                    try {

                        while (response.isRedirect()) {
                            if (response.code() == 302) {
                                request[0] = request[0].newBuilder()
                                        .url(response.header("Location"))
                                        .build();

                                response = client.newCall(request[0]).execute();
                            }
                        }

                        Response finalResponse = response;
                        // Handle the final response
                        if (response.isSuccessful()) {
                            try (ResponseBody body = response.body()) {
                                String responseData = body.string();
                                Document document = Jsoup.parse(responseData);
                                handler.post(() -> listener.onResponse(document));
                                handler.post(() -> listener.onResponseCode(finalResponse.code()));
                            }
                        } else {
                            handler.post(() -> listener.onResponseCode(finalResponse.code()));
                        }
                    } catch (IOException e) {
                        handler.post(() -> listener.onFailure(e));
                    }
                }

                @Override
                public void onFailure(Call call, IOException e) {
                    handler.post(() -> listener.onFailure(e));
                }
            });
        } else {
            listener.onFailure(new IOException("No internet connection"));
        }
    }

    /*public void GET_ResponseBody(String url, OnHttpResponseListener<ResponseBody> listener) {
        if (PortalApp.isConnected()) {
            Request request = new Request.Builder()
                    .url(url)
                    .cacheControl(cacheControl)
                    .build();
            executor.execute(() -> {
                try {
                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        // Handle successful response
                        ResponseBody body = response.body();
                        handler.post(() -> listener.onResponseCode(response.code()));
                        handler.post(() -> listener.onResponse(body));

                    } else {
                        // Handle unsuccessful response
                        handler.post(() -> listener.onResponseCode(response.code()));
                    }
                } catch (IOException e) {
                    // Handle network or IO errors
                    handler.post(() -> listener.onFailure(e));
                }
            });
        } else {
            listener.onFailure(new IOException("No internet connection"));
        }
    }*/


    public void GET_ResponseBody(String url, OnHttpResponseListener<ResponseBody> listener) {
        if (NetworkUtil.isConnected()) {
            Request request = new Request.Builder()
                    .url(url)
                    .cacheControl(cacheControl)
                    .build();
            call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onResponse(Call call, Response response){
                    if (response.isSuccessful()) {
                        ResponseBody body = response.body();
                        handler.post(() -> listener.onResponseCode(response.code()));
                        handler.post(() -> listener.onResponse(body));
                    } else {
                        handler.post(() -> listener.onResponseCode(response.code()));
                    }
                }

                @Override
                public void onFailure(Call call, IOException e) {
                    handler.post(() -> listener.onFailure(e));
                }
            });
        } else {
            listener.onFailure(new IOException("No internet connection"));
        }
    }

}
