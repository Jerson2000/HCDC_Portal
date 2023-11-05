package com.jerson.hcdc_portal.ui.adapter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jerson.hcdc_portal.PortalApp;
import com.jerson.hcdc_portal.databinding.ItemContainerChatAiBinding;
import com.jerson.hcdc_portal.databinding.ItemContainerChatUserBinding;
import com.jerson.hcdc_portal.model.chat_ai.ChatModel;

import java.util.List;

public class ChatAIAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<ChatModel> list;

    public ChatAIAdapter(List<ChatModel> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType){
            case 0:
                return new SendMessageViewHolder(ItemContainerChatUserBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));
            case 1:
                return new ReceivedMessageViewHolder(ItemContainerChatAiBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(getItemViewType(position) == 0){
            ((SendMessageViewHolder) holder).setData(list.get(position));
        }else{
            ((ReceivedMessageViewHolder) holder).setData(list.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public int getItemViewType(int position) {
        ChatModel chatMessage = list.get(position);
        return chatMessage.getType();
    }

    static class SendMessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemContainerChatUserBinding binding;

        SendMessageViewHolder(ItemContainerChatUserBinding itemSentMessageBinding) {
            super(itemSentMessageBinding.getRoot());
            binding = itemSentMessageBinding;
        }

        void setData(ChatModel msg) {
            binding.textMessage.setText(msg.getMessage());
        }
    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemContainerChatAiBinding binding;

        ReceivedMessageViewHolder(ItemContainerChatAiBinding itemContainerChatAiBinding) {
            super(itemContainerChatAiBinding.getRoot());
            binding = itemContainerChatAiBinding;
        }

        void setData(ChatModel msg) {
            binding.textMessage.setText(msg.getMessage());
            binding.textMessage.setOnLongClickListener(v->{
                Clipboard(msg.getMessage(),binding.getRoot().getContext());
                return true;
            });
        }
    }

    static void Clipboard(String text,Context context){
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("text",text);
        clipboard.setPrimaryClip(clip);
        PortalApp.showToast("Clipboard copied");
    }
    

}
