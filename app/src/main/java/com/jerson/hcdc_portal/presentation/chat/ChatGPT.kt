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
import okhttp3.FormBody

@AndroidEntryPoint
class ChatGPT:AppCompatActivity() {
    private lateinit var binding:ActivityChatgptBinding
    private val chatGPTViewModel:ChatGPTViewModel by viewModels()
    private lateinit var loadingDialog: LoadingDialog
    private var formBodyBuilder:FormBody.Builder?=null
    private val chatList = mutableListOf<Chat>()
    private lateinit var adapter:ChatGPTAdapter


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatgptBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadingDialog = LoadingDialog(this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply{
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        adapter = ChatGPTAdapter(chatList)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        getChatValue()
        chat()

        binding.btnSend.setOnClickListener{
            if(binding.msgET.text.toString().isNotEmpty()){
                formBodyBuilder?.let {
                    val msg = binding.msgET.text.toString()
                    val formBody = it.add("message",msg).build()
                    chatList.add(Chat(Role.USER.value, msg))
                    chatList.add(Chat(Role.AI.value, "Generating..."))
                    binding.btnSend.isEnabled = false
                    adapter.notifyDataSetChanged()
                    binding.msgET.text = null
                    binding.recyclerView.smoothScrollToPosition(adapter.itemCount - 1)
                    this.hideKeyboard()
                    chatGPTViewModel.chat(formBody)
                }
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

    private fun getChatValue(){
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                chatGPTViewModel.fetchChatDataValue.collect{
                    when(it){
                        is Resource.Loading->{
                            loadingDialog.show()
                        }
                        is  Resource.Success->{
                            loadingDialog.dismiss()
                            formBodyBuilder = it.data
                            chatList.add(Chat(Role.AI.value, "Hello!👋 I'm your helpful assistant."))
                            adapter.notifyDataSetChanged()
                        }
                        is Resource.Error->{
                            it.message?.let { it1 -> SnackBarKt.snackBarLong(binding.root, it1) }
                        }
                        else -> Unit
                    }
                }
            }
        }
    }

    private fun chat(){
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                chatGPTViewModel.fetchChatGPT.collect{
                    when(it){
                        is  Resource.Success->{
                            chatList.removeAt(chatList.size - 1)
                            chatList.add(Chat(Role.AI.value,it.data?.data))
                            binding.btnSend.isEnabled = true
                            binding.recyclerView.smoothScrollToPosition(adapter.itemCount - 1)
                            adapter.notifyDataSetChanged()
                        }
                        is Resource.Error->{
                            chatList.removeAt(chatList.size - 1)
                            chatList.add(Chat(Role.AI.value,it.message))
                            it.message?.let { it1 -> SnackBarKt.snackBarLong(binding.root, it1) }
                        }
                        else -> Unit
                    }
                }
            }
        }
    }
}