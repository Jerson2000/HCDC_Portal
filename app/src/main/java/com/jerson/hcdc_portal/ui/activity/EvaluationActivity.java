package com.jerson.hcdc_portal.ui.activity;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.jerson.hcdc_portal.PortalApp;
import com.jerson.hcdc_portal.R;
import com.jerson.hcdc_portal.databinding.ActivityEvaluationBinding;
import com.jerson.hcdc_portal.util.PreferenceManager;
import com.jerson.hcdc_portal.viewmodel.EvaluationViewModel;

public class EvaluationActivity extends AppCompatActivity {
    private ActivityEvaluationBinding binding;
    private String evaluationHtml;
    private EvaluationViewModel viewModel;
    private PreferenceManager preferenceManager;
    private WebView myWebView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEvaluationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(EvaluationViewModel.class);
        preferenceManager = new PreferenceManager(this);

        myWebView = binding.myWebView;

        getEvaluation();
        observeErr();






    }

    void getEvaluation(){
        viewModel.getEvaluation().observe(this,data->{
            if(data!=null){
                evaluationHtml = data;
                String htmlHeaders="<html>" +
                        "<body>" +
                        evaluationHtml +
                        "</body></html>";
                preferenceManager.putString(PortalApp.KEY_HTML_EVALUATION,data);
                myWebView.loadDataWithBaseURL(null, htmlHeaders, "text/html", "UTF-8", null);
            }
        });
    }

    void observeErr(){
        viewModel.getErr().observe(this,err->{
            Toast.makeText(this, err.getMessage(), Toast.LENGTH_SHORT).show();
            if(err!=null){
                myWebView.loadDataWithBaseURL(null,  preferenceManager.getString(PortalApp.KEY_HTML_EVALUATION), "text/html", "UTF-8", null);
            }
        });
    }


}