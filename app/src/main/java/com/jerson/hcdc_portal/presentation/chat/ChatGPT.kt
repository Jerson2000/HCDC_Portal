package com.jerson.hcdc_portal.presentation.chat

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.jerson.hcdc_portal.databinding.ActivityChatgptBinding
import com.jerson.hcdc_portal.domain.model.Chat
import com.jerson.hcdc_portal.domain.model.Role
import com.jerson.hcdc_portal.presentation.chat.adapter.ChatGPTAdapter
import com.jerson.hcdc_portal.presentation.chat.viewmodel.ChatGPTViewModel
import com.jerson.hcdc_portal.util.LoadingDialog
import com.jerson.hcdc_portal.util.Resource
import com.jerson.hcdc_portal.util.SnackBarKt
import com.jerson.hcdc_portal.util.hideKeyboard
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChatGPT : AppCompatActivity() {
    private lateinit var binding: ActivityChatgptBinding
    private val chatGPTViewModel: ChatGPTViewModel by viewModels()
    private lateinit var loadingDialog: LoadingDialog
    private val chatList = mutableListOf<Chat>()
    private lateinit var adapter: ChatGPTAdapter


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatgptBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadingDialog = LoadingDialog(this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        adapter = ChatGPTAdapter(chatList)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        chat()

        binding.btnSend.setOnClickListener {
            if (binding.msgET.text.toString().isNotEmpty()) {
                val msg = binding.msgET.text.toString()
                chatList.add(Chat(Role.USER, msg))
                chatGPTViewModel.chat(chatList)
                binding.btnSend.isEnabled = false
                adapter.notifyDataSetChanged()
                binding.msgET.text = null
                binding.recyclerView.smoothScrollToPosition(adapter.itemCount - 1)

                this.hideKeyboard()
            }
        }
        binding.msgET.setOnTouchListener { _, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_UP) {
                if (chatList.isNotEmpty() && !isFinishing) {
                    binding.recyclerView.smoothScrollToPosition(adapter.itemCount - 1)
                }
            }
            false
        }

        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun chat() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                chatGPTViewModel.fetchChatGPT.collect {
                    when (it) {
                        is Resource.Success -> {
                            chatList.add(Chat(Role.ASSISTANT, it.data!!))
                            binding.btnSend.isEnabled = true
                            binding.recyclerView.smoothScrollToPosition(adapter.itemCount - 1)
                            adapter.notifyDataSetChanged()
                        }

                        is Resource.Error -> {
                            it.message?.let { it1 -> SnackBarKt.snackBarLong(binding.root, it1) }
                        }

                        else -> Unit
                    }
                }
            }
        }
    }
}