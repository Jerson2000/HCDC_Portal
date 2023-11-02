package com.jerson.hcdc_portal.model.chat_ai;


import com.google.gson.annotations.SerializedName;

public class ChatBotAIModel {
    @SerializedName("data")
    private ChatBotAI data;

    public ChatBotAI getData() {
        return data;
    }

    public void setData(ChatBotAI data) {
        this.data = data;
    }

    static public class ChatBotAI{
        @SerializedName("status")
        private String status;
        @SerializedName("msg")
        private String msg;
        @SerializedName("data")
        private String data;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }
    }
}
