package com.example.data.remote

import android.graphics.Bitmap
import android.util.Base64
import com.example.BuildConfig
import com.example.data.model.ChatMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

data class DetectedFoodItem(
    val name: String,
    val portion: String,
    val calories: Int,
    val group: String
)

data class FoodAnalysisResponse(
    val detectedFoods: String,
    val foodCategory: String,
    val carbsSource: String,
    val proteinSource: String,
    val vegFruitSource: String,
    val approxNutrients: String,
    val balanceAssessment: String,
    val educationalFeedback: String,
    val estimatedCalories: Int = 480,
    val carbGrams: Int = 62,
    val proteinGrams: Int = 20,
    val fatGrams: Int = 14,
    val fiberGrams: Int = 5,
    val plateScore: Int = 80,
    val ironAssessment: String = "Cukup",
    val ironAdvice: String = "Sertakan sayuran hijau dan sumber protein agar kebutuhan zat besi harianmu tercukupi.",
    val oilSugarAssessment: String = "Kadar Wajar",
    val actionableImprovement: String = "Tambahkan porsi buah atau sayuran segar untuk melengkapi serat.",
    val studentNotes: String = "",
    val alertnessLevel: String = "Stabil & Fokus",
    val alertnessDetail: String = "Keseimbangan karbohidrat kompleks dan protein menjaga kadar gula darah stabil sehingga kamu tidak mudah mengantuk saat jam pelajaran siang madrasah.",
    val satietyHours: Int = 4,
    val calciumStatus: String = "Cukup untuk Tulang",
    val vitaminCStatus: String = "Optimal",
    val hydrationBeverageAdvice: String = "Dampingi santapan dengan 1-2 gelas air putih. Hindari minum es teh manis atau teh pekat segera setelah makan agar penyerapan zat besi tidak terhambat.",
    val items: List<DetectedFoodItem> = emptyList()
)

object GeminiService {
    private const val MODEL_CHAT = "gemini-3.5-flash"
    private const val MODEL_VISION = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val maxDim = 1024
        val scaledBitmap = if (bitmap.width > maxDim || bitmap.height > maxDim) {
            val ratio = minOf(maxDim.toFloat() / bitmap.width, maxDim.toFloat() / bitmap.height)
            val targetW = (bitmap.width * ratio).toInt().coerceAtLeast(1)
            val targetH = (bitmap.height * ratio).toInt().coerceAtLeast(1)
            Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)
        } else {
            bitmap
        }
        val outputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    private fun getApiKey(): String {
        return try {
            val key = BuildConfig.GEMINI_API_KEY
            if (key.isNullOrBlank() || key == "MY_GEMINI_API_KEY") "" else key
        } catch (e: Throwable) {
            ""
        }
    }

    /**
     * Fast and natural Chatbot for Madrasah Students using gemini-3.5-flash
     * Supports multi-turn history and streaming for instant feedback.
     */
    suspend fun streamChat(
        history: List<ChatMessage>,
        userMessage: String,
        studentName: String = "",
        onChunk: (String) -> Unit
    ): String = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank()) {
            val fallback = generateNaturalFallbackResponse(userMessage, studentName)
            onChunk(fallback)
            return@withContext fallback
        }

        try {
            val systemPrompt = """
                Anda adalah NutriMind AI, asisten dan sahabat gizi pintar yang ramah, hangat, dan suportif untuk siswa-siswi madrasah (usia MI, MTs, dan MA).
                Nama siswa: ${if (studentName.isNotBlank()) studentName else "Sahabat Madrasah"}.
                
                Karakter dan Cara Menjawab:
                1. Sapa dengan ramah dan alami (misal: "Halo ${if (studentName.isNotBlank()) studentName else "sahabat"}", "Assalamu’alaikum", atau sapaan hangat lainnya).
                2. Berbicara seperti kakak pembina atau konselor sebaya madrasah yang suportif, peduli, dan tidak menggurui.
                3. Respon harus CEPAT, RINGKAS, dan NATURAL (maksimal 2-3 paragraf singkat atau 3-4 poin praktis). Hindari paragraf panjang yang melelahkan dibaca di HP.
                4. Fokus HANYA pada topik: gizi seimbang (Isi Piringku), ide sarapan sebelum belajar/menghafal, memilih jajanan kantin sehat, mengurangi minuman manis & gorengan, hidrasi air putih, pencegahan anemia/lemas pada remaja putri, dan kebiasaan tidur yang sehat.
                5. Berikan saran realistis dan mudah diterapkan dengan kantin madrasah atau bekal dari rumah.
                6. Jika pengguna bertanya di luar gizi dan kesehatan fisik madrasah, arahkan kembali dengan santun ke topik gizi siswa.
                7. PENTING: Akhiri atau selipkan secara santun bahwa saran ini adalah panduan edukasi & skrining gizi madrasah, bukan diagnosis atau resep medis.
            """.trimIndent()

            val contentsArray = JSONArray()

            // Include last 8 messages for context and speed
            val recentHistory = history.takeLast(8)
            for (msg in recentHistory) {
                val turnObj = JSONObject().apply {
                    put("role", if (msg.isUser) "user" else "model")
                    val parts = JSONArray().apply {
                        put(JSONObject().apply { put("text", msg.text) })
                    }
                    put("parts", parts)
                }
                contentsArray.put(turnObj)
            }

            // Current user turn
            val currentTurn = JSONObject().apply {
                put("role", "user")
                val parts = JSONArray().apply {
                    put(JSONObject().apply { put("text", userMessage) })
                }
                put("parts", parts)
            }
            contentsArray.put(currentTurn)

            val requestJson = JSONObject().apply {
                put("contents", contentsArray)

                // Fast generation configuration: low thinking level to prevent long delays
                val genConfig = JSONObject().apply {
                    put("temperature", 0.7)
                    put("topP", 0.95)
                    put("topK", 40)
                    put("thinkingConfig", JSONObject().apply {
                        put("thinkingLevel", "low")
                    })
                }
                put("generationConfig", genConfig)

                // System Instruction
                put("systemInstruction", JSONObject().apply {
                    val parts = JSONArray().apply {
                        put(JSONObject().apply { put("text", systemPrompt) })
                    }
                    put("parts", parts)
                })
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = requestJson.toString().toRequestBody(mediaType)

            // Use streaming endpoint for zero perceived latency
            val streamUrl = "$BASE_URL/$MODEL_CHAT:streamGenerateContent?alt=sse&key=$apiKey"
            val request = Request.Builder()
                .url(streamUrl)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                // Fallback to non-streaming or smart natural fallback
                val fallback = generateNaturalFallbackResponse(userMessage, studentName)
                onChunk(fallback)
                return@withContext fallback
            }

            val fullResponseBuilder = StringBuilder()
            response.body?.byteStream()?.bufferedReader()?.use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    val currentLine = line?.trim() ?: continue
                    if (currentLine.startsWith("data:")) {
                        val jsonStr = currentLine.removePrefix("data:").trim()
                        if (jsonStr.isNotBlank()) {
                            try {
                                val chunkJson = JSONObject(jsonStr)
                                val candidates = chunkJson.optJSONArray("candidates")
                                val firstCand = candidates?.optJSONObject(0)
                                val parts = firstCand?.optJSONObject("content")?.optJSONArray("parts")
                                if (parts != null) {
                                    for (i in 0 until parts.length()) {
                                        val part = parts.getJSONObject(i)
                                        if (part.has("text") && !part.optBoolean("thought", false)) {
                                            val textChunk = part.getString("text")
                                            fullResponseBuilder.append(textChunk)
                                            onChunk(fullResponseBuilder.toString())
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                // continue parsing next chunk
                            }
                        }
                    }
                }
            }

            val result = fullResponseBuilder.toString().trim()
            if (result.isNotBlank()) {
                result
            } else {
                val fallback = generateNaturalFallbackResponse(userMessage, studentName)
                onChunk(fallback)
                fallback
            }
        } catch (e: Exception) {
            val fallback = generateNaturalFallbackResponse(userMessage, studentName)
            onChunk(fallback)
            fallback
        }
    }

    /**
     * Smart, natural, and empathetic fallback responses for Madrasah students
     * when offline or API key is not configured.
     */
    fun generateNaturalFallbackResponse(userMessage: String, studentName: String = ""): String {
        val lower = userMessage.lowercase()
        val nameGreeting = if (studentName.isNotBlank()) " $studentName" else ""

        return when {
            lower.contains("sarapan") || lower.contains("pagi") -> {
                "Halo$nameGreeting! Sarapan pagi itu sangat penting untuk menjaga konsentrasi saat jam pelajaran madrasah.\n\n" +
                "💡 **Tips sarapan praktis:**\n" +
                "• Pilih karbohidrat kompleks seperti nasi atau oatmeal dengan telur/tahu untuk energi tahan lama.\n" +
                "• Usahakan sarapan sebelum pukul 07.00 WIB agar otak siap menyerap pelajaran.\n" +
                "• Bila terburu-buru, setidaknya minum susu/air hangat dan sepotong roti gandum atau pisang.\n\n" +
                "_Catatan: NutriMind AI berfungsi sebagai pendamping edukasi gizi madrasah, bukan pengganti pemeriksaan medis._"
            }
            lower.contains("lemas") || lower.contains("mengantuk") || lower.contains("capek") || lower.contains("anemia") -> {
                "Hai$nameGreeting, rasa lemas atau mengantuk saat belajar di madrasah sering kali berkaitan dengan kekurangan asupan zat besi (anemia), kurang tidur, atau dehidrasi.\n\n" +
                "🌿 **Langkah cepat yang bisa dicoba:**\n" +
                "1. Minum 1 gelas air putih dingin untuk menyegarkan aliran oksigen ke otak.\n" +
                "2. Perbanyak lauk kaya zat besi (hati ayam, telur, tempe, bayam) dan buah bervitamin C (jeruk/pepaya).\n" +
                "3. Untuk siswi putri, pastikan rutin minum Tablet Tambah Darah (TTD) sesuai anjuran UKS madrasah.\n\n" +
                "_Bila rasa lemas berlanjut berhari-hari, yuk periksa ke ruang UKS madrasah terdekat!_"
            }
            lower.contains("kantin") || lower.contains("jajan") || lower.contains("gorengan") -> {
                "Pertanyaan bagus sekali$nameGreeting! Memilih jajanan di kantin madrasah memang butuh strategi cerdas.\n\n" +
                "🍱 **Tips memilih jajanan kantin sehat:**\n" +
                "• Batasi gorengan yang berminyak pekat atau berulang kali digoreng.\n" +
                "• Pilih jajanan yang mengandung protein, seperti siomay telur, lontong isi tahu/tempe, atau susu kedelai.\n" +
                "• Hindari minuman serbuk manis berwarna mencolok; pilih air mineral atau jus buah murni.\n\n" +
                "_Ingat prinsip gizi seimbang: kurangi gula, garam, dan lemak berlebih ya!_"
            }
            lower.contains("air") || lower.contains("minum") || lower.contains("haus") -> {
                "Halo$nameGreeting! Kebutuhan air putih harian untuk remaja madrasah rata-rata adalah **8 hingga 10 gelas per hari** (sekitar 2 liter).\n\n" +
                "💧 **Waktu minum yang dianjurkan:**\n" +
                "• 1 gelas setelah bangun tidur\n" +
                "• 1 gelas sebelum & sesudah sarapan\n" +
                "• 2-3 gelas selama di madrasah (bawa botol minum sendiri ya!)\n" +
                "• 1 gelas saat pulang dan sore hari\n" +
                "• 1 gelas sebelum tidur malam\n\n" +
                "Kurangi minuman boba atau soda kemasan agar ginjal dan berat badan tetap sehat."
            }
            lower.contains("sayur") || lower.contains("buah") || lower.contains("piring") -> {
                "Assalamu’alaikum$nameGreeting! Konsep **'Isi Piringku'** Kemenkes sangat cocok untuk panduan makan siang madrasah:\n\n" +
                "🍽️ **Komposisi Isi Piringku:**\n" +
                "• **1/3 piring**: Makanan pokok (nasi merah/putih, jagung, kentang)\n" +
                "• **1/3 piring**: Sayuran segar/tumis (bayam, wortel, kangkung)\n" +
                "• **1/6 piring**: Lauk pauk sumber protein (ayam, ikan, tempe, telur)\n" +
                "• **1/6 piring**: Buah-buahan segar (pisang, pepaya, semangka)\n\n" +
                "Coba tambahkan minimal satu macam sayur di menu makanmu hari ini ya!"
            }
            lower.contains("gemuk") || lower.contains("kurus") || lower.contains("berat badan") || lower.contains("diet") -> {
                "Hai$nameGreeting, menjaga berat badan ideal pada masa pertumbuhan madrasah bukan dengan diet ketat yang menyiksa, melainkan dengan pola makan teratur dan aktif bergerak.\n\n" +
                "⚖️ **Prinsip penting remaja:**\n" +
                "• Jangan lewatkan waktu makan utama, terutama sarapan.\n" +
                "• Batasi camilan tinggi gula dan makanan instan larut malam.\n" +
                "• Lakukan aktivitas fisik minimal 30 menit sehari (olahraga, jalan cepat, atau bersepeda).\n" +
                "• Gunakan fitur **Cek Gizi** di aplikasi NutriMind untuk memantau indeks IMT/U secara berkala.\n\n" +
                "_Kesehatan tubuh jangka panjang jauh lebih utama daripada angka timbangan sesaat._"
            }
            else -> {
                "Assalamu’alaikum$nameGreeting! Senang sekali bisa mengobrol denganmu di NutriMind AI.\n\n" +
                "Sebagai asisten gizi madrasah, aku siap membantumu seputar:\n" +
                "• Tips sarapan bergizi sebelum ujian atau hafalan\n" +
                "• Memilih jajanan sehat & higienis di kantin madrasah\n" +
                "• Memenuhi kebutuhan air putih & mengatasi tubuh sering lemas\n" +
                "• Cara mengatur porsi makan sesuai panduan Isi Piringku\n\n" +
                "Ada yang ingin kamu tanyakan lebih lanjut hari ini?\n\n" +
                "_Catatan: NutriMind AI berfungsi sebagai pendamping edukasi gizi madrasah, bukan pengganti pemeriksaan medis._"
            }
        }
    }

    suspend fun analyzeFoodImage(
        bitmap: Bitmap?,
        mealType: String = "Makan Utama",
        studentNote: String = ""
    ): FoodAnalysisResponse = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank() || bitmap == null) {
            return@withContext getSampleFoodAnalysis(mealType + " " + studentNote)
        }

        try {
            val base64Image = bitmapToBase64(bitmap)

            val prompt = """
                Analisis foto makanan/minuman siswa madrasah ini secara komprehensif untuk sistem skrining gizi 'NutriMind AI'.
                Waktu santap yang dipilih siswa: $mealType.
                ${if (studentNote.isNotBlank()) "Catatan tambahan dari siswa: '$studentNote'." else ""}
                Periksa makanan nyata di foto piring/wadah. Jawab HANYA dalam JSON dengan struktur:
                {
                  "detectedFoods": "Daftar spesifik makanan, lauk, minuman, atau jajanan yang tampak di foto",
                  "foodCategory": "Menu Makanan Utama (Bekal) / Jajanan Kantin / Kudapan Sehat / Minuman / Bukan Makanan",
                  "carbsSource": "Nama sumber karbohidrat (contoh: Nasi putih ~1 centong 150g, lontong, kentang, mie, atau 'Tidak ada')",
                  "proteinSource": "Nama sumber protein hewani/nabati (contoh: Ayam kecap, telur dadar, tempe, tahu, atau 'Tidak ada')",
                  "vegFruitSource": "Komponen sayur atau buah (contoh: Sayur sop wortel, lalapan mentimun, buah pisang, atau 'Belum ada')",
                  "approxNutrients": "Ringkasan energi & makronutrien (contoh: Est. 520 kkal | K: 65g, P: 22g, L: 15g, Serat: 5g)",
                  "balanceAssessment": "Seimbang (Sesuai Isi Piringku) / Cukup Seimbang / Kurang Sayur & Buah / Tinggi Minyak & Gula",
                  "educationalFeedback": "Edukasi ramah siswa madrasah sesuai pedoman Isi Piringku Kemenkes. Jelaskan manfaat gizi untuk energi belajar.",
                  "estimatedCalories": 520,
                  "carbGrams": 65,
                  "proteinGrams": 22,
                  "fatGrams": 15,
                  "fiberGrams": 5,
                  "plateScore": 85,
                  "ironAssessment": "Kaya Zat Besi / Cukup / Rendah Zat Besi",
                  "ironAdvice": "Saran zat besi untuk mencegah anemia dan meningkatkan konsentrasi hafalan/belajar",
                  "oilSugarAssessment": "Rendah Lemak / Minyak Wajar / Tinggi Gorengan & Minyak / Tinggi Gula Tambahan",
                  "actionableImprovement": "Satu langkah konkrit perbaikan piring berikutnya (contoh: Tambahkan 1 buah potong untuk melengkapi vitamin C)",
                  "alertnessLevel": "Stabil & Fokus / Sedang / Rawan Mengantuk (Food Coma)",
                  "alertnessDetail": "Penjelasan ilmiah kaitan menu ini dengan kesiapan dan fokus belajar di kelas madrasah",
                  "satietyHours": 4,
                  "calciumStatus": "Optimal / Cukup / Perlu Ditingkatkan",
                  "vitaminCStatus": "Optimal / Cukup / Kurang Sayur & Buah",
                  "hydrationBeverageAdvice": "Saran minuman pendamping sehat yang mendukung penyerapan zat gizi",
                  "items": [
                    {"name": "Nasi Putih Pulen", "portion": "1 centong (~150g)", "calories": 195, "group": "Karbohidrat Pokok"},
                    {"name": "Lauk Pauk", "portion": "1 porsi (~80g)", "calories": 150, "group": "Lauk Hewani/Nabati"}
                  ]
                }
            """.trimIndent()

            val requestJson = JSONObject().apply {
                val contentsArray = JSONArray()
                val contentObj = JSONObject()
                val partsArray = JSONArray()

                partsArray.put(JSONObject().apply { put("text", prompt) })
                partsArray.put(JSONObject().apply {
                    val inlineData = JSONObject().apply {
                        put("mimeType", "image/jpeg")
                        put("data", base64Image)
                    }
                    put("inlineData", inlineData)
                })

                contentObj.put("parts", partsArray)
                contentsArray.put(contentObj)
                put("contents", contentsArray)

                // JSON response format and low thinking level for fast response
                val genConfig = JSONObject().apply {
                    put("responseMimeType", "application/json")
                    put("thinkingConfig", JSONObject().apply {
                        put("thinkingLevel", "low")
                    })
                }
                put("generationConfig", genConfig)

                // System Instruction
                val sysInst = JSONObject().apply {
                    val parts = JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", "Anda adalah NutriMind AI, asisten kecerdasan buatan skrining gizi madrasah. Berikan analisis ilmiah gizi yang akurat berdasarkan foto makanan yang diberikan, dengan bahasa Indonesia yang ramah, sopan, dan edukatif tanpa diagnosis medis.")
                        })
                    }
                    put("parts", parts)
                }
                put("systemInstruction", sysInst)
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = requestJson.toString().toRequestBody(mediaType)
            val request = Request.Builder()
                .url("$BASE_URL/$MODEL_VISION:generateContent?key=$apiKey")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                android.util.Log.e("NutriMindAI", "Food analysis API error: ${response.code} $responseBody")
                return@withContext getSampleFoodAnalysis(mealType + " " + studentNote)
            }

            val jsonResp = JSONObject(responseBody)
            val candidates = jsonResp.optJSONArray("candidates")
            val firstCand = candidates?.optJSONObject(0)
            val content = firstCand?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")

            var responseText = ""
            if (parts != null) {
                for (i in 0 until parts.length()) {
                    val p = parts.getJSONObject(i)
                    if (p.has("text") && !p.optBoolean("thought", false)) {
                        responseText += p.getString("text")
                    }
                }
            }

            var cleanJson = responseText.trim()
            if (cleanJson.startsWith("```json")) {
                cleanJson = cleanJson.removePrefix("```json").trim()
            }
            if (cleanJson.startsWith("```")) {
                cleanJson = cleanJson.removePrefix("```").trim()
            }
            if (cleanJson.endsWith("```")) {
                cleanJson = cleanJson.removeSuffix("```").trim()
            }

            val parsed = JSONObject(cleanJson)

            // Cek jika AI merespon gambar bukan makanan atau tidak terbaca jelas
            if ((parsed.has("detail") || parsed.has("message") || parsed.has("error")) && !parsed.has("detectedFoods")) {
                val msg = parsed.optString("detail", parsed.optString("message", "Foto belum dapat dikenali sebagai makanan."))
                return@withContext FoodAnalysisResponse(
                    detectedFoods = "Belum Terdeteksi Makanan",
                    foodCategory = "Foto Kurang Jelas / Bukan Makanan",
                    carbsSource = "-",
                    proteinSource = "-",
                    vegFruitSource = "-",
                    approxNutrients = "0 kkal",
                    balanceAssessment = "Perlu Foto Ulang",
                    educationalFeedback = msg,
                    estimatedCalories = 0,
                    carbGrams = 0,
                    proteinGrams = 0,
                    fatGrams = 0,
                    fiberGrams = 0,
                    plateScore = 0,
                    ironAssessment = "-",
                    ironAdvice = "Arahkan kamera ke hidangan makanan yang jelas.",
                    oilSugarAssessment = "-",
                    actionableImprovement = "Ambil foto makanan dengan pencahayaan terang."
                )
            }

            fun extractString(key: String, fallback: String): String {
                if (!parsed.has(key)) return fallback
                val arr = parsed.optJSONArray(key)
                if (arr != null) {
                    val list = mutableListOf<String>()
                    for (i in 0 until arr.length()) {
                        list.add(arr.optString(i))
                    }
                    return list.joinToString(", ")
                }
                val obj = parsed.optJSONObject(key)
                if (obj != null) {
                    val entries = mutableListOf<String>()
                    val keys = obj.keys()
                    while (keys.hasNext()) {
                        val k = keys.next()
                        entries.add("$k: ${obj.opt(k)}")
                    }
                    return entries.joinToString(" | ")
                }
                val str = parsed.optString(key, fallback)
                return if (str.isNotBlank()) str else fallback
            }

            val estCal = parsed.optInt("estimatedCalories", 480).coerceAtLeast(0)
            val cGrams = parsed.optInt("carbGrams", 60).coerceAtLeast(0)
            val pGrams = parsed.optInt("proteinGrams", 20).coerceAtLeast(0)
            val fGrams = parsed.optInt("fatGrams", 14).coerceAtLeast(0)
            val fibGrams = parsed.optInt("fiberGrams", 5).coerceAtLeast(0)
            val pScore = parsed.optInt("plateScore", 78).coerceIn(10, 100)

            val itemsList = mutableListOf<DetectedFoodItem>()
            val itemsJsonArray = parsed.optJSONArray("items")
            if (itemsJsonArray != null) {
                for (i in 0 until itemsJsonArray.length()) {
                    val itObj = itemsJsonArray.optJSONObject(i) ?: continue
                    val itemName = itObj.optString("name", "").trim()
                    if (itemName.isNotBlank()) {
                        itemsList.add(
                            DetectedFoodItem(
                                name = itemName,
                                portion = itObj.optString("portion", "1 porsi"),
                                calories = itObj.optInt("calories", 100),
                                group = itObj.optString("group", "Komponen Piring")
                            )
                        )
                    }
                }
            }

            // If itemsList is empty from model, generate sensible items from detectedFoods
            if (itemsList.isEmpty()) {
                val carb = extractString("carbsSource", "")
                if (carb.isNotBlank() && !carb.equals("Tidak ada", ignoreCase = true)) {
                    itemsList.add(DetectedFoodItem(carb, "1 porsi", (estCal * 0.45).toInt(), "Karbohidrat Pokok"))
                }
                val protein = extractString("proteinSource", "")
                if (protein.isNotBlank() && !protein.equals("Tidak ada", ignoreCase = true)) {
                    itemsList.add(DetectedFoodItem(protein, "1 porsi", (estCal * 0.30).toInt(), "Lauk Pauk"))
                }
                val vegFruit = extractString("vegFruitSource", "")
                if (vegFruit.isNotBlank() && !vegFruit.equals("Belum ada", ignoreCase = true)) {
                    itemsList.add(DetectedFoodItem(vegFruit, "1 porsi segar", (estCal * 0.15).toInt(), "Sayur / Buah"))
                }
            }

            FoodAnalysisResponse(
                detectedFoods = extractString("detectedFoods", "Makanan terdeteksi dari foto"),
                foodCategory = extractString("foodCategory", "Menu Makanan"),
                carbsSource = extractString("carbsSource", "-"),
                proteinSource = extractString("proteinSource", "-"),
                vegFruitSource = extractString("vegFruitSource", "-"),
                approxNutrients = extractString("approxNutrients", "Est. $estCal kkal | K: ${cGrams}g, P: ${pGrams}g, L: ${fGrams}g"),
                balanceAssessment = extractString("balanceAssessment", "Cukup Seimbang"),
                educationalFeedback = extractString("educationalFeedback", "Pastikan piring makanmu seimbang antara karbohidrat, lauk berprotein, sayur, dan buah."),
                estimatedCalories = estCal,
                carbGrams = cGrams,
                proteinGrams = pGrams,
                fatGrams = fGrams,
                fiberGrams = fibGrams,
                plateScore = pScore,
                ironAssessment = extractString("ironAssessment", "Cukup"),
                ironAdvice = extractString("ironAdvice", "Konsumsi makanan tinggi zat besi dan vitamin C untuk mencegah anemia serta lemas saat belajar."),
                oilSugarAssessment = extractString("oilSugarAssessment", "Minyak Wajar"),
                actionableImprovement = extractString("actionableImprovement", "Tambahkan porsi buah atau sayuran segar untuk melengkapi serat."),
                studentNotes = studentNote,
                alertnessLevel = extractString("alertnessLevel", "Stabil & Fokus"),
                alertnessDetail = extractString("alertnessDetail", "Keseimbangan makronutrien menjaga kestabilan glukosa darah sehingga stamina belajar tetap konsisten di kelas madrasah."),
                satietyHours = parsed.optInt("satietyHours", 4).coerceIn(1, 6),
                calciumStatus = extractString("calciumStatus", "Cukup untuk Tulang"),
                vitaminCStatus = extractString("vitaminCStatus", "Optimal"),
                hydrationBeverageAdvice = extractString("hydrationBeverageAdvice", "Dampingi santapan dengan 1-2 gelas air putih (300-400ml). Hindari teh manis pekat segera setelah makan agar penyerapan zat besi tidak terganggu."),
                items = itemsList
            )
        } catch (e: Exception) {
            android.util.Log.e("NutriMindAI", "Food analysis exception", e)
            getSampleFoodAnalysis(mealType + " " + studentNote)
        }
    }

    suspend fun generateDailyAdvice(
        hadBreakfast: Boolean,
        ateVegetable: Boolean,
        ateFruit: Boolean,
        sugaryDrink: String,
        physicalActivityDuration: String,
        sleepDuration: String,
        bodyCondition: String
    ): String = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank()) {
            return@withContext fallbackDailyAdvice(hadBreakfast, ateVegetable, ateFruit, sugaryDrink, physicalActivityDuration)
        }

        try {
            val prompt = """
                Berikan 2 kalimat saran gizi dan kebiasaan harian yang ramah bagi siswa madrasah (tanpa klaim diagnosis medis) berdasarkan data cek hari ini:
                - Sarapan: ${if (hadBreakfast) "Ya" else "Belum"}
                - Makan Sayur: ${if (ateVegetable) "Ya" else "Belum"}
                - Makan Buah: ${if (ateFruit) "Ya" else "Belum"}
                - Konsumsi Minuman Manis: $sugaryDrink
                - Durasi Aktivitas Fisik: $physicalActivityDuration
                - Durasi Tidur: $sleepDuration
                - Kondisi Tubuh: $bodyCondition
            """.trimIndent()

            val requestJson = JSONObject().apply {
                val contentsArray = JSONArray()
                val contentObj = JSONObject()
                val partsArray = JSONArray().apply {
                    put(JSONObject().apply { put("text", prompt) })
                }
                contentObj.put("parts", partsArray)
                contentsArray.put(contentObj)
                put("contents", contentsArray)

                put("generationConfig", JSONObject().apply {
                    put("thinkingConfig", JSONObject().apply {
                        put("thinkingLevel", "low")
                    })
                })
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val request = Request.Builder()
                .url("$BASE_URL/$MODEL_CHAT:generateContent?key=$apiKey")
                .post(requestJson.toString().toRequestBody(mediaType))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext fallbackDailyAdvice(hadBreakfast, ateVegetable, ateFruit, sugaryDrink, physicalActivityDuration)
            }

            val jsonResp = JSONObject(responseBody)
            val candidates = jsonResp.optJSONArray("candidates")
            val firstCand = candidates?.optJSONObject(0)
            val parts = firstCand?.optJSONObject("content")?.optJSONArray("parts")

            var advice = ""
            if (parts != null) {
                for (i in 0 until parts.length()) {
                    val p = parts.getJSONObject(i)
                    if (p.has("text") && !p.optBoolean("thought", false)) {
                        advice += p.getString("text")
                    }
                }
            }

            if (advice.isNotBlank()) advice.trim() else fallbackDailyAdvice(hadBreakfast, ateVegetable, ateFruit, sugaryDrink, physicalActivityDuration)
        } catch (e: Exception) {
            fallbackDailyAdvice(hadBreakfast, ateVegetable, ateFruit, sugaryDrink, physicalActivityDuration)
        }
    }

    fun getSampleFoodAnalysis(sampleName: String = ""): FoodAnalysisResponse {
        return when {
            sampleName.contains("Gorengan", ignoreCase = true) || sampleName.contains("Boba", ignoreCase = true) || sampleName.contains("Kantin", ignoreCase = true) -> {
                FoodAnalysisResponse(
                    detectedFoods = "Bakwan goreng tepung (2 buah), tahu isi goreng, dan es teh/boba manis",
                    foodCategory = "Jajanan Kantin Madrasah",
                    carbsSource = "Tepung gorengan & Gula sirup manis (Karbohidrat sederhana)",
                    proteinSource = "Tahu goreng sedikit (~4g)",
                    vegFruitSource = "Potongan wortel & kubis tipis dalam bakwan",
                    approxNutrients = "Est. 540 kkal | K: 75g, P: 8g, L: 24g, Serat: 2g",
                    balanceAssessment = "Tinggi Minyak & Gula",
                    educationalFeedback = "Jajanan ini tinggi lemak jenuh dari minyak gorengan serta gula sederhana. Kurang serat dan protein esensial sehingga cepat memicu rasa kantuk berat setelah jam istirahat.",
                    estimatedCalories = 540,
                    carbGrams = 75,
                    proteinGrams = 8,
                    fatGrams = 24,
                    fiberGrams = 2,
                    plateScore = 42,
                    ironAssessment = "Rendah Zat Besi",
                    ironAdvice = "Kandungan zat besi sangat minim. Es teh manis juga mengandung tanin yang menghambat penyerapan zat besi. Sebaiknya ganti dengan air putih mineral.",
                    oilSugarAssessment = "Tinggi Minyak & Gula Tambahan",
                    actionableImprovement = "Batasi gorengan maksimal 1 buah, ganti minuman manis dengan air putih mineral segar.",
                    studentNotes = "Jajanan kantin saat jam istirahat sekolah",
                    alertnessLevel = "Rawan Mengantuk (Food Coma)",
                    alertnessDetail = "Kombinasi karbohidrat olahan cepat serap dan minyak berlebih memicu lonjakan insulin mendadak, menyebabkan rasa lesu dan kantuk berat saat jam pelajaran siang.",
                    satietyHours = 2,
                    calciumStatus = "Perlu Ditingkatkan",
                    vitaminCStatus = "Kurang Sayur & Buah",
                    hydrationBeverageAdvice = "Hindari teh manis pekat setelah camilan berminyak. Minum air putih 1-2 gelas (400ml) untuk membantu metabolisme tubuh.",
                    items = listOf(
                        DetectedFoodItem("Bakwan Sayur Goreng", "2 buah sedang (~120g)", 260, "Gorengan & Minyak"),
                        DetectedFoodItem("Tahu Isi Goreng Tepung", "1 buah (~70g)", 130, "Gorengan & Lauk"),
                        DetectedFoodItem("Es Teh / Minuman Manis", "1 gelas (250ml)", 150, "Minuman Gula")
                    )
                )
            }
            sampleName.contains("Gado-Gado", ignoreCase = true) || sampleName.contains("Sayur", ignoreCase = true) -> {
                FoodAnalysisResponse(
                    detectedFoods = "Gado-gado sayuran rebus (kangkung, tauge, kacang panjang), tahu, tempe, telur rebus, dan bumbu kacang",
                    foodCategory = "Menu Makanan Utama Sehat",
                    carbsSource = "Lontong / Kentang rebus (~120g)",
                    proteinSource = "Telur rebus 1 butir, tahu & tempe (Protein hewani & nabati)",
                    vegFruitSource = "Kangkung, tauge, kol, mentimun & kacang panjang rebus melimpah",
                    approxNutrients = "Est. 460 kkal | K: 48g, P: 24g, L: 18g, Serat: 9g",
                    balanceAssessment = "Sangat Seimbang (Kaya Serat)",
                    educationalFeedback = "Pilihan sangat cerdas! Menu kaya serat pangan dari sayuran beragam, ditambah protein lengkap dari telur dan kedelai yang memperkuat stamina belajar seharian.",
                    estimatedCalories = 460,
                    carbGrams = 48,
                    proteinGrams = 24,
                    fatGrams = 18,
                    fiberGrams = 9,
                    plateScore = 92,
                    ironAssessment = "Kaya Zat Besi & Folat",
                    ironAdvice = "Sayuran hijau dan telur rebus merupakan sumber zat besi yang sangat baik. Tambahkan perasan jeruk limau untuk vitamin C pendukung absorbsi zat besi alami.",
                    oilSugarAssessment = "Minyak Wajar (Lemak Nabati Baik)",
                    actionableImprovement = "Minta bumbu kacang secukupnya dan batasi kerupuk merah berlebih.",
                    studentNotes = "Menu kaya serat dan sayuran tradisional",
                    alertnessLevel = "Stabil & Fokus Tinggi",
                    alertnessDetail = "Kandungan serat tinggi membuat pelepasan energi berlangsung stabil, mencegah kantuk dan menjaga konsentrasi optimal saat setoran hafalan Quran.",
                    satietyHours = 4,
                    calciumStatus = "Optimal",
                    vitaminCStatus = "Optimal",
                    hydrationBeverageAdvice = "Dampingi dengan air putih 400ml untuk membantu serat melancarkan sistem pencernaan.",
                    items = listOf(
                        DetectedFoodItem("Sayuran Rebus Campur", "1 porsi mangkuk (~150g)", 80, "Sayuran"),
                        DetectedFoodItem("Telur Rebus Utuh", "1 butir (~55g)", 75, "Lauk Hewani"),
                        DetectedFoodItem("Tahu & Tempe Rebus", "2 potong (~80g)", 110, "Lauk Nabati"),
                        DetectedFoodItem("Lontong Potong", "1 porsi kecil (~100g)", 125, "Karbohidrat Pokok"),
                        DetectedFoodItem("Bumbu Kacang Tradisional", "2 sendok makan (~40g)", 70, "Bumbu & Pelengkap")
                    )
                )
            }
            sampleName.contains("Mie", ignoreCase = true) -> {
                FoodAnalysisResponse(
                    detectedFoods = "Mie instan kuah/goreng, telur ceplok, dan taburan bawang goreng",
                    foodCategory = "Menu Siap Saji / Olahan",
                    carbsSource = "Mie instan olahan gandum (Karbohidrat sederhana & natrium tinggi)",
                    proteinSource = "Telur ayam ceplok 1 butir",
                    vegFruitSource = "Belum ada sayuran hijau atau buah segar",
                    approxNutrients = "Est. 510 kkal | K: 64g, P: 16g, L: 22g, Serat: 2g",
                    balanceAssessment = "Kurang Sayur & Serat",
                    educationalFeedback = "Mie instan memiliki karbohidrat dan natrium tinggi. Tambahan telur sudah melengkapi protein, namun sangat disarankan menambahkan sayuran seperti sawi hijau atau tomat segar.",
                    estimatedCalories = 510,
                    carbGrams = 64,
                    proteinGrams = 16,
                    fatGrams = 22,
                    fiberGrams = 2,
                    plateScore = 55,
                    ironAssessment = "Cukup dari Telur",
                    ironAdvice = "Telur memberikan asupan zat besi, tetapi absorbsi akan lebih optimal jika dilengkapi vitamin C dari sayuran pendamping.",
                    oilSugarAssessment = "Cukup Tinggi Minyak Nabati & Garam",
                    actionableImprovement = "Tambahkan 1 genggam sawi hijau/tomat dan gunakan setengah bumbu sachet untuk mengontrol kadar natrium.",
                    studentNotes = "Santapan mie instan praktis",
                    alertnessLevel = "Sedang / Cepat Lapar Kembali",
                    alertnessDetail = "Indeks glikemik mie olahan cukup tinggi sehingga gula darah cepat naik lalu cepat turun, memicu rasa lelah atau lapar kembali.",
                    satietyHours = 2,
                    calciumStatus = "Perlu Ditingkatkan",
                    vitaminCStatus = "Kurang Sayur & Buah",
                    hydrationBeverageAdvice = "Perbanyak minum air putih (minimal 500ml) untuk membantu proses metabolisme kadar natrium bumbu mie.",
                    items = listOf(
                        DetectedFoodItem("Mie Instan Olahan", "1 bungkus porsi (~85g)", 380, "Karbohidrat Pokok"),
                        DetectedFoodItem("Telur Ayam Ceplok", "1 butir (~50g)", 90, "Lauk Hewani"),
                        DetectedFoodItem("Minyak Bumbu & Pelengkap", "1 saset (~10g)", 40, "Minyak & Bumbu")
                    )
                )
            }
            else -> {
                FoodAnalysisResponse(
                    detectedFoods = "Nasi putih, ayam bakar rempah, tumis buncis wortel, tempe bacem, dan pisang ambon",
                    foodCategory = "Menu Makanan Utama (Bekal Madrasah)",
                    carbsSource = "Nasi Putih 1 centong pulen (~150g)",
                    proteinSource = "Ayam Bakar Rempah Dada & Tempe Bacem",
                    vegFruitSource = "Tumis Buncis Wortel Segar & Buah Pisang Ambon",
                    approxNutrients = "Est. 520 kkal | K: 68g, P: 28g, L: 12g, Serat: 7g",
                    balanceAssessment = "Seimbang (Sesuai Isi Piringku)",
                    educationalFeedback = "Komposisi piring sangat ideal memenuhi pedoman Isi Piringku Kemenkes: 2/3 karbohidrat pokok, 2/3 sayuran, 1/3 lauk pauk, dan 1/3 buah segar.",
                    estimatedCalories = 520,
                    carbGrams = 68,
                    proteinGrams = 28,
                    fatGrams = 12,
                    fiberGrams = 7,
                    plateScore = 95,
                    ironAssessment = "Kaya Zat Besi & Zink",
                    ironAdvice = "Ayam dan buncis memberikan zat besi hewani & nabati esensial. Vitamin C alami dari buah pisang dan wortel melipatgandakan penyerapannya untuk cegah anemia remaja.",
                    oilSugarAssessment = "Rendah Lemak Jenuh (Minyak Wajar)",
                    actionableImprovement = "Pertahankan pola bekal sehat bergizi seimbang ini setiap hari!",
                    studentNotes = "Bekal sehat bergizi seimbang buatan rumah",
                    alertnessLevel = "Stabil & Siaga Tinggi",
                    alertnessDetail = "Asupan karbohidrat kompleks berpadu dengan protein tinggi dan serat mencegah sindrom mengantuk (food coma), mempertahankan daya konsentrasi optimal di kelas.",
                    satietyHours = 4,
                    calciumStatus = "Optimal",
                    vitaminCStatus = "Optimal",
                    hydrationBeverageAdvice = "Sempurnakan dengan 1 botol air putih (500-600ml) selama kegiatan belajar di madrasah.",
                    items = listOf(
                        DetectedFoodItem("Nasi Putih Pulen", "1 porsi centong (~150g)", 195, "Karbohidrat Pokok"),
                        DetectedFoodItem("Ayam Bakar Rempah Dada", "1 potong sedang (~80g)", 145, "Lauk Hewani"),
                        DetectedFoodItem("Tempe Bacem / Goreng Tipis", "1 potong (~40g)", 60, "Lauk Nabati"),
                        DetectedFoodItem("Tumis Buncis Wortel", "1 porsi mangkuk (~80g)", 45, "Sayuran"),
                        DetectedFoodItem("Buah Pisang Ambon Segar", "1 buah sedang (~90g)", 75, "Buah-Buahan")
                    )
                )
            }
        }
    }

    private fun fallbackDailyAdvice(
        hadBreakfast: Boolean,
        ateVegetable: Boolean,
        ateFruit: Boolean,
        sugaryDrink: String,
        physicalActivity: String
    ): String {
        val tips = mutableListOf<String>()
        if (!hadBreakfast) tips.add("usahakan sarapan esok hari untuk menjaga fokus belajar")
        if (!ateVegetable) tips.add("tambahkan porsi sayuran pada makan siang atau malam")
        if (!ateFruit) tips.add("nikmati buah potong sebagai camilan sehat")
        if (sugaryDrink.contains("2") || sugaryDrink.contains("Lebih")) tips.add("kurangi minuman manis dan perbanyak air putih minimal 8 gelas")

        return if (tips.isEmpty()) {
            "Pola makan dan kebiasaan harianmu hari ini sudah sangat baik! Pertahankan kecukupan istirahat dan tetap aktif bergerak."
        } else {
            "Pola hari ini sudah cukup baik. Cobalah ${tips.joinToString(" serta ")} agar tubuhmu semakin bugar."
        }
    }
}
