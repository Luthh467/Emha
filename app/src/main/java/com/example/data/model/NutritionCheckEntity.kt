package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "nutrition_checks")
data class NutritionCheckEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val studentId: String,
    val studentName: String,
    val age: Int,
    val gender: String,
    val weightKg: Float,
    val heightCm: Float,
    val bmi: Float,
    val bmiCategory: String, // "Gizi Kurang", "Gizi Baik (Normal)", "Gizi Lebih", "Obesitas"
    val riskCategory: String, // "Risiko Rendah", "Perlu Perhatian", "Risiko Tinggi"
    val breakfastHabit: String,
    val vegHabit: String,
    val fruitHabit: String,
    val fastFoodHabit: String,
    val sugaryDrinkHabit: String,
    val physicalActivity: String,
    val sedentaryDuration: String,
    val factorsToWatch: String,
    val generalAdvice: String,
    val dateString: String,
    val timestamp: Long = System.currentTimeMillis()
)
