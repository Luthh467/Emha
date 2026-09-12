package com.example.data.remote

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.DailyCheckEntity
import com.example.data.model.NutritionCheckEntity
import com.example.data.model.UksFollowUpEntity
import com.example.data.model.UserEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Layanan Cloud Firestore untuk menyinkronkan data cek gizi siswa dari berbagai perangkat (HP masing-masing siswa)
 * ke satu Dashboard UKS madrasah secara real-time.
 */
class FirestoreSyncService(private val context: Context) {

    private val firestore: FirebaseFirestore by lazy {
        val dbId = context.getString(R.string.firestore_database_id)
        FirebaseFirestore.getInstance(dbId)
    }

    companion object {
        private const val TAG = "FirestoreSync"
        private const val COL_USERS = "users"
        private const val COL_NUTRITION_CHECKS = "nutrition_checks"
        private const val COL_DAILY_CHECKS = "daily_checks"
        private const val COL_FOLLOW_UPS = "uks_follow_ups"
    }

    // ==================== PENGGUNA (SISWA & PETUGAS UKS) ====================

    suspend fun syncUserToCloud(user: UserEntity) {
        try {
            val docData = hashMapOf(
                "id" to user.id,
                "email" to user.email,
                "nameOrInitial" to user.nameOrInitial,
                "role" to user.role,
                "className" to user.className,
                "age" to user.age,
                "gender" to user.gender,
                "studentIdNumber" to user.studentIdNumber,
                "madrasahName" to user.madrasahName,
                "isVerified" to user.isVerified,
                "isProfileComplete" to user.isProfileComplete,
                "agreedToTerms" to user.agreedToTerms,
                "createdAt" to user.createdAt,
                "lastLogin" to user.lastLogin
            )
            firestore.collection(COL_USERS).document(user.id).set(docData).await()
            Log.d(TAG, "Successfully synced user to cloud: ${user.id}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync user to cloud: ${e.message}", e)
        }
    }

    fun observeAllStudentsFromCloud(): Flow<List<UserEntity>> = callbackFlow {
        var listener: ListenerRegistration? = null
        try {
            listener = firestore.collection(COL_USERS)
                .whereEqualTo("role", "STUDENT")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Listen error for students: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val list = snapshot.documents.mapNotNull { doc ->
                            try {
                                UserEntity(
                                    id = doc.getString("id") ?: doc.id,
                                    email = doc.getString("email") ?: "",
                                    nameOrInitial = doc.getString("nameOrInitial") ?: "",
                                    role = doc.getString("role") ?: "STUDENT",
                                    className = doc.getString("className") ?: "",
                                    age = (doc.getLong("age") ?: 16L).toInt(),
                                    gender = doc.getString("gender") ?: "Laki-laki",
                                    studentIdNumber = doc.getString("studentIdNumber") ?: "",
                                    madrasahName = doc.getString("madrasahName") ?: "Madrasah Aliyah Negeri",
                                    isVerified = doc.getBoolean("isVerified") ?: true,
                                    isProfileComplete = doc.getBoolean("isProfileComplete") ?: true,
                                    agreedToTerms = doc.getBoolean("agreedToTerms") ?: true,
                                    createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                                    lastLogin = doc.getLong("lastLogin") ?: System.currentTimeMillis()
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }
                        trySend(list)
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error attaching students listener: ${e.message}")
        }
        awaitClose { listener?.remove() }
    }

    // ==================== CEK GIZI SISWA (NUTRITION CHECKS) ====================

    suspend fun syncNutritionCheckToCloud(check: NutritionCheckEntity) {
        try {
            val docId = if (check.id > 0) "${check.studentId}_${check.id}" else "${check.studentId}_${check.timestamp}"
            val docData = hashMapOf(
                "studentId" to check.studentId,
                "studentName" to check.studentName,
                "age" to check.age,
                "gender" to check.gender,
                "weightKg" to check.weightKg.toDouble(),
                "heightCm" to check.heightCm.toDouble(),
                "bmi" to check.bmi.toDouble(),
                "bmiCategory" to check.bmiCategory,
                "riskCategory" to check.riskCategory,
                "breakfastHabit" to check.breakfastHabit,
                "vegHabit" to check.vegHabit,
                "fruitHabit" to check.fruitHabit,
                "fastFoodHabit" to check.fastFoodHabit,
                "sugaryDrinkHabit" to check.sugaryDrinkHabit,
                "physicalActivity" to check.physicalActivity,
                "sedentaryDuration" to check.sedentaryDuration,
                "factorsToWatch" to check.factorsToWatch,
                "generalAdvice" to check.generalAdvice,
                "dateString" to check.dateString,
                "timestamp" to check.timestamp
            )
            firestore.collection(COL_NUTRITION_CHECKS).document(docId).set(docData).await()
            Log.d(TAG, "Successfully uploaded nutrition check to cloud for student: ${check.studentId}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to upload nutrition check to cloud: ${e.message}", e)
        }
    }

    fun observeAllNutritionChecksFromCloud(): Flow<List<NutritionCheckEntity>> = callbackFlow {
        var listener: ListenerRegistration? = null
        try {
            listener = firestore.collection(COL_NUTRITION_CHECKS)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Listen error for nutrition checks: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val list = snapshot.documents.mapNotNull { doc ->
                            try {
                                NutritionCheckEntity(
                                    id = (doc.getLong("timestamp") ?: System.currentTimeMillis()),
                                    studentId = doc.getString("studentId") ?: "",
                                    studentName = doc.getString("studentName") ?: "Siswa",
                                    age = (doc.getLong("age") ?: 16L).toInt(),
                                    gender = doc.getString("gender") ?: "Laki-laki",
                                    weightKg = (doc.getDouble("weightKg") ?: 50.0).toFloat(),
                                    heightCm = (doc.getDouble("heightCm") ?: 160.0).toFloat(),
                                    bmi = (doc.getDouble("bmi") ?: 20.0).toFloat(),
                                    bmiCategory = doc.getString("bmiCategory") ?: "Gizi Baik (Normal)",
                                    riskCategory = doc.getString("riskCategory") ?: "Risiko Rendah",
                                    breakfastHabit = doc.getString("breakfastHabit") ?: "",
                                    vegHabit = doc.getString("vegHabit") ?: "",
                                    fruitHabit = doc.getString("fruitHabit") ?: "",
                                    fastFoodHabit = doc.getString("fastFoodHabit") ?: "",
                                    sugaryDrinkHabit = doc.getString("sugaryDrinkHabit") ?: "",
                                    physicalActivity = doc.getString("physicalActivity") ?: "",
                                    sedentaryDuration = doc.getString("sedentaryDuration") ?: "",
                                    factorsToWatch = doc.getString("factorsToWatch") ?: "",
                                    generalAdvice = doc.getString("generalAdvice") ?: "",
                                    dateString = doc.getString("dateString") ?: "",
                                    timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }
                        trySend(list)
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error attaching nutrition checks listener: ${e.message}")
        }
        awaitClose { listener?.remove() }
    }

    // ==================== CEK HARIAN ====================

    suspend fun syncDailyCheckToCloud(check: DailyCheckEntity) {
        try {
            val docId = "${check.studentId}_${check.dateString}"
            val docData = hashMapOf(
                "studentId" to check.studentId,
                "dateString" to check.dateString,
                "hadBreakfast" to check.hadBreakfast,
                "mealCount" to check.mealCount,
                "ateVegetable" to check.ateVegetable,
                "ateFruit" to check.ateFruit,
                "sugaryDrinkCount" to check.sugaryDrinkCount,
                "didPhysicalActivity" to check.didPhysicalActivity,
                "physicalActivityDuration" to check.physicalActivityDuration,
                "sedentaryDuration" to check.sedentaryDuration,
                "sleepDuration" to check.sleepDuration,
                "bodyCondition" to check.bodyCondition,
                "summaryAdvice" to check.summaryAdvice,
                "timestamp" to check.timestamp
            )
            firestore.collection(COL_DAILY_CHECKS).document(docId).set(docData).await()
            Log.d(TAG, "Successfully synced daily check to cloud: $docId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync daily check to cloud: ${e.message}", e)
        }
    }

    // ==================== TINDAK LANJUT UKS ====================

    suspend fun syncFollowUpToCloud(followUp: UksFollowUpEntity) {
        try {
            val docId = "${followUp.studentId}_${followUp.timestamp}"
            val docData = hashMapOf(
                "studentId" to followUp.studentId,
                "officerName" to followUp.officerName,
                "note" to followUp.note,
                "actionType" to followUp.actionType,
                "dateString" to followUp.dateString,
                "timestamp" to followUp.timestamp
            )
            firestore.collection(COL_FOLLOW_UPS).document(docId).set(docData).await()
            Log.d(TAG, "Successfully synced follow up to cloud: $docId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync follow up to cloud: ${e.message}", e)
        }
    }

    fun observeAllFollowUpsFromCloud(): Flow<List<UksFollowUpEntity>> = callbackFlow {
        var listener: ListenerRegistration? = null
        try {
            listener = firestore.collection(COL_FOLLOW_UPS)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Listen error for follow ups: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val list = snapshot.documents.mapNotNull { doc ->
                            try {
                                UksFollowUpEntity(
                                    id = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                                    studentId = doc.getString("studentId") ?: "",
                                    officerName = doc.getString("officerName") ?: "",
                                    note = doc.getString("note") ?: "",
                                    actionType = doc.getString("actionType") ?: "Konsultasi Edukatif",
                                    dateString = doc.getString("dateString") ?: "",
                                    timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }
                        trySend(list)
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error attaching follow ups listener: ${e.message}")
        }
        awaitClose { listener?.remove() }
    }
}
