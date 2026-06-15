package com.example.myhealthconsultant.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * 对话历史实体 - 存储AI对话记录
 */
@Entity(
    tableName = "chat_history",
    indices = [Index("userId"), Index("sessionId")]
)
data class ChatHistory(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userId: String,                  // 用户ID
    val sessionId: String,               // 会话ID
    val role: String,                    // user/assistant
    val content: String,                 // 消息内容
    val timestamp: Long = System.currentTimeMillis()
)
