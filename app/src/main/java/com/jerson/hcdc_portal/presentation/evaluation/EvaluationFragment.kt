package com.jerson.hcdc_portal.presentation.evaluation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.jerson.hcdc_portal.databinding.FragmentEvaluationEmbeddedBinding
import com.jerson.hcdc_portal.presentation.evaluation.viewmodel.EvaluationViewModel
import com.jerson.hcdc_portal.presentation.login.viewmodel.LoginViewModel
import com.jerson.hcdc_portal.util.AppPreference
import com.jerson.hcdc_portal.util.Constants
import com.jerson.hcdc_portal.util.Resource
import com.jerson.hcdc_portal.util.SnackBarKt
import com.jerson.hcdc_portal.util.setupCustomTheme
import com.jerson.hcdc_portal.util.stopRefreshing
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class EvaluationFragment : Fragment() {

    private var _binding: FragmentEvaluationEmbeddedBinding? = null
    private val binding get() = _binding!!
    private val evalViewModel: EvaluationViewModel by viewModels()
    private val loginViewModel: LoginViewModel by viewModels()
    private lateinit var webView: WebView

    @Inject
    lateinit var pref: AppPreference

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEvaluationEmbeddedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.swipeRefresh.setupCustomTheme(requireContext())
        binding.swipeRefresh.setOnRefreshListener {
            evalViewModel.fetchEvaluation()
        }

        webView = binding.myWebView
        webView.settings.apply {
            allowFileAccess = false
            allowContentAccess = false
        }

        val cachedHtml = pref.getStringPreference(Constants.KEY_HTML_EVALUATION)
        if (cachedHtml.isNotBlank()) {
            displayToWebView(cachedHtml)
        }

        val isLoaded = pref.getBooleanPreference(Constants.KEY_HTML_EVALUATION_LOADED)
        if (!isLoaded) {
            evalViewModel.fetchEvaluation()
        }

        observeEvaluation()
        observeRelogin()
    }

    private fun observeEvaluation() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                evalViewModel.fetchEvaluation.collect { res ->
                    when (res) {
                        is Resource.Loading -> {
                            if (pref.getStringPreference(Constants.KEY_HTML_EVALUATION).isBlank()) {
                                _binding?.progressBar?.visibility = View.VISIBLE
                            }
                        }
                        is Resource.Success -> {
                            _binding?.progressBar?.visibility = View.GONE
                            _binding?.swipeRefresh?.stopRefreshing()
                            res.data?.let {
                                displayToWebView(it)
                            }
                        }
                        is Resource.Error -> {
                            _binding?.progressBar?.visibility = View.GONE
                            _binding?.swipeRefresh?.stopRefreshing()
                            val msg = res.message.orEmpty()
                            if (msg.contains("session end", true)) {
                                loginViewModel.reLogon()
                            } else if (msg.isNotBlank()) {
                                _binding?.let { b -> SnackBarKt.snackBarLong(b.root, msg) }
                            }
                        }
                        else -> Unit
                    }
                }
            }
        }
    }

    private fun observeRelogin() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                loginViewModel.login.collect { res ->
                    if (res is Resource.Success) {
                        evalViewModel.fetchEvaluation()
                    }
                }
            }
        }
    }

    private fun displayToWebView(htmlData: String) {
        val html = "<html><head><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"></head><body>$htmlData</body></html>"
        webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        webView.destroy()
        _binding = null
    }
}
