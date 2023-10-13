package com.jerson.hcdc_portal.ui.activity;

import static androidx.webkit.WebSettingsCompat.FORCE_DARK_OFF;
import static androidx.webkit.WebSettingsCompat.FORCE_DARK_ON;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;
import androidx.webkit.WebSettingsCompat;
import androidx.webkit.WebViewFeature;

import com.jerson.hcdc_portal.PortalApp;
import com.jerson.hcdc_portal.databinding.ActivityEvaluationBinding;
import com.jerson.hcdc_portal.util.BaseActivity;
import com.jerson.hcdc_portal.util.PreferenceManager;
import com.jerson.hcdc_portal.viewmodel.EvaluationViewModel;

public class EvaluationActivity extends BaseActivity<ActivityEvaluationBinding> {
    private static final String TAG = "EvaluationActivity";
    private ActivityEvaluationBinding binding;
    private String evaluationHtml;
    private EvaluationViewModel viewModel;
    private PreferenceManager preferenceManager;
    private WebView myWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = getBinding();

        viewModel = new ViewModelProvider(this).get(EvaluationViewModel.class);
        preferenceManager = new PreferenceManager(this);

        if(!getBindingNull()) init();




    }

    void init(){
        myWebView = binding.myWebView;

        if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
            switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
                case Configuration.UI_MODE_NIGHT_YES:
                    WebSettingsCompat.setForceDark(myWebView.getSettings(), FORCE_DARK_ON);
                    break;
                case Configuration.UI_MODE_NIGHT_NO:
                case Configuration.UI_MODE_NIGHT_UNDEFINED:
                    WebSettingsCompat.setForceDark(myWebView.getSettings(), FORCE_DARK_OFF);
                    break;
            }
        }

        getEvaluation();
        observeErr();
        binding.btnBack.setOnClickListener(v->onBackPressed());
    }

    void getEvaluation() {
        viewModel.getEvaluation().observe(this, data -> {
            if (!data.equals("wala")) {
                evaluationHtml = data;
                String htmlHeaders = "<html>" +
                        "<body>" +
                        evaluationHtml +
                        "</body></html>";
                preferenceManager.putString(PortalApp.KEY_HTML_EVALUATION, data);

                myWebView.loadDataWithBaseURL(null, htmlHeaders, "text/html", "UTF-8", null);

            } else {
                myWebView.loadDataWithBaseURL(null, preferenceManager.getString(PortalApp.KEY_HTML_EVALUATION), "text/html", "UTF-8", null);
            }
            binding.myWebView.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.GONE);
        });
    }

    void observeErr() {
        viewModel.getErr().observe(this, err -> {
            Toast.makeText(this, err.getMessage(), Toast.LENGTH_SHORT).show();
            if (err != null) {
                myWebView.loadDataWithBaseURL(null, preferenceManager.getString(PortalApp.KEY_HTML_EVALUATION), "text/html", "UTF-8", null);
                binding.myWebView.setVisibility(View.VISIBLE);
                binding.progressBar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected ActivityEvaluationBinding createBinding(LayoutInflater layoutInflater) {
        return ActivityEvaluationBinding.inflate(layoutInflater);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        myWebView.destroy();
    }
}