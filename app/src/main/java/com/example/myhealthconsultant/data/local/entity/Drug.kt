package com.example.myhealthconsultant.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * 药品实体 - 预置OTC药品数据
 */
@Entity(tableName = "drugs")
data class Drug(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,                    // 药品名称
    val genericName: String?,            // 通用名
    val category: String,                // 分类（感冒类/消炎类等）
    val type: String,                    // 类型（OTC/处方药）
    val ingredients: String,             // 成分
    val indications: String,             // 适应症
    val dosage: String,                  // 用法用量
    val sideEffects: String,             // 不良反应
    val contraindications: String,       // 禁忌
    val precautions: String?,            // 注意事项
    val imageUrl: String? = null,        // 网络图片URL
    val imageResId: Int? = null,         // 本地drawable资源ID
    val isFavorite: Boolean = false,     // 是否收藏
    val createdAt: Long = System.currentTimeMillis()
)
