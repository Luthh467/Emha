package com.example.nutrimind.data.repository

import com.example.nutrimind.data.model.UserProfile
import com.example.nutrimind.data.model.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repository autentikasi Firebase Auth dan otorisasi peran (Siswa vs Guru)
 */
class AuthRepository {

    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUser: Flow<UserProfile?> = _currentUser.asStateFlow()

    fun getCurrentUser(): UserProfile? = _currentUser.value

    fun isUserSignedIn(): Boolean = _currentUser.value != null

    fun isTeacher(): Boolean = _currentUser.value?.role == UserRole.GURU

    fun isStudent(): Boolean = _currentUser.value?.role == UserRole.SISWA

    suspend fun saveUserProfile(profile: UserProfile): Result<Unit> {
        return try {
            _currentUser.value = profile
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signOut() {
        _currentUser.value = null
    }
}
