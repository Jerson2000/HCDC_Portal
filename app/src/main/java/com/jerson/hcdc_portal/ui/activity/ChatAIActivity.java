package com.jerson.hcdc_portal.ui.activity;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.jerson.hcdc_portal.PortalApp;
import com.jerson.hcdc_portal.R;
import com.jerson.hcdc_portal.databinding.ActivityChatAiactivityBinding;
import com.jerson.hcdc_portal.databinding.AiChooserLayoutBinding;
import com.jerson.hcdc_portal.listener.OnClickListener;
import com.jerson.hcdc_portal.model.chat_ai.ChatBotAIModel;
import com.jerson.hcdc_portal.model.chat_ai.ChatModel;
import com.jerson.hcdc_portal.network.HttpClient;
import com.jerson.hcdc_portal.repo.ChatAIRepo;
import com.jerson.hcdc_portal.ui.adapter.ChatAIAdapter;
import com.jerson.hcdc_portal.util.BaseActivity;
import com.jerson.hcdc_portal.util.Dialog;
import com.jerson.hcdc_portal.viewmodel.ChatAIViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.FormBody;

public class ChatAIActivity extends BaseActivity<ActivityChatAiactivityBinding> {
    private List<ChatModel> chatList = new ArrayList<>();
    private ChatAIAdapter adapter;
    private ChatAIViewModel aiViewModel;
    private ChatBotAIModel chatBotAIModel;
    private AlertDialog dialog;
    private AiChooserLayoutBinding aiChooserLayoutBinding;
    private String AI_URL;
    private FormBody.Builder formBuilder;
    private FormBody formBody;
    private boolean isLlama;
    private boolean isShow;

    @Override
    protected void onStart() {
        super.onStart();
        if (!getBindingNull()) {
            if(!isShow){
                setAIDialog();
                isShow = true;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!getBindingNull()) init();

    }

    @SuppressLint("ClickableViewAccessibility")
    void init() {
        setSupportActionBar(getBinding().toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        aiViewModel = new ViewModelProvider(this).get(ChatAIViewModel.class);
        aiChooserLayoutBinding = AiChooserLayoutBinding.inflate(LayoutInflater.from(this));

        adapter = new ChatAIAdapter(chatList);
        getBinding().recyclerView.setLayoutManager(new LinearLayoutManager(this));
        getBinding().recyclerView.setAdapter(adapter);

        getBinding().userBtn.setOnClickListener(v -> {
            if (!getBinding().msgET.getText().toString().equals("")) {
                chatList.add(new ChatModel(ChatModel.TYPE_USER, getBinding().msgET.getText().toString()));
                chatList.add(new ChatModel(ChatModel.TYPE_AI, "Generating.."));
                adapter.notifyDataSetChanged();
                if (isLlama)
                    postLlama();
                else
                    aiChatBot();

            }
        });

        getBinding().msgET.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                if (chatList.size() != 0) {
                    if (!isFinishing())
                        getBinding().recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
                }

            }
            return false;
        });

        getERR();
    }

    void aiChatBot() {
        toggle(true);
        formBody = formBuilder
                .add("message", getBinding().msgET.getText().toString()).build();
        aiViewModel.postChatBotAI(AI_URL, formBody).observe(this, data -> {
            if (data != null) {
                chatList.remove(chatList.size() - 1);
                if (!data.getData().getStatus().toLowerCase(Locale.ROOT).contains("err")) {
                    chatBotAIModel = data;
                    chatList.add(new ChatModel(ChatModel.TYPE_AI, chatBotAIModel.getData().getData()));
                } else {
                    chatList.add(new ChatModel(ChatModel.TYPE_AI, data.getData().getMsg()));
                }

                if (!getBindingNull() && !isFinishing()) {
                    getBinding().recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
                    adapter.notifyDataSetChanged();
                    getBinding().msgET.setText(null);
                    toggle(false);
                }
            }
        });
    }

    void postLlama(){
        toggle(true);
        aiViewModel.postLlama(getBinding().msgET.getText().toString().trim()).observe(this,data->{
            if(!data.equals("")){
                chatList.remove(chatList.size() - 1);
                chatList.add(new ChatModel(ChatModel.TYPE_AI, data));
                if (!getBindingNull() && !isFinishing()) {
                    getBinding().recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
                    adapter.notifyDataSetChanged();
                    getBinding().msgET.setText(null);
                    toggle(false);
                }
            }
        });
    }

    void getERR() {
        aiViewModel.getErr().observe(this, e -> {
            if (chatList.size() != 0) chatList.remove(chatList.size() - 1);
            chatList.add(new ChatModel(ChatModel.TYPE_AI, e.getMessage()));
        });
    }

    void toggle(boolean loading) {
        if (loading) {
            getBinding().msgET.setEnabled(false);
            getBinding().userBtn.setEnabled(false);
            getBinding().userBtn.setText("Please wait");
        } else {
            getBinding().msgET.setEnabled(true);
            getBinding().userBtn.setEnabled(true);
            getBinding().userBtn.setText("Send");
        }
    }

    void setAIDialog() {
        ViewGroup parentView = (ViewGroup) aiChooserLayoutBinding.getRoot().getParent();
        if (parentView != null) {
            parentView.removeView(aiChooserLayoutBinding.getRoot());
        }
        dialog = Dialog.CustomDialog("Choose your AI Assistant", this, aiChooserLayoutBinding.getRoot())
                .setMessage("It appears to be the same, but they are actually different sites. Just pick one, and it will be ready to go!")
                .show();

        Glide.with(this)
                .load("https://chatbotai.one/wp-content/uploads/2023/06/cropped-Untitled_design__8_-removebg-preview-32x32.png")
                .into(aiChooserLayoutBinding.ChatBotIV);
        Glide.with(this)
                .load("https://chatgbt.one/wp-content/uploads/2023/05/cropped-cropped-chatgbt-32x32.png")
                .into(aiChooserLayoutBinding.ChatGBTIV);
        Glide.with(this)
                .load("https://chatgbt.one/wp-content/uploads/2023/05/cropped-cropped-chatgbt-32x32.png")
                .into(aiChooserLayoutBinding.ChatGTPIV);
        Glide.with(this)
                .load("https://chatgbt.one/wp-content/uploads/2023/05/cropped-cropped-chatgbt-32x32.png")
                .into(aiChooserLayoutBinding.ChatGPTTIV);
        Glide.with(this)
                .load("https://chatgbt.one/wp-content/uploads/2023/05/cropped-cropped-chatgbt-32x32.png")
                .into(aiChooserLayoutBinding.ChatGPTIV);
        Glide.with(this)
                .load("https://llama-2.ai/wp-content/uploads/2023/08/Llama-2-icon-150x150.png")
                .into(aiChooserLayoutBinding.llamaIV);

        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);


        aiChooserLayoutBinding.chatBotBtn.setOnClickListener(v -> {
            HttpClient.getInstance().cancelRequest();
            AI_URL = ChatAIRepo.CHATBOTAI_ENDPOINT;
            isLlama =  false;
            getBinding().toolbar.setTitle("ChatBotAI");
            getParseValues(AI_URL);
        });
        aiChooserLayoutBinding.chatGBTBtn.setOnClickListener(v -> {
            AI_URL = ChatAIRepo.CHATGBT_ENDPOINT;
            isLlama =  false;
            getBinding().toolbar.setTitle("ChatGBT");
            getParseValues(AI_URL);
        });
        aiChooserLayoutBinding.chatGTPBtn.setOnClickListener(v -> {
            HttpClient.getInstance().cancelRequest();
            AI_URL = ChatAIRepo.CHATGTP_ENDPOINT;
            isLlama =  false;
            getBinding().toolbar.setTitle("ChatGTP");
            getParseValues(AI_URL);
        });
        aiChooserLayoutBinding.chatGPTTBtn.setOnClickListener(v -> {
            HttpClient.getInstance().cancelRequest();
            AI_URL = ChatAIRepo.CHATGPTT_ENDPOINT;
            isLlama =  false;
            getBinding().toolbar.setTitle("ChatGPTT");
            getParseValues(AI_URL);
        });
        aiChooserLayoutBinding.chatGPTBtn.setOnClickListener(v -> {
            HttpClient.getInstance().cancelRequest();
            AI_URL = ChatAIRepo.CHATAIGPT_ENDPOINT;
            isLlama =  false;
            getBinding().toolbar.setTitle("ChatAIGPT");
            getParseValues(AI_URL);
        });
        aiChooserLayoutBinding.llamaBtn.setOnClickListener(v->{
            HttpClient.getInstance().cancelRequest();
            getBinding().toolbar.setTitle("Llama2");
            isLlama =  true;
            dialog.dismiss();
        });

    }

    void getParseValues(String AI_URL) {
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
        ProgressBar progressBar = new ProgressBar(this);
        progressBar.setIndeterminate(true);
        dialog = Dialog.CustomDialog(null, this, progressBar).show();
        aiViewModel.getParseValues(AI_URL, object -> {
            if (object != null) {
                formBuilder = object;
                dialog.dismiss();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.choose_ai_menu, menu);
        MenuItem choose = menu.findItem(R.id.ai_choose);
        choose.setOnMenuItemClickListener(menuItem -> {
            setAIDialog();
            return true;
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected ActivityChatAiactivityBinding createBinding(LayoutInflater layoutInflater) {
        return ActivityChatAiactivityBinding.inflate(layoutInflater);
    }
}