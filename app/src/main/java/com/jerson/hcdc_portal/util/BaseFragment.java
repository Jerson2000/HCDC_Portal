package com.jerson.hcdc_portal.util;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.viewbinding.ViewBinding;

import com.jerson.hcdc_portal.network.HttpClient;

public abstract class BaseFragment<T extends ViewBinding> extends Fragment implements DefaultLifecycleObserver {
    private T binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = onCreateViewBinding(inflater, container);
        return binding.getRoot();
    }

    protected abstract T onCreateViewBinding(LayoutInflater layoutInflater, ViewGroup container);

    public T getBinding() {
        return binding;
    }

    public boolean getBindingNull(){
        return binding == null;
    }
    public boolean isGetViewLifecycleNull(){
        return getView()==null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (binding != null) {
            ViewGroup viewGroup = (ViewGroup) binding.getRoot();
            viewGroup.removeAllViews();
            binding = null;
        }
        HttpClient.getInstance().cancelRequest();
    }
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Register your class as observer
        if (getActivity() != null) {
            getActivity().getLifecycle().addObserver(this);
        }
    }

    @Override
    public void onCreate(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onCreate(owner);
        // Remove the observer
        if (getActivity() != null) {
            getActivity().getLifecycle().removeObserver(this);
        }
    }

}