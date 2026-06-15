package com.example.myhealthconsultant.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "cabinet_medicines",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId"), Index("prescriptionId")]
)
data class CabinetMedicine(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val name: String,
    val genericName: String? = null,
    val category: String = "其他",
    val specification: String = "",
    val quantity: Int = 1,
    val unit: String = "盒",
    val productionDate: Long? = null,
    val expiryDate: Long? = null,
    val storageLocation: String = "",
    val notes: String = "",
    val photoUrl: String? = null,
    val prescriptionId: String? = null,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
