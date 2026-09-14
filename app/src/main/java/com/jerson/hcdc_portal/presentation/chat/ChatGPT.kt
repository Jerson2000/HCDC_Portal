package com.jerson.hcdc_portal.presentation.chat

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.google.gson.Gson
import com.jerson.hcdc_portal.R
import com.jerson.hcdc_portal.data.local.PortalDB
import com.jerson.hcdc_portal.databinding.ActivityChatgptBinding
import com.jerson.hcdc_portal.domain.model.Building
import com.jerson.hcdc_portal.domain.model.Chat
import com.jerson.hcdc_portal.domain.model.Floor
import com.jerson.hcdc_portal.domain.model.Grade
import com.jerson.hcdc_portal.domain.model.Role
import com.jerson.hcdc_portal.domain.model.Room
import com.jerson.hcdc_portal.domain.model.Rooms
import com.jerson.hcdc_portal.domain.model.Schedule
import com.jerson.hcdc_portal.presentation.chat.adapter.ChatGPTAdapter
import com.jerson.hcdc_portal.presentation.chat.viewmodel.ChatGPTViewModel
import com.jerson.hcdc_portal.util.ClassState
import com.jerson.hcdc_portal.util.LoadingDialog
import com.jerson.hcdc_portal.util.Resource
import com.jerson.hcdc_portal.util.ScheduleHelper
import com.jerson.hcdc_portal.util.SnackBarKt
import com.jerson.hcdc_portal.util.hideKeyboard
import com.jerson.hcdc_portal.util.isAtBottom
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class ChatGPT : AppCompatActivity() {

    private lateinit var binding: ActivityChatgptBinding
    private val viewModel: ChatGPTViewModel by viewModels()

    @Inject
    lateinit var portalDB: PortalDB

    private lateinit var loadingDialog: LoadingDialog
    private lateinit var adapter: ChatGPTAdapter
    private val chatList = mutableListOf<Chat>()
    private var cachedRooms: Room? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatgptBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            val navInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Adjust bottom padding so bottomContainer is never covered by the keyboard or navigation bar
            val bottomPadding = if (imeInsets.bottom > 0) imeInsets.bottom else navInsets.bottom
            binding.bottomContainer.setPadding(
                binding.bottomContainer.paddingLeft,
                binding.bottomContainer.paddingTop,
                binding.bottomContainer.paddingRight,
                bottomPadding
            )
            if (imeInsets.bottom > 0 && chatList.isNotEmpty()) {
                binding.recyclerView.post {
                    scrollToBottom(force = true)
                }
            }
            insets
        }

        loadLocalRoomData()
        setupUI()
        setupRecyclerView()
        setupSuggestedChips()
        observeViewModel()
    }

    private fun loadLocalRoomData() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val json = assets.open("rooms.json").bufferedReader().use { it.readText() }
                cachedRooms = Gson().fromJson(json, Room::class.java)
            } catch (_: Exception) {
                // Ignore if not present
            }
        }
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

        binding.bgImage.load(R.drawable.thinking) {
            size(260, 260)
        }

        binding.btnSend.setOnClickListener {
            val message = binding.msgET.text.toString().trim()
            if (message.isNotEmpty()) {
                submitUserQuery(message)
            }
        }

        binding.msgET.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && chatList.isNotEmpty() && !isFinishing) {
                binding.recyclerView.postDelayed({
                    scrollToBottom(force = true)
                }, 200)
            }
        }

        binding.msgET.setOnClickListener {
            if (chatList.isNotEmpty() && !isFinishing) {
                binding.recyclerView.postDelayed({
                    scrollToBottom(force = true)
                }, 200)
            }
        }
    }

    private fun setupSuggestedChips() {
        binding.chipNextClass.setOnClickListener {
            submitUserQuery(getString(R.string.prompt_next_class))
        }
        binding.chipGrades.setOnClickListener {
            submitUserQuery(getString(R.string.prompt_grades))
        }
        binding.chipBalance.setOnClickListener {
            submitUserQuery(getString(R.string.prompt_balance))
        }
        binding.chipSubjects.setOnClickListener {
            submitUserQuery(getString(R.string.prompt_subjects))
        }
        binding.chipRoom.setOnClickListener {
            submitUserQuery(getString(R.string.prompt_room))
        }
    }

    private fun setupRecyclerView() {
        adapter = ChatGPTAdapter(chatList)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun submitUserQuery(query: String) {
        binding.welcomeCard.visibility = View.GONE
        chatList.add(Chat(Role.USER, query))
        adapter.notifyItemInserted(chatList.lastIndex)
        scrollToBottom()

        binding.btnSend.icon = ContextCompat.getDrawable(this@ChatGPT, R.drawable.ic_more_horiz)
        binding.btnSend.isEnabled = false
        binding.msgET.text = null
        hideKeyboard()

        // Check if query can be answered with instant smart local portal data
        lifecycleScope.launch {
            val localAnswer = answerLocallyIfPossible(query)
            if (localAnswer != null) {
                deliverAssistantResponse(localAnswer)
            } else {
                viewModel.chat(chatList)
            }
        }
    }

    private suspend fun answerLocallyIfPossible(query: String): String? = withContext(Dispatchers.IO) {
        val q = query.lowercase().trim()

        // 1. Next Class / Today's Schedule
        if (q.contains("next class") || q.contains("my class") || q.contains("schedule today")) {
            val schedules: List<Schedule> = portalDB.scheduleDao().getSchedules().firstOrNull() ?: emptyList()
            if (schedules.isEmpty()) {
                return@withContext "You do not have any enrolled classes saved yet. Please sync your schedule first."
            }
            val nextInfo = ScheduleHelper.getNextClass(schedules)
            return@withContext if (nextInfo != null) {
                val s = nextInfo.schedule
                val code = s.subjectCode ?: "Class"
                val desc = s.description
                val room = ScheduleHelper.formatRoomBuilding(s.room)
                val time = s.time ?: "TBA"
                val status = nextInfo.statusText
                "🎓 **Your Next Class**\n\n• **Subject:** $code — $desc\n• **Time:** $time\n• **Room:** $room\n• **Status:** $status"
            } else {
                val todayClasses = schedules.filter { ScheduleHelper.isToday(it) }
                if (todayClasses.isEmpty()) {
                    "🎉 You have no classes scheduled for today! Enjoy your free time."
                } else {
                    "You're all done with your classes for today!"
                }
            }
        }

        // 2. Grades / GWA
        if (q.contains("grade") || q.contains("gwa") || q.contains("gpa") || q.contains("score")) {
            val grades: List<Grade> = portalDB.gradeDao().getAllGrades().firstOrNull() ?: emptyList()
            if (grades.isEmpty()) {
                return@withContext "No grades recorded for your account yet."
            }
            val sb = StringBuilder("📊 **Academic Performance Summary**\n\n")
            grades.take(6).forEach { grade: Grade ->
                val subj = grade.subjectCode ?: "Subject"
                val finalG = grade.finalGrade ?: grade.midGrade ?: "N/A"
                val remark = grade.finalRemark ?: grade.midRemark ?: ""
                sb.append("• **$subj**: $finalG ($remark)\n")
            }
            if (grades.size > 6) {
                sb.append("\n_...and ${grades.size - 6} more subjects in your Grades tab._")
            }
            return@withContext sb.toString()
        }

        // 3. Balance / Accountabilities / Fees
        if (q.contains("balance") || q.contains("accountabilit") || q.contains("fee") || q.contains("tuition") || q.contains("pay")) {
            val accounts = portalDB.accountDao().getAccounts().firstOrNull() ?: emptyList()
            if (accounts.isEmpty()) {
                return@withContext "💳 **Account Status**\n\nNo outstanding account records found. Your balance appears cleared or has not synced yet."
            }
            val totalBalance = accounts.lastOrNull()?.runningBal ?: "₱0.00"
            return@withContext "💳 **Account Overview**\n\n• **Outstanding Balance:** $totalBalance\n• Total recorded entries: ${accounts.size}\n\nCheck the **Finance** tab for the itemized transaction ledger."
        }

        // 4. Subjects enrolled
        if (q.contains("subject") || q.contains("enrolled") || q.contains("courses")) {
            val schedules = portalDB.scheduleDao().getSchedules().firstOrNull() ?: emptyList()
            if (schedules.isEmpty()) {
                return@withContext "No enrolled subjects found in your offline storage."
            }
            val sb = StringBuilder("📚 **Enrolled Subjects (${schedules.size})**\n\n")
            schedules.forEach { s: Schedule ->
                sb.append("• **${s.subjectCode}**: ${s.description} (${s.unit ?: "3.0"} units)\n")
            }
            return@withContext sb.toString()
        }

        // 5. Room / Building location query
        val roomRegex = Regex("room\\s*(\\d+|[a-zA-Z0-9]+)", RegexOption.IGNORE_CASE)
        val match = roomRegex.find(q)
        if (match != null || q.contains("building") || q.contains("where is") || q.contains("location")) {
            val target = match?.groupValues?.get(1)?.lowercase() ?: q
            val buildings: List<Building> = cachedRooms?.building ?: emptyList()
            for (b: Building in buildings) {
                for (f: Floor in b.floors) {
                    val found: Rooms? = f.rooms.firstOrNull { r: Rooms ->
                        r.room_no.contains(target, ignoreCase = true) ||
                        r.desc.contains(target, ignoreCase = true)
                    }
                    if (found != null) {
                        return@withContext "📍 **Room ${found.room_no}**\n\n• **Description:** ${found.desc.ifEmpty { "Classroom" }}\n• **Floor:** Floor ${f.floor_no}\n• **Building:** ${b.name.ifEmpty { "HCDC Campus" }}"
                    }
                }
            }
        }

        null // Fallback to remote AI API
    }

    private fun deliverAssistantResponse(response: String) {
        chatList.add(Chat(Role.ASSISTANT, response))
        binding.btnSend.icon = ContextCompat.getDrawable(this@ChatGPT, R.drawable.ic_send)
        binding.btnSend.isEnabled = true
        adapter.notifyItemInserted(chatList.lastIndex)
        scrollToBottom()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.fetchChatGPT.collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            resource.data?.let {
                                deliverAssistantResponse(it)
                                viewModel.resetChatResponse()
                            }
                        }

                        is Resource.Error -> {
                            resource.message?.let { msg ->
                                val friendly = com.jerson.hcdc_portal.util.ErrorMessageHelper.format(msg)
                                val fallback = "I couldn't reach the AI server ($friendly). You can still tap the suggestion chips above to ask about your **Next Class**, **Grades**, **Balance**, **Subjects**, or campus **Rooms**."
                                deliverAssistantResponse(fallback)
                                viewModel.resetChatResponse()
                            }
                        }

                        is Resource.Loading -> {
                            // Handled by btnSend state
                        }

                        else -> Unit
                    }
                }
            }
        }
    }

    private fun scrollToBottom(force: Boolean = false) {
        if (chatList.isEmpty()) return
        with(binding.recyclerView) {
            if (force || !isAtBottom) {
                smoothScrollToPosition(chatList.lastIndex)
            }
        }
    }
}