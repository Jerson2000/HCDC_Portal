package com.jerson.hcdc_portal.util;

import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewbinding.ViewBinding;

import com.jerson.hcdc_portal.network.HttpClient;

public abstract class BaseActivity<T extends ViewBinding> extends AppCompatActivity {
    private T binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = createBinding(getLayoutInflater());
        setContentView(binding.getRoot());

    }

    protected T getBinding() {
        return binding;
    }

    protected boolean getBindingNull(){
        return binding==null;
    }

    protected abstract T createBinding(LayoutInflater layoutInflater);


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (binding != null) binding = null;
        HttpClient.getInstance().cancelRequest();
    }
}
