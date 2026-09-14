package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String, // e.g. student email or officer id
    val email: String,
    val nameOrInitial: String,
    val role: String, // "STUDENT" or "UKS"
    val className: String = "",
    val age: Int = 16,
    val gender: String = "Laki-laki", // "Laki-laki" / "Perempuan"
    val studentIdNumber: String = "", // NIS / NIP
    val madrasahName: String = "Madrasah Aliyah Negeri",
    val passwordHash: String = "",
    val isVerified: Boolean = true, // UKS accounts start unverified unless approved
    val isProfileComplete: Boolean = false,
    val agreedToTerms: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val lastLogin: Long = System.currentTimeMillis()
)
