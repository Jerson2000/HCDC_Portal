package com.jerson.hcdc_portal.domain.model

data class Chat(
    val role:Int,
    val message:String?
)
data class ChatGPT(
    val status:String?,
    val msg:String?,
    val data:String?
)

enum class Role(val value: Int) {
    AI(1),
    USER(0);

    companion object {
        fun fromValue(value: Int): Role? {
            return entries.find { it.value == value }
        }
    }
}