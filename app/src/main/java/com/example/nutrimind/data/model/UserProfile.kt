package com.example.nutrimind.data.model

enum class UserRole {
    SISWA,
    GURU
}

data class UserProfile(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val role: UserRole = UserRole.SISWA,
    val nisn: String? = null,
    val kelas: String? = null,
    val nip: String? = null,
    val schoolName: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
