package com.jerson.hcdc_portal.presentation.chat.adapter
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.jerson.hcdc_portal.databinding.ItemContainerChatAiBinding
import com.jerson.hcdc_portal.databinding.ItemContainerChatUserBinding
import com.jerson.hcdc_portal.domain.model.Chat
import com.jerson.hcdc_portal.domain.model.Role
import io.noties.markwon.Markwon

class ChatGPTAdapter(private val list: List<Chat>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // Track which messages have already been animated
    private val animatedMessages = mutableSetOf<Int>()

    inner class UserViewHolder(private val binding: ItemContainerChatUserBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(chat: Chat) {
            binding.textMessage.text = chat.content
        }
    }

    inner class AIViewHolder(private val binding: ItemContainerChatAiBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val handler = Handler(Looper.getMainLooper())
        private var currentRunnable: Runnable? = null

        fun bind(chat: Chat, position: Int) {
            currentRunnable?.let { handler.removeCallbacks(it) }
            binding.textMessage.text = ""

            val content = chat.content
            val markWon = Markwon.create(binding.root.context)

            if (animatedMessages.contains(position)) {
//                binding.textMessage.text = content
                markWon.setMarkdown(binding.textMessage,content)
            } else {
                var index = 0
                val typeWriter = object : Runnable {
                    override fun run() {
                        if (index <= content.length) {
                            markWon.setMarkdown(binding.textMessage,content.substring(0, index))
                            index++
                            handler.postDelayed(this, 30L)
                        } else {
                            animatedMessages.add(position) // Mark as animated
                        }
                    }
                }
                currentRunnable = typeWriter
                handler.post(typeWriter)
            }

            binding.textMessage.setOnLongClickListener {
                copyToClipboard(content, binding.root.context)
                true
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return list[position].role.ordinal
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            Role.USER.ordinal -> {
                val binding = ItemContainerChatUserBinding.inflate(inflater, parent, false)
                UserViewHolder(binding)
            }
            Role.ASSISTANT.ordinal -> {
                val binding = ItemContainerChatAiBinding.inflate(inflater, parent, false)
                AIViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val chat = list[position]
        when (holder) {
            is UserViewHolder -> holder.bind(chat)
            is AIViewHolder -> holder.bind(chat, position)
        }
    }

    override fun getItemCount(): Int = list.size

    private fun copyToClipboard(text: String, context: Context) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("text", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show()
    }
}