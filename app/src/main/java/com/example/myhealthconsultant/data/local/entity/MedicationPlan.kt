package com.example.myhealthconsultant.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * 用药计划实体 - 用户设置的用药提醒计划
 */
@Entity(
    tableName = "medication_plans",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Drug::class,
            parentColumns = ["id"],
            childColumns = ["drugId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId"), Index("drugId")]
)
data class MedicationPlan(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userId: String,                  // 用户ID
    val drugId: String,                  // 药品ID
    val drugName: String,                // 药品名称（冗余存储）
    val dosage: String,                  // 剂量（如：1片）
    val frequency: String,               // 频次（每日1次/每日3次等）
    val timeSlot: String,                // 时间段（morning/afternoon/evening）
    val startDate: Long,                 // 开始日期
    val endDate: Long?,                  // 结束日期（可选）
    val reminderEnabled: Boolean,        // 是否启用提醒
    val reminderHour: Int?,              // 提醒小时（0-23）
    val reminderMinute: Int?,            // 提醒分钟（0-59）
    val mealBasedTime: String?,          // 餐后时间（after_breakfast/after_lunch/after_dinner，null为自定义）
    val setSystemAlarm: Boolean = false, // 是否同时设置系统闹钟
    val notes: String?,                  // 备注
    val isActive: Boolean = true,        // 是否激活
    val createdAt: Long = System.currentTimeMillis()
)
