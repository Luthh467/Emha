package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "education_articles")
data class EducationArticle(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val category: String, // "Gizi Seimbang", "Sarapan", "Buah dan Sayur", "Makanan dan Minuman", "Aktivitas Fisik", "Tidur & Sehat"
    val summary: String,
    val content: String,
    val readTimeMinutes: Int = 3,
    val recommendedForStatus: String = "ALL", // "ALL", "Gizi Kurang", "Gizi Lebih", "Perlu Perhatian", "Risiko Tinggi"
    val isFavorite: Boolean = false,
    val publishedDate: String = "2026-09-01"
)
