package com.jerson.hcdc_portal.listener;

public interface OnHttpResponseListener<T> {
    void onResponse(T response);
    void onFailure(Exception e);
}
