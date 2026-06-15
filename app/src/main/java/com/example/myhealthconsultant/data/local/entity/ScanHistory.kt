package com.example.myhealthconsultant.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * 扫描历史实体 - 存储拍照识药记录
 */
@Entity(
    tableName = "scan_history",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId")]
)
data class ScanHistory(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userId: String,                  // 用户ID
    val imageUrl: String,                // 图片本地路径
    val recognizedDrugName: String,      // 识别的药品名称
    val confidence: Float,               // 识别置信度 (0.0 - 1.0)
    val drugDetails: String?,            // 药品详情JSON字符串
    val scannedAt: Long = System.currentTimeMillis()
)
