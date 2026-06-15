package com.example.myhealthconsultant.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * 用户实体
 */
@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val phone: String,                   // 手机号 (唯一标识)
    val passwordHash: String,            // 密码哈希
    val nickname: String = "",           // 昵称
    val avatarUrl: String? = null,       // 头像URL
    val wechatOpenId: String? = null,    // 微信OpenID
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
