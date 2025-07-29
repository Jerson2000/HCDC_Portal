package com.jerson.hcdc_portal.domain.repository

import com.jerson.hcdc_portal.domain.model.Chat
import com.jerson.hcdc_portal.util.Resource
import kotlinx.coroutines.flow.Flow

interface ChatGPTRepository {
    suspend fun chat(chatList:List<Chat>): Flow<Resource<String>>
}