package com.jerson.hcdc_portal.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.jerson.hcdc_portal.model.chat_ai.ChatBotAIModel;
import com.jerson.hcdc_portal.repo.ChatAIRepo;

public class ChatAIViewModel extends ViewModel {
    private ChatAIRepo repo;
    private MutableLiveData<Throwable> err = new MutableLiveData<>();

    public ChatAIViewModel() {
        this.repo = new ChatAIRepo();
    }

    public LiveData<ChatBotAIModel> postChatBotAI(String msg){
        return repo.postChatBotAI(msg,err);
    }

    public MutableLiveData<Throwable> getErr() {
        return err;
    }
}
