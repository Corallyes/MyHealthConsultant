package com.example.myhealthconsultant.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "prescriptions",
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
data class Prescription(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val doctorName: String = "",
    val hospitalName: String = "",
    val diagnosis: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
