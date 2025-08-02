package com.jerson.hcdc_portal.presentation.chat

import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.jerson.hcdc_portal.R
import com.jerson.hcdc_portal.databinding.ActivityChatgptBinding
import com.jerson.hcdc_portal.domain.model.Chat
import com.jerson.hcdc_portal.domain.model.Role
import com.jerson.hcdc_portal.presentation.chat.adapter.ChatGPTAdapter
import com.jerson.hcdc_portal.presentation.chat.viewmodel.ChatGPTViewModel
import com.jerson.hcdc_portal.util.LoadingDialog
import com.jerson.hcdc_portal.util.Resource
import com.jerson.hcdc_portal.util.SnackBarKt
import com.jerson.hcdc_portal.util.hideKeyboard
import com.jerson.hcdc_portal.util.isAtBottom
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChatGPT : AppCompatActivity() {

    private lateinit var binding: ActivityChatgptBinding
    private val viewModel: ChatGPTViewModel by viewModels()

    private lateinit var loadingDialog: LoadingDialog
    private lateinit var adapter: ChatGPTAdapter
    private val chatList = mutableListOf<Chat>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatgptBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupUI() {
        loadingDialog = LoadingDialog(this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.bgImage.load(R.drawable.thinking){
            size(200,200)
        }

        binding.btnSend.setOnClickListener {
            handleSendMessage()
        }

        binding.msgET.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP && chatList.isNotEmpty() && !isFinishing) {
                scrollToBottom()
                v.performClick()
            }
            false
        }

    }

    private fun setupRecyclerView() {
        adapter = ChatGPTAdapter(chatList)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun handleSendMessage() {
        val message = binding.msgET.text.toString().trim()
        if (message.isNotEmpty()) {
            chatList.add(Chat(Role.USER, message))
            viewModel.chat(chatList)
            updateUIAfterSending()
        }
    }

    private fun updateUIAfterSending() {
        binding.btnSend.icon = ContextCompat.getDrawable(this@ChatGPT, R.drawable.ic_more_horiz)
        binding.btnSend.isEnabled = false
        binding.msgET.text = null
        adapter.notifyItemInserted(chatList.lastIndex)
        scrollToBottom()
        hideKeyboard()
    }



    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.fetchChatGPT.collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            resource.data?.let {
                                chatList.add(Chat(Role.ASSISTANT, it))
                                binding.btnSend.icon = ContextCompat.getDrawable(this@ChatGPT, R.drawable.ic_send)
                                binding.btnSend.isEnabled = true
                                adapter.notifyItemInserted(chatList.lastIndex)
                                scrollToBottom()
                            }
                        }

                        is Resource.Error -> {
                            resource.message?.let { msg ->
                                SnackBarKt.snackBarLong(binding.root, msg)
                                binding.btnSend.isEnabled = true
                            }
                        }

                        is Resource.Loading -> {
                            // Optional: Show loading indicator
                        }

                        else -> Unit
                    }
                }
            }
        }
    }

    private fun scrollToBottom() {
        with(binding.recyclerView) {
            if (!isAtBottom) {
                smoothScrollToPosition(chatList.lastIndex)
            }
        }
    }
}