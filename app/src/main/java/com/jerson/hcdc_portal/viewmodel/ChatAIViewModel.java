package com.jerson.hcdc_portal.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.jerson.hcdc_portal.listener.OnClickListener;
import com.jerson.hcdc_portal.model.chat_ai.ChatBotAIModel;
import com.jerson.hcdc_portal.repo.ChatAIRepo;

import okhttp3.FormBody;

public class ChatAIViewModel extends ViewModel {
    private ChatAIRepo repo;
    private MutableLiveData<Throwable> err = new MutableLiveData<>();

    public ChatAIViewModel() {
        this.repo = new ChatAIRepo();
    }

    public LiveData<ChatBotAIModel> postChatBotAI(String url, FormBody data){
        return repo.postChatBotAI(url,data,err);
    }

    public void getParseValues(String url, OnClickListener<FormBody.Builder> res){
        repo.getParseValues(url,res,err);
    }

    public LiveData<String> postLlama(String msg){
        return repo.postLlama2(msg,err);
    }

    public MutableLiveData<Throwable> getErr() {
        return err;
    }
}
