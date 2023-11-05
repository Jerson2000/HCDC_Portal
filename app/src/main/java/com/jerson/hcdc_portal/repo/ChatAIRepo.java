package com.jerson.hcdc_portal.repo;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.jerson.hcdc_portal.listener.OnClickListener;
import com.jerson.hcdc_portal.listener.OnHttpResponseListener;
import com.jerson.hcdc_portal.model.chat_ai.ChatBotAIModel;
import com.jerson.hcdc_portal.network.HttpClient;

import org.json.JSONObject;
import org.jsoup.nodes.Document;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.RequestBody;

public class ChatAIRepo {
    // these five endpoints are the same response, domain name ray nag lahi
    public static final String CHATBOTAI_ENDPOINT = "https://chatbotai.one/wp-admin/admin-ajax.php";
    public static final String CHATGPTT_ENDPOINT = "https://chatgptt.me/wp-admin/admin-ajax.php";
    public static final String CHATGBT_ENDPOINT = "https://chatgbt.one/wp-admin/admin-ajax.php";
    public static final String CHATGTP_ENDPOINT = "https://chatgtp.ca/wp-admin/admin-ajax.php";
    public static final String CHATAIGPT_ENDPOINT = "https://chataigpt.org/wp-admin/admin-ajax.php";
    // Llama2 endpoint is quite unstable 
    private final String LLAMA2_ENDPOINT = "https://www.llama2.ai/api";


    public LiveData<ChatBotAIModel> postChatBotAI(String url, FormBody formData, MutableLiveData<Throwable> err) {
        MutableLiveData<ChatBotAIModel> data = new MutableLiveData<>();
        HttpClient.getInstance().POST_JSON(url, formData, new OnHttpResponseListener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                /*System.out.println(response);*/
                ChatBotAIModel model = new Gson().fromJson(response.toString(), ChatBotAIModel.class);
                data.setValue(model);
            }

            @Override
            public void onFailure(Exception e) {
                err.postValue(e);
            }

            @Override
            public void onResponseCode(int code) {
                if (code > 200) {
                    err.postValue(new Exception("Something went wrong!"));
                }
            }
        });
        return data;
    }

    public void getParseValues(String url, OnClickListener<FormBody.Builder> res, MutableLiveData<Throwable> err) {
        HttpClient.getInstance().GET(getBaseUrl(url), new OnHttpResponseListener<Document>() {
            @Override
            public void onResponse(Document response) {
                res.onItemClick(new FormBody.Builder()
                        .add("_wpnonce", response.select("div.wpaicg-chat-shortcode").attr("data-nonce").trim())
                        .add("post_id", response.select("div.wpaicg-chat-shortcode").attr("data-post-id").trim())
                        .add("url", response.select("div.wpaicg-chat-shortcode").attr("data-url").trim())
                        .add("action", "wpaicg_chat_shortcode_message")
                        .add("bot_id", response.select("div.wpaicg-chat-shortcode").attr("data-bot-id").trim())
                );

                /*System.out.println(response.select("div.wpaicg-chat-shortcode"));*/
            }

            @Override
            public void onFailure(Exception e) {
                err.setValue(e);
            }

            @Override
            public void onResponseCode(int code) {
                if (code > 200) {
                    err.postValue(new Exception("Something went wrong!"));
                }
            }
        });
    }

    String getBaseUrl(String url) {
        int pathStartIndex = url.indexOf("/", 8);
        String base = url.substring(0, pathStartIndex);
        return base;
    }

    //    MAG SIGEG TIMEOUT GIATAY HAHAHAHA
    public LiveData<String> postLlama2(String msg, MutableLiveData<Throwable> err) {
        MutableLiveData<String> data = new MutableLiveData<>();
        String jsonBody = "{\n" +
                "    \"prompt\": \"" + msg + "\",\n" +
                "    \"version\": \"02e509c789964a7ea8736978a43525956ef40397be9033abf9fd2badfe68c9e3\",\n" +
                "    \"systemPrompt\": \"You are a helpful assistant.\",\n" +
                "    \"temperature\": 0.75,\n" +
                "    \"topP\": 0.9,\n" +
                "    \"maxTokens\": 800,\n" +
                "    \"image\": null,\n" +
                "    \"audio\": null\n" +
                "}";
        HttpClient.getInstance().POST_STRING(LLAMA2_ENDPOINT, RequestBody.create(jsonBody, MediaType.get("application/json")), new OnHttpResponseListener<String>() {
            @Override
            public void onResponse(String response) {
                data.setValue(response);
            }

            @Override
            public void onFailure(Exception e) {
                err.setValue(e);
            }

            @Override
            public void onResponseCode(int code) {
                if (code > 200) {
                    err.setValue(new Exception("Something went wrong!"));
                }
            }
        });

        return data;
    }


}
