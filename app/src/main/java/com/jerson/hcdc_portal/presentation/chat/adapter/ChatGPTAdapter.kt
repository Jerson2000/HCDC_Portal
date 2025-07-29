package com.jerson.hcdc_portal.presentation.chat.adapter

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.jerson.hcdc_portal.databinding.ItemContainerChatAiBinding
import com.jerson.hcdc_portal.databinding.ItemContainerChatUserBinding
import com.jerson.hcdc_portal.domain.model.Chat
import com.jerson.hcdc_portal.domain.model.Role

class ChatGPTAdapter(private val list:List<Chat>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    inner class UserViewHolder(private val binding: ItemContainerChatUserBinding):RecyclerView.ViewHolder(binding.root){
        fun bind(chat: Chat){
            binding.textMessage.text = chat.content
        }
    }

    private fun createUserViewHolder(parent: ViewGroup): UserViewHolder {
        val binding = ItemContainerChatUserBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UserViewHolder(binding)
    }


    inner class AIViewHolder(private val binding: ItemContainerChatAiBinding):RecyclerView.ViewHolder(binding.root){
        fun bind(chat: Chat){
            binding.textMessage.text = chat.content
            binding.textMessage.setOnLongClickListener{
                copyToClipboard(binding.textMessage.text.toString(),binding.root.context)
                true
            }

        }
    }

    private fun createAIViewHolder(parent: ViewGroup): AIViewHolder {
        val binding = ItemContainerChatAiBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AIViewHolder(binding)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            Role.USER.ordinal -> createUserViewHolder(parent)
            Role.ASSISTANT.ordinal -> createAIViewHolder(parent)
            else -> throw IllegalArgumentException("Invalid view type")
        }

    }



    override fun getItemViewType(position: Int): Int {
        return list[position].role.ordinal
    }


    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder.itemViewType) {
            Role.USER.ordinal -> (holder as UserViewHolder).bind(list[position])
            Role.ASSISTANT.ordinal -> (holder as AIViewHolder).bind(list[position])
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    fun copyToClipboard(text: String, context: Context) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("text", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context,"Copied!",Toast.LENGTH_LONG).show()
    }

}