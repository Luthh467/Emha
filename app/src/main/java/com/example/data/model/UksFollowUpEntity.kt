package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "uks_follow_ups")
data class UksFollowUpEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val studentId: String,
    val officerName: String,
    val note: String,
    val actionType: String = "Konsultasi Edukatif", // e.g. "Konsultasi Kebiasaan Sarapan", "Edukasi Bekal Sehat", "Rujukan Nakes"
    val dateString: String,
    val timestamp: Long = System.currentTimeMillis()
)
