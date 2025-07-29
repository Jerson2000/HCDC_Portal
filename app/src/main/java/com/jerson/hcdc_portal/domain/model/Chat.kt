package com.jerson.hcdc_portal.domain.model

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

data class Chat(
    val role: Role,
    val content: String
){
    override fun toString(): String {
        return """{"role":"${role.name.lowercase()}","content":"$content"}"""
    }
}
enum class Role {
    ASSISTANT,
    USER
}

class RoleSerializer : JsonSerializer<Role> {
    override fun serialize(src: Role?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return JsonPrimitive(src?.name?.lowercase())
    }
}
