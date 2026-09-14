package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "food_scans")
data class FoodScanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val studentId: String,
    val imagePath: String, // file path or drawable identifier
    val detectedFoods: String,
    val foodCategory: String,
    val carbsSource: String,
    val proteinSource: String,
    val vegFruitSource: String,
    val approxNutrients: String,
    val balanceAssessment: String,
    val educationalFeedback: String,
    val disclaimer: String = "Analisis foto merupakan perkiraan dan dapat berbeda dari kondisi sebenarnya.",
    val dateString: String,
    val timestamp: Long = System.currentTimeMillis()
)
