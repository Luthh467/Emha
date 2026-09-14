package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppRepository
import com.example.data.NutritionCalculator
import com.example.data.NutritionEvaluationResult
import com.example.data.model.NutritionCheckEntity
import com.example.ui.components.NutriMindDisclaimerCard
import com.example.ui.components.RiskCategoryBadge
import com.example.ui.components.nutriMindInputTextStyle
import com.example.ui.components.nutriMindTextFieldColors
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldDark
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun NutritionCheckScreen(
    repository: AppRepository,
    onFinished: () -> Unit
) {
    val currentUser by repository.currentUser.collectAsState()
    val student = currentUser ?: return
    val scope = rememberCoroutineScope()

    // Form states
    var weightInput by remember { mutableStateOf("52.0") }
    var heightInput by remember { mutableStateOf("165.0") }
    var breakfastHabit by remember { mutableStateOf("Rutin Setiap Hari") }
    var vegHabit by remember { mutableStateOf("1x Sehari") }
    var fruitHabit by remember { mutableStateOf("Beberapa Hari Sekali") }
    var fastFoodHabit by remember { mutableStateOf("1-2x Seminggu") }
    var sugaryDrinkHabit by remember { mutableStateOf("Jarang / Air Putih Saja") }
    var physicalActivity by remember { mutableStateOf("Sedang (30-60 menit)") }
    var sedentaryDuration by remember { mutableStateOf("3 - 6 Jam / Hari") }

    var evaluationResult by remember { mutableStateOf<NutritionEvaluationResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }
    var savedSuccess by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    // Live calculation for Kemenkes BMI formula
    val liveWeight = weightInput.toFloatOrNull()
    val liveHeight = heightInput.toFloatOrNull()
    val isInputValid = liveWeight != null && liveWeight in 20f..250f && liveHeight != null && liveHeight in 80f..250f

    val liveBmi by remember(liveWeight, liveHeight) {
        derivedStateOf {
            if (isInputValid && liveWeight != null && liveHeight != null) {
                NutritionCalculator.calculateBmi(liveWeight, liveHeight)
            } else null
        }
    }

    val liveClassification by remember(liveBmi) {
        derivedStateOf {
            liveBmi?.let { NutritionCalculator.getKemenkesClassification(it) }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Formulir Cek Gizi Mandiri",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = EmeraldDark
            )
        )
        Text(
            text = "Skrining antropometri berbasis Standar Indeks Massa Tubuh (IMT) Kementerian Kesehatan RI dan evaluasi pola hidup remaja.",
            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF475569)),
            modifier = Modifier.padding(top = 2.dp, bottom = 14.dp)
        )

        // Banner Standar Resmi Kemenkes RI
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = EmeraldContainer.copy(alpha = 0.5f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldDark.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.VerifiedUser,
                    contentDescription = null,
                    tint = EmeraldDark,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Standar Antropometri Resmi Kemenkes RI",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = EmeraldDark
                    )
                    Text(
                        text = "Sesuai Permenkes No. 2 Tahun 2020 & Pedoman Gizi Seimbang Kemenkes: IMT = BB (kg) / (TB (m))².",
                        fontSize = 11.sp,
                        color = Color(0xFF0F766E),
                        lineHeight = 15.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- Bagian A: Data Antropometri ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "A. Pengukuran Fisik (Antropometri)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF0F172A)
                    )
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFE2E8F0), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("Kemenkes RI", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                    }
                }

                Text(
                    text = "Siswa: ${student.nameOrInitial} (${student.gender}, ${student.age} tahun, Kelas ${student.className})",
                    fontSize = 12.sp,
                    color = Color(0xFF475569),
                    modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = weightInput,
                        onValueChange = { weightInput = it; errorMessage = null },
                        label = { Text("Berat Badan (kg)", fontWeight = FontWeight.Medium) },
                        leadingIcon = {
                            Icon(Icons.Default.MonitorWeight, contentDescription = null, tint = EmeraldDark)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("weight_input"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = nutriMindTextFieldColors(),
                        textStyle = nutriMindInputTextStyle()
                    )

                    OutlinedTextField(
                        value = heightInput,
                        onValueChange = { heightInput = it; errorMessage = null },
                        label = { Text("Tinggi Badan (cm)", fontWeight = FontWeight.Medium) },
                        leadingIcon = {
                            Icon(Icons.Default.Height, contentDescription = null, tint = EmeraldDark)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("height_input"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = nutriMindTextFieldColors(),
                        textStyle = nutriMindInputTextStyle()
                    )
                }

                // Panel Perhitungan Rumus IMT Kemenkes Real-Time
                if (isInputValid && liveWeight != null && liveHeight != null && liveBmi != null && liveClassification != null) {
                    val heightM = liveHeight / 100f
                    val heightSq = (heightM * heightM * 10000f).roundToInt() / 10000f
                    val healthyRange = NutritionCalculator.calculateHealthyWeightRange(liveHeight)
                    val idealBroca = NutritionCalculator.calculateIdealWeightBroca(liveHeight, student.gender)

                    Spacer(modifier = Modifier.height(14.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Calculate,
                                        contentDescription = null,
                                        tint = EmeraldDark,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Kalkulasi Rumus IMT Kemenkes RI",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = EmeraldDark
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .background(Color(liveClassification!!.colorHex), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = liveClassification!!.categoryName.substringBefore(" ("),
                                        fontSize = 10.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Rumus Step-by-Step
                            Text(
                                text = "Langkah Perhitungan:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF475569)
                            )
                            Text(
                                text = "1. Tinggi Badan (m) = $liveHeight cm ÷ 100 = $heightM m\n" +
                                        "2. (Tinggi Badan)² = $heightM × $heightM = $heightSq m²\n" +
                                        "3. IMT = $liveWeight kg ÷ $heightSq m² = $liveBmi kg/m²",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF0F172A),
                                lineHeight = 18.sp,
                                modifier = Modifier.padding(top = 2.dp, bottom = 6.dp)
                            )

                            HorizontalDivider(color = Color(0xFFCBD5E1), thickness = 0.8.dp)
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(text = "Rentang Normal Kemenkes:", fontSize = 10.sp, color = Color(0xFF64748B))
                                    Text(
                                        text = "${healthyRange.first} - ${healthyRange.second} kg",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(text = "BB Ideal (Broca):", fontSize = 10.sp, color = Color(0xFF64748B))
                                    Text(
                                        text = "$idealBroca kg",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldDark
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- Tabel Standar Ambang Batas IMT Kemenkes RI ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = EmeraldDark,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Klasifikasi Ambang Batas IMT Kemenkes RI",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF0F172A)
                    )
                }
                Text(
                    text = "Pedoman Gizi Seimbang Kementerian Kesehatan Republik Indonesia:",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(top = 2.dp, bottom = 10.dp)
                )

                NutritionCalculator.KEMENKES_STANDARDS.forEach { cat ->
                    val isCurrent = liveBmi != null && liveBmi!! >= cat.minBmi && liveBmi!! <= cat.maxBmi
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .background(
                                if (isCurrent) Color(cat.colorHex).copy(alpha = 0.12f) else Color.Transparent,
                                RoundedCornerShape(6.dp)
                            )
                            .border(
                                width = if (isCurrent) 1.5.dp else 0.dp,
                                color = if (isCurrent) Color(cat.colorHex) else Color.Transparent,
                                shape = RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(Color(cat.colorHex), RoundedCornerShape(2.dp))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = cat.categoryName,
                                fontSize = 12.sp,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                color = if (isCurrent) Color(cat.colorHex) else Color(0xFF1E293B)
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = cat.rangeText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCurrent) Color(cat.colorHex) else Color(0xFF475569)
                            )
                            if (isCurrent) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("📍 Anda", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color(cat.colorHex))
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- Bagian B: Pertanyaan Kebiasaan & Gaya Hidup ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "B. Kebiasaan Makan & Pola Hidup Harian",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "Pilih jawaban yang paling menggambarkan kebiasaanmu dalam seminggu terakhir.",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                )

                // 1. Kebiasaan sarapan
                SurveyRadioGroup(
                    question = "1. Kebiasaan sarapan pagi sebelum ke madrasah:",
                    options = listOf("Rutin Setiap Hari", "Kadang-kadang (2-4x)", "Jarang / Tidak Pernah"),
                    selectedOption = breakfastHabit,
                    onOptionSelected = { breakfastHabit = it }
                )

                // 2. Konsumsi sayur
                SurveyRadioGroup(
                    question = "2. Konsumsi sayur-sayuran:",
                    options = listOf("Rutin Setiap Makan", "1x Sehari", "Jarang / Tidak Setiap Hari"),
                    selectedOption = vegHabit,
                    onOptionSelected = { vegHabit = it }
                )

                // 3. Konsumsi buah
                SurveyRadioGroup(
                    question = "3. Konsumsi buah-buahan segar:",
                    options = listOf("Rutin Setiap Hari", "Beberapa Hari Sekali", "Jarang / Hampir Tidak Pernah"),
                    selectedOption = fruitHabit,
                    onOptionSelected = { fruitHabit = it }
                )

                // 4. Makanan cepat saji / gorengan
                SurveyRadioGroup(
                    question = "4. Konsumsi makanan cepat saji / gorengan kantin:",
                    options = listOf("Jarang / Tidak Pernah", "1-2x Seminggu", ">= 3x Seminggu"),
                    selectedOption = fastFoodHabit,
                    onOptionSelected = { fastFoodHabit = it }
                )

                // 5. Minuman manis kemasan / boba / teh manis
                SurveyRadioGroup(
                    question = "5. Konsumsi minuman manis kemasan / boba:",
                    options = listOf("Jarang / Air Putih Saja", "1 Gelas Sehari", ">= 2 Gelas / Minuman Kemasan Manis Tiap Hari"),
                    selectedOption = sugaryDrinkHabit,
                    onOptionSelected = { sugaryDrinkHabit = it }
                )

                // 6. Aktivitas fisik
                SurveyRadioGroup(
                    question = "6. Aktivitas fisik / olahraga harian:",
                    options = listOf("Aktif (>= 60 menit/hari)", "Sedang (30-60 menit)", "Kurang (< 30 menit)"),
                    selectedOption = physicalActivity,
                    onOptionSelected = { physicalActivity = it }
                )

                // 7. Durasi duduk / layar
                SurveyRadioGroup(
                    question = "7. Durasi duduk / menatap layar gawai di luar jam belajar:",
                    options = listOf("< 3 Jam / Hari", "3 - 6 Jam / Hari", "> 6 Jam / Hari"),
                    selectedOption = sedentaryDuration,
                    onOptionSelected = { sedentaryDuration = it }
                )
            }
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = errorMessage!!,
                color = Color(0xFFE11D48),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tombol Analisis Status Gizi
        Button(
            onClick = {
                val weight = weightInput.toFloatOrNull()
                val height = heightInput.toFloatOrNull()
                if (weight == null || weight < 20f || weight > 250f) {
                    errorMessage = "Mohon masukkan berat badan yang valid (20-250 kg)."
                    return@Button
                }
                if (height == null || height < 80f || height > 250f) {
                    errorMessage = "Mohon masukkan tinggi badan yang valid (80-250 cm)."
                    return@Button
                }

                evaluationResult = NutritionCalculator.evaluateNutrition(
                    weightKg = weight,
                    heightCm = height,
                    age = student.age,
                    gender = student.gender,
                    breakfastHabit = breakfastHabit,
                    vegHabit = vegHabit,
                    fruitHabit = fruitHabit,
                    fastFoodHabit = fastFoodHabit,
                    sugaryDrinkHabit = sugaryDrinkHabit,
                    physicalActivity = physicalActivity,
                    sedentaryDuration = sedentaryDuration
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("analyze_nutrition_button"),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldDark)
        ) {
            Text(
                text = "Analisis Status Gizi (Standar Kemenkes RI)",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color.White
            )
        }

        // --- Output Hasil Analisis ---
        if (evaluationResult != null) {
            val result = evaluationResult!!
            val classification = result.classification ?: NutritionCalculator.getKemenkesClassification(result.bmi)
            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("nutrition_result_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Hasil Evaluasi Gizi Kemenkes",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldDark
                                )
                            )
                            Text(
                                text = "Kementerian Kesehatan Republik Indonesia",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                        RiskCategoryBadge(category = result.riskCategory)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Panel Utama IMT
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Nilai IMT", fontSize = 12.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                            Text(
                                text = "${result.bmi}",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(classification.colorHex)
                            )
                            Text(
                                text = "kg/m²",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(52.dp)
                                .background(Color(0xFFCBD5E1))
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text("Klasifikasi Kemenkes", fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                            Text(
                                text = classification.categoryName,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(classification.colorHex),
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            Text(
                                text = "Rentang: ${classification.rangeText}",
                                fontSize = 11.sp,
                                color = Color(0xFF475569)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Detail Rincian Rumus
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Rincian Rumus IMT Kemenkes:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "• Rumus: IMT = BB (kg) / (TB (m))²\n" +
                                        "• Data: ${weightInput} kg / (${result.heightInMeters} m)² = ${result.bmi} kg/m²\n" +
                                        "• Rentang Berat Normal Kemenkes: ${result.healthyWeightMin} kg - ${result.healthyWeightMax} kg\n" +
                                        "• Berat Badan Ideal Broca: ${result.idealWeightKg} kg",
                                fontSize = 11.sp,
                                color = Color(0xFF334155),
                                lineHeight = 17.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Deskripsi Status Kemenkes
                    Text(
                        text = "Deskripsi Kondisi Antropometri:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF1E293B)
                    )
                    Text(
                        text = classification.statusDescription,
                        fontSize = 12.sp,
                        color = Color(0xFF334155),
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )

                    // Faktor risiko gaya hidup
                    Text(
                        text = "Faktor Kebiasaan Harian yang Perlu Diperhatikan:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF1E293B)
                    )
                    Text(
                        text = result.factorsToWatch,
                        fontSize = 12.sp,
                        color = Color(0xFF475569),
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )

                    // Rekomendasi tindakan Kemenkes
                    Text(
                        text = "Rekomendasi Tindak Lanjut Gizi Seimbang Kemenkes:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF1E293B)
                    )
                    Text(
                        text = result.generalAdvice,
                        fontSize = 12.sp,
                        color = Color(0xFF065F46),
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                    )

                    // Tombol Simpan ke Riwayat Pemantauan
                    Button(
                        onClick = {
                            isSaving = true
                            scope.launch {
                                val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                                val checkEntity = NutritionCheckEntity(
                                    studentId = student.id,
                                    studentName = student.nameOrInitial,
                                    age = student.age,
                                    gender = student.gender,
                                    weightKg = weightInput.toFloat(),
                                    heightCm = heightInput.toFloat(),
                                    bmi = result.bmi,
                                    bmiCategory = result.bmiCategory,
                                    riskCategory = result.riskCategory,
                                    breakfastHabit = breakfastHabit,
                                    vegHabit = vegHabit,
                                    fruitHabit = fruitHabit,
                                    fastFoodHabit = fastFoodHabit,
                                    sugaryDrinkHabit = sugaryDrinkHabit,
                                    physicalActivity = physicalActivity,
                                    sedentaryDuration = sedentaryDuration,
                                    factorsToWatch = result.factorsToWatch,
                                    generalAdvice = result.generalAdvice,
                                    dateString = dateStr
                                )
                                repository.saveNutritionCheck(checkEntity)
                                isSaving = false
                                savedSuccess = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("save_nutrition_check_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (savedSuccess) Color(0xFF16A34A) else EmeraldDark
                        )
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                        } else if (savedSuccess) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Berhasil Disimpan ke Riwayat Pemantauan", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Text("Simpan Data ke Riwayat Pemantauan", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        NutriMindDisclaimerCard()
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun SurveyRadioGroup(
    question: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = question,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            color = Color(0xFF1E293B)
        )
        Spacer(modifier = Modifier.height(4.dp))
        options.forEach { option ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOptionSelected(option) }
                    .padding(vertical = 3.dp)
            ) {
                RadioButton(
                    selected = selectedOption == option,
                    onClick = { onOptionSelected(option) },
                    colors = RadioButtonDefaults.colors(selectedColor = EmeraldDark)
                )
                Text(
                    text = option,
                    fontSize = 13.sp,
                    color = Color(0xFF334155),
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}
