package com.jerson.hcdc_portal.domain.repository

import com.jerson.hcdc_portal.domain.model.ChatGPT
import com.jerson.hcdc_portal.util.Resource
import kotlinx.coroutines.flow.Flow
import okhttp3.FormBody

interface ChatGPTRepository {
    suspend fun chat(formBody: FormBody): Flow<Resource<ChatGPT>>
    suspend fun chatDataValue():Flow<Resource<FormBody.Builder>>
}