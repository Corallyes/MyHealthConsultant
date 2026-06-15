package com.example.myhealthconsultant.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * 用药记录实体 - 记录用户的打卡/服药记录
 */
@Entity(
    tableName = "medication_records",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MedicationPlan::class,
            parentColumns = ["id"],
            childColumns = ["planId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId"), Index("planId"), Index("takenDate")]
)
data class MedicationRecord(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userId: String,                  // 用户ID
    val planId: String,                  // 关联计划ID
    val drugName: String,                // 药品名称
    val dosage: String,                  // 实际服用剂量
    val takenDate: Long,                 // 服药日期时间戳（只保留日期部分）
    val takenTime: String?,              // 服药时间（HH:mm）
    val isTaken: Boolean = true,         // 是否已服用
    val notes: String?,                  // 备注
    val createdAt: Long = System.currentTimeMillis()
)
