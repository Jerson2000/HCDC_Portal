package com.jerson.hcdc_portal.repo;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.jerson.hcdc_portal.listener.OnHttpResponseListener;
import com.jerson.hcdc_portal.model.SubjectOfferedModel;
import com.jerson.hcdc_portal.model.chat_ai.ChatBotAIModel;
import com.jerson.hcdc_portal.network.HttpClient;

import org.json.JSONObject;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.RequestBody;

public class ChatAIRepo {
    private final String CHATBOTAI_ENDPOINT = "https://chatbotai.one/wp-admin/admin-ajax.php";
    private final String CHATGPTT_ENDPOINT = "https://chatgptt.me/wp-admin/admin-ajax.php";
    private final String LLAMA2_ENDPOINT = "https://www.llama2.ai/api";


    public LiveData<ChatBotAIModel> postChatBotAI(String msg, MutableLiveData<Throwable> err) {
        MutableLiveData<ChatBotAIModel> data = new MutableLiveData<>();
        FormBody formBody = new FormBody.Builder()
                .add("_wpnonce","af5faae5ae")
                .add("post_id","11")
                .add("url","https://chatbotai.one")
                .add("action","wpaicg_chat_shortcode_message")
                .add("message",msg)
                .add("bot_id","0")
                .build();
        /*FormBody formBody = new FormBody.Builder()
                .add("_wpnonce", "b47cbc4aee")
                .add("post_id", "22")
                .add("url", "https://chatgptt.me")
                .add("action", "wpaicg_chat_shortcode_message")
                .add("message", msg)
                .add("bot_id", "0")
                .build();*/

        HttpClient.getInstance().POST_JSON(CHATBOTAI_ENDPOINT, formBody, new OnHttpResponseListener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                System.out.println(response);
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

//    MAG SIGEG TIMEOUT GIATAY HAHAHAHA
    public LiveData<String> postLlama2(){
        MutableLiveData<String> data = new MutableLiveData<>();
        String msg = "android java abstraction enables to catch the error";
        String jsonBody="{\n" +
                "    \"prompt\": \""+msg+"\",\n" +
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
                System.out.println(response);
            }

            @Override
            public void onFailure(Exception e) {
                System.out.println(e);
            }

            @Override
            public void onResponseCode(int code) {
                System.out.println(code);
            }
        });

        return data;
    }


}
