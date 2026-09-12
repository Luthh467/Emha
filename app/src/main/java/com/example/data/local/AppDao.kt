package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DailyCheckEntity
import com.example.data.model.EducationArticle
import com.example.data.model.FoodScanEntity
import com.example.data.model.NutritionCheckEntity
import com.example.data.model.UksFollowUpEntity
import com.example.data.model.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // --- Users ---
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun getUserById(id: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmailDirect(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserByIdDirect(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE role = 'STUDENT' ORDER BY nameOrInitial ASC")
    fun getAllStudents(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET isVerified = :verified WHERE id = :userId")
    suspend fun verifyUser(userId: String, verified: Boolean)

    @Query("DELETE FROM users WHERE id IN ('siswa.ahmad@gmail.com', 'siswa.rahma@gmail.com', 'siswa.rizky@gmail.com', 'siswa.nuraini@gmail.com', 'uks@madrasah.sch.id')")
    suspend fun clearSampleUsers()

    // --- Nutrition Checks ---
    @Query("SELECT * FROM nutrition_checks WHERE studentId = :studentId ORDER BY timestamp DESC")
    fun getNutritionChecksForStudent(studentId: String): Flow<List<NutritionCheckEntity>>

    @Query("SELECT * FROM nutrition_checks WHERE studentId = :studentId ORDER BY timestamp DESC LIMIT 1")
    fun getLatestNutritionCheckForStudent(studentId: String): Flow<NutritionCheckEntity?>

    @Query("SELECT * FROM nutrition_checks ORDER BY timestamp DESC")
    fun getAllNutritionChecks(): Flow<List<NutritionCheckEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNutritionCheck(check: NutritionCheckEntity): Long

    @Query("DELETE FROM nutrition_checks WHERE id = :id")
    suspend fun deleteNutritionCheck(id: Long)

    // --- Daily Checks ---
    @Query("SELECT * FROM daily_checks ORDER BY timestamp DESC")
    fun getAllDailyChecks(): Flow<List<DailyCheckEntity>>

    @Query("SELECT * FROM daily_checks WHERE studentId = :studentId ORDER BY dateString DESC")
    fun getDailyChecksForStudent(studentId: String): Flow<List<DailyCheckEntity>>

    @Query("SELECT * FROM daily_checks WHERE studentId = :studentId AND dateString = :dateString LIMIT 1")
    fun getDailyCheckForDate(studentId: String, dateString: String): Flow<DailyCheckEntity?>

    @Query("SELECT * FROM daily_checks WHERE studentId = :studentId AND dateString = :dateString LIMIT 1")
    suspend fun getDailyCheckForDateDirect(studentId: String, dateString: String): DailyCheckEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyCheck(check: DailyCheckEntity): Long

    @Update
    suspend fun updateDailyCheck(check: DailyCheckEntity)

    // --- Food Scans ---
    @Query("SELECT * FROM food_scans ORDER BY timestamp DESC")
    fun getAllFoodScans(): Flow<List<FoodScanEntity>>

    @Query("SELECT * FROM food_scans WHERE studentId = :studentId ORDER BY timestamp DESC")
    fun getFoodScansForStudent(studentId: String): Flow<List<FoodScanEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodScan(scan: FoodScanEntity): Long

    @Query("DELETE FROM food_scans WHERE id = :id")
    suspend fun deleteFoodScan(id: Long)

    // --- Education Articles ---
    @Query("SELECT * FROM education_articles ORDER BY id ASC")
    fun getAllArticles(): Flow<List<EducationArticle>>

    @Query("SELECT * FROM education_articles WHERE isFavorite = 1")
    fun getFavoriteArticles(): Flow<List<EducationArticle>>

    @Query("UPDATE education_articles SET isFavorite = :isFav WHERE id = :id")
    suspend fun updateArticleFavorite(id: Long, isFav: Boolean)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticles(articles: List<EducationArticle>)

    @Query("SELECT COUNT(*) FROM education_articles")
    suspend fun getArticleCount(): Int

    // --- UKS Follow-Ups ---
    @Query("SELECT * FROM uks_follow_ups WHERE studentId = :studentId ORDER BY timestamp DESC")
    fun getFollowUpsForStudent(studentId: String): Flow<List<UksFollowUpEntity>>

    @Query("SELECT * FROM uks_follow_ups ORDER BY timestamp DESC")
    fun getAllFollowUps(): Flow<List<UksFollowUpEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFollowUp(followUp: UksFollowUpEntity): Long
}
