package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_checks")
data class DailyCheckEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val studentId: String,
    val dateString: String, // format YYYY-MM-DD
    val hadBreakfast: Boolean,
    val mealCount: Int,
    val ateVegetable: Boolean,
    val ateFruit: Boolean,
    val sugaryDrinkCount: String, // "Tidak ada", "1 kali", "2 kali", "Lebih dari 2 kali"
    val didPhysicalActivity: Boolean,
    val physicalActivityDuration: String, // "< 30 menit", "30–60 menit", "> 60 menit"
    val sedentaryDuration: String, // "< 3 jam", "3–6 jam", "> 6 jam"
    val sleepDuration: String, // "< 6 jam", "6–8 jam", "> 8 jam"
    val bodyCondition: String, // "Sangat Segar", "Cukup Bugar", "Lemas / Lelah", "Kurang Enak Badan"
    val summaryAdvice: String,
    val isLocked: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
