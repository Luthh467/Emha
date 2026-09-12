package com.example.data

import android.content.Context
import android.graphics.Bitmap
import com.example.data.local.AppDao
import com.example.data.local.AppDatabase
import com.example.data.model.ChatMessage
import com.example.data.model.DailyCheckEntity
import com.example.data.model.EducationArticle
import com.example.data.model.FoodScanEntity
import com.example.data.model.NutritionCheckEntity
import com.example.data.model.UksFollowUpEntity
import com.example.data.model.UserEntity
import com.example.data.remote.FirestoreSyncService
import com.example.data.remote.FoodAnalysisResponse
import com.example.data.remote.GeminiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AppRepository(context: Context) {
    private val database = AppDatabase.getDatabase(context)
    private val dao: AppDao = database.appDao()
    private val prefs = context.getSharedPreferences("nutrimind_session", Context.MODE_PRIVATE)
    private val cloudSync = FirestoreSyncService(context)
    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _isSessionLoading = MutableStateFlow(true)
    val isSessionLoading: StateFlow<Boolean> = _isSessionLoading.asStateFlow()

    val hasActiveSession: Boolean
        get() = !prefs.getString("active_user_id", null).isNullOrBlank()

    init {
        repositoryScope.launch {
            try {
                dao.clearSampleUsers()
            } catch (e: Exception) {
                // ignore
            }
            val activeId = prefs.getString("active_user_id", null)
            if (!activeId.isNullOrBlank()) {
                val user = dao.getUserByIdDirect(activeId) ?: dao.getUserByEmailDirect(activeId)
                if (user != null) {
                    _currentUser.value = user
                } else {
                    prefs.edit().remove("active_user_id").apply()
                }
            }
            _isSessionLoading.value = false

            // Sinkronkan data dari Cloud Firestore ke Room Cache lokal secara real-time
            launch {
                try {
                    cloudSync.observeAllStudentsFromCloud().collect { cloudStudents ->
                        for (s in cloudStudents) {
                            dao.insertUser(s)
                        }
                    }
                } catch (e: Exception) {
                    // fallback local
                }
            }

            launch {
                try {
                    cloudSync.observeAllNutritionChecksFromCloud().collect { cloudChecks ->
                        for (c in cloudChecks) {
                            dao.insertNutritionCheck(c)
                        }
                    }
                } catch (e: Exception) {
                    // fallback local
                }
            }

            launch {
                try {
                    cloudSync.observeAllFollowUpsFromCloud().collect { cloudFollowUps ->
                        for (f in cloudFollowUps) {
                            dao.insertFollowUp(f)
                        }
                    }
                } catch (e: Exception) {
                    // fallback local
                }
            }
        }
    }

    // --- Gemini Chatbot State ---
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    fun initializeChatIfEmpty(studentName: String) {
        if (_chatMessages.value.isEmpty()) {
            val welcomeText = if (studentName.isNotBlank()) {
                "Assalamu’alaikum $studentName! Aku NutriMind AI, teman konsultasi gizi sehatmu di madrasah. Kamu bisa tanya apa saja seputar sarapan, bekal sehat, jajanan kantin, kebutuhan air putih, atau tips agar tidak lemas saat belajar. Ada yang ingin kamu tanyakan hari ini?"
            } else {
                "Assalamu’alaikum! Aku NutriMind AI, asisten gizi cerdas siswa madrasah. Mau tanya tips sarapan, bekal sehat, cara memilih jajanan kantin, atau kebutuhan air putih harianmu? Tanyakan apa saja ya!"
            }
            _chatMessages.value = listOf(
                ChatMessage(
                    isUser = false,
                    text = welcomeText
                )
            )
        }
    }

    suspend fun sendChatMessage(userText: String, studentName: String) {
        val trimmed = userText.trim()
        if (trimmed.isBlank()) return
        val userMsg = ChatMessage(isUser = true, text = trimmed)
        val currentList = _chatMessages.value.toMutableList()
        currentList.add(userMsg)

        // Add pending bot placeholder for streaming
        val botMsgId = java.util.UUID.randomUUID().toString()
        val pendingBotMsg = ChatMessage(
            id = botMsgId,
            isUser = false,
            text = "NutriMind sedang mengetik...",
            isStreaming = true
        )
        currentList.add(pendingBotMsg)
        _chatMessages.value = currentList

        // Call GeminiService with streaming update
        val historyForGemini = currentList.dropLast(2)
        GeminiService.streamChat(
            history = historyForGemini,
            userMessage = trimmed,
            studentName = studentName,
            onChunk = { currentChunkText ->
                _chatMessages.value = _chatMessages.value.map { msg ->
                    if (msg.id == botMsgId) {
                        msg.copy(text = currentChunkText, isStreaming = true)
                    } else {
                        msg
                    }
                }
            }
        )

        // Mark streaming as completed
        _chatMessages.value = _chatMessages.value.map { msg ->
            if (msg.id == botMsgId) {
                msg.copy(isStreaming = false)
            } else {
                msg
            }
        }
    }

    fun clearChat(studentName: String) {
        _chatMessages.value = emptyList()
        initializeChatIfEmpty(studentName)
    }

    fun setCurrentUser(user: UserEntity?) {
        _currentUser.value = user
        if (user != null) {
            prefs.edit().putString("active_user_id", user.id).apply()
        } else {
            prefs.edit().remove("active_user_id").apply()
        }
    }

    suspend fun getUserByEmail(email: String): UserEntity? {
        return dao.getUserByEmailDirect(email)
    }

    suspend fun getUserById(id: String): UserEntity? {
        return dao.getUserByIdDirect(id)
    }

    suspend fun saveUser(user: UserEntity) {
        dao.insertUser(user)
        _currentUser.value = user
        prefs.edit().putString("active_user_id", user.id).apply()
        cloudSync.syncUserToCloud(user)
    }

    suspend fun updateUserProfile(user: UserEntity) {
        dao.updateUser(user)
        _currentUser.value = user
        prefs.edit().putString("active_user_id", user.id).apply()
        cloudSync.syncUserToCloud(user)
    }

    suspend fun verifyUksUser(userId: String, verified: Boolean) {
        dao.verifyUser(userId, verified)
    }

    fun getAllStudents(): Flow<List<UserEntity>> = dao.getAllStudents()

    fun getAllUsers(): Flow<List<UserEntity>> = dao.getAllUsers()

    // --- Nutrition Checks ---
    fun getNutritionChecks(studentId: String): Flow<List<NutritionCheckEntity>> =
        dao.getNutritionChecksForStudent(studentId)

    fun getLatestNutritionCheck(studentId: String): Flow<NutritionCheckEntity?> =
        dao.getLatestNutritionCheckForStudent(studentId)

    fun getAllNutritionChecks(): Flow<List<NutritionCheckEntity>> = dao.getAllNutritionChecks()

    suspend fun saveNutritionCheck(check: NutritionCheckEntity): Long {
        val id = dao.insertNutritionCheck(check)
        val checkWithId = check.copy(id = id)
        cloudSync.syncNutritionCheckToCloud(checkWithId)
        return id
    }

    // --- Daily Checks ---
    fun getDailyChecks(studentId: String): Flow<List<DailyCheckEntity>> =
        dao.getDailyChecksForStudent(studentId)

    fun getDailyCheckForDate(studentId: String, dateString: String): Flow<DailyCheckEntity?> =
        dao.getDailyCheckForDate(studentId, dateString)

    suspend fun saveDailyCheck(check: DailyCheckEntity): Long {
        val id = dao.insertDailyCheck(check)
        val checkWithId = check.copy(id = id)
        cloudSync.syncDailyCheckToCloud(checkWithId)
        return id
    }

    suspend fun updateDailyCheck(check: DailyCheckEntity) {
        dao.updateDailyCheck(check)
        cloudSync.syncDailyCheckToCloud(check)
    }

    fun getAllDailyChecks(): Flow<List<DailyCheckEntity>> = dao.getAllDailyChecks()

    // --- Food Scans ---
    fun getAllFoodScans(): Flow<List<FoodScanEntity>> = dao.getAllFoodScans()

    fun getFoodScans(studentId: String): Flow<List<FoodScanEntity>> =
        dao.getFoodScansForStudent(studentId)

    suspend fun saveFoodScan(scan: FoodScanEntity): Long {
        return dao.insertFoodScan(scan)
    }

    // --- Education Articles ---
    fun getAllArticles(): Flow<List<EducationArticle>> = dao.getAllArticles()

    fun getFavoriteArticles(): Flow<List<EducationArticle>> = dao.getFavoriteArticles()

    suspend fun toggleArticleFavorite(id: Long, isFav: Boolean) {
        dao.updateArticleFavorite(id, isFav)
    }

    // --- UKS Follow-ups ---
    fun getFollowUpsForStudent(studentId: String): Flow<List<UksFollowUpEntity>> =
        dao.getFollowUpsForStudent(studentId)

    fun getAllFollowUps(): Flow<List<UksFollowUpEntity>> = dao.getAllFollowUps()

    suspend fun saveFollowUp(followUp: UksFollowUpEntity): Long {
        val id = dao.insertFollowUp(followUp)
        val followUpWithId = followUp.copy(id = id)
        cloudSync.syncFollowUpToCloud(followUpWithId)
        return id
    }

    // --- Gemini AI Features ---
    suspend fun analyzeFoodImage(bitmap: Bitmap?, mealType: String = "Makan Utama", studentNote: String = ""): FoodAnalysisResponse {
        return GeminiService.analyzeFoodImage(bitmap, mealType, studentNote)
    }

    suspend fun generateDailyAdvice(
        hadBreakfast: Boolean,
        ateVegetable: Boolean,
        ateFruit: Boolean,
        sugaryDrink: String,
        physicalActivityDuration: String,
        sleepDuration: String,
        bodyCondition: String
    ): String {
        return GeminiService.generateDailyAdvice(
            hadBreakfast, ateVegetable, ateFruit, sugaryDrink, physicalActivityDuration, sleepDuration, bodyCondition
        )
    }
}
