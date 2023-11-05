package com.jerson.hcdc_portal.model.chat_ai;

public class ChatModel {
    public static final int TYPE_USER = 0;
    public static final int TYPE_AI = 1;

    private int type;
    private String message;

    public ChatModel() {
    }

    public ChatModel(int type, String message) {
        this.type = type;
        this.message = message;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
