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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppRepository
import com.example.data.NutritionCalculator
import com.example.data.model.DailyCheckEntity
import com.example.ui.components.NutriMindDisclaimerCard
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldDark
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun DailyCheckScreen(
    repository: AppRepository,
    onFinished: () -> Unit
) {
    val currentUser by repository.currentUser.collectAsState()
    val student = currentUser ?: return
    val scope = rememberCoroutineScope()

    val todayDateString = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }
    val existingCheck by repository.getDailyCheckForDate(student.id, todayDateString).collectAsState(initial = null)
    val latestNutritionCheck by repository.getLatestNutritionCheck(student.id).collectAsState(initial = null)

    // Antropometri State untuk Rumus BMT & TDEE
    var currentWeightKg by remember { mutableFloatStateOf(52f) }
    var currentHeightCm by remember { mutableFloatStateOf(162f) }

    // Kuisioner Singkat States
    var hadBreakfast by remember { mutableStateOf(true) }
    var mealCount by remember { mutableIntStateOf(3) }
    var ateVegetable by remember { mutableStateOf(true) }
    var ateFruit by remember { mutableStateOf(false) }
    var waterGlassCount by remember { mutableStateOf("7–8 gelas (Optimal)") }
    var sugaryDrinkCount by remember { mutableStateOf("1 kali") }
    var didPhysicalActivity by remember { mutableStateOf(true) }
    var physicalActivityDuration by remember { mutableStateOf("30–60 menit") }
    var sedentaryDuration by remember { mutableStateOf("< 3 jam") }
    var sleepDuration by remember { mutableStateOf("6–8 jam") }
    var bodyCondition by remember { mutableStateOf("Cukup Bugar") }

    var isSaving by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    var savedAdvice by remember { mutableStateOf<String?>(null) }
    var showSuccessMessage by remember { mutableStateOf(false) }

    // Inisialisasi data dari antropometri terakhir
    LaunchedEffect(latestNutritionCheck) {
        latestNutritionCheck?.let {
            if (it.weightKg > 0f) currentWeightKg = it.weightKg
            if (it.heightCm > 0f) currentHeightCm = it.heightCm
        }
    }

    LaunchedEffect(existingCheck) {
        existingCheck?.let {
            hadBreakfast = it.hadBreakfast
            mealCount = it.mealCount
            ateVegetable = it.ateVegetable
            ateFruit = it.ateFruit
            sugaryDrinkCount = it.sugaryDrinkCount
            didPhysicalActivity = it.didPhysicalActivity
            physicalActivityDuration = it.physicalActivityDuration
            sedentaryDuration = it.sedentaryDuration
            sleepDuration = it.sleepDuration
            bodyCondition = it.bodyCondition
            savedAdvice = it.summaryAdvice
        }
    }

    // Kalkulasi Rumus BMT & TDEE secara reaktif
    val bmrTdeeResult = remember(currentWeightKg, currentHeightCm, student.age, student.gender, physicalActivityDuration) {
        val bmi = NutritionCalculator.calculateBmi(currentWeightKg, currentHeightCm)
        val category = NutritionCalculator.getKemenkesClassification(bmi).categoryName
        NutritionCalculator.calculateBmrTdee(
            weightKg = currentWeightKg,
            heightCm = currentHeightCm,
            age = student.age,
            gender = student.gender,
            physicalActivityDuration = physicalActivityDuration,
            bmiCategory = category
        )
    }

    val isReadOnly = existingCheck != null && !isEditing
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // --- Header Section ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Cek Kesehatan & Kalori Harian",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = EmeraldDark
                    )
                )
                Text(
                    text = "Perhitungan BMT, TDEE & Kuisioner Skrining Sehat Kemenkes",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B)),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            if (existingCheck != null) {
                Box(
                    modifier = Modifier
                        .background(EmeraldContainer, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isEditing) Icons.Default.Edit else Icons.Default.Lock,
                            contentDescription = null,
                            tint = EmeraldDark,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isEditing) "Mode Edit" else "Tersimpan",
                            color = EmeraldDark,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- Kartu 1: Kalkulator BMT & TDEE Sesuai Rumus Kemenkes / Mifflin-St Jeor ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("bmr_tdee_calculator_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFFFFF7ED), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = Color(0xFFEA580C),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Kalkulasi Energi BMT & TDEE",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "Rumus Standar Kemenkes (Mifflin-St Jeor)",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .background(EmeraldContainer, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${student.gender} • ${student.age} Thn",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldDark
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Pengatur Cepat Berat Badan & Tinggi Badan
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Penyetel Berat Badan
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Berat Badan", fontSize = 11.sp, color = Color(0xFF64748B))
                            Text(
                                text = "${(currentWeightKg * 10f).roundToInt() / 10f} kg",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            if (!isReadOnly) {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { if (currentWeightKg > 30f) currentWeightKg -= 0.5f },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = "Kurang", modifier = Modifier.size(14.dp), tint = Color(0xFF475569))
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(
                                        onClick = { if (currentWeightKg < 180f) currentWeightKg += 0.5f },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Tambah", modifier = Modifier.size(14.dp), tint = EmeraldDark)
                                    }
                                }
                            }
                        }
                    }

                    // Penyetel Tinggi Badan
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Tinggi Badan", fontSize = 11.sp, color = Color(0xFF64748B))
                            Text(
                                text = "${currentHeightCm.toInt()} cm",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            if (!isReadOnly) {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { if (currentHeightCm > 100f) currentHeightCm -= 1f },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = "Kurang", modifier = Modifier.size(14.dp), tint = Color(0xFF475569))
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(
                                        onClick = { if (currentHeightCm < 220f) currentHeightCm += 1f },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Tambah", modifier = Modifier.size(14.dp), tint = EmeraldDark)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Kotak Tampilan Nilai BMT dan TDEE
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Box Nilai BMT (Basal Metabolic Tariif / Rate)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFFF7ED))
                            .border(1.dp, Color(0xFFFED7AA), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = Color(0xFFEA580C), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Nilai BMT (BMR)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF9A3412)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${bmrTdeeResult.bmr.toInt()} kkal",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFC2410C)
                            )
                            Text(
                                text = "Energi organ vital saat istirahat",
                                fontSize = 10.sp,
                                color = Color(0xFF9A3412)
                            )
                        }
                    }

                    // Box Nilai TDEE (Total Daily Energy Expenditure)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF0FDF4))
                            .border(1.dp, Color(0xFFBBF7D0), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Nilai TDEE Harian",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF166534)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${bmrTdeeResult.tdee.toInt()} kkal",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = EmeraldDark
                            )
                            Text(
                                text = "Total kalori dengan aktivitas",
                                fontSize = 10.sp,
                                color = Color(0xFF166534)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Keterangan Rumus & Faktor Pengali
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Column {
                        Text(
                            text = "📐 Rumus Mifflin-St Jeor: BMT × Faktor Aktivitas (${bmrTdeeResult.activityFactor}x)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF334155)
                        )
                        Text(
                            text = "Tingkat aktivitas saat ini: ${bmrTdeeResult.activityLevelName}",
                            fontSize = 10.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Pembagian Kalori Santap Harian (Isi Piringku Kemenkes)
                Text(
                    text = "Rekomendasi Alokasi Kalori Makan Harian (Kemenkes):",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MealCalorieItem(
                        modifier = Modifier.weight(1f),
                        title = "Sarapan",
                        percentage = "25%",
                        calories = bmrTdeeResult.breakfastCalories,
                        bgColor = Color(0xFFFFFBEB),
                        textColor = Color(0xFFB45309)
                    )
                    MealCalorieItem(
                        modifier = Modifier.weight(1f),
                        title = "M. Siang",
                        percentage = "35%",
                        calories = bmrTdeeResult.lunchCalories,
                        bgColor = Color(0xFFEFF6FF),
                        textColor = Color(0xFF1D4ED8)
                    )
                    MealCalorieItem(
                        modifier = Modifier.weight(1f),
                        title = "M. Malam",
                        percentage = "25%",
                        calories = bmrTdeeResult.dinnerCalories,
                        bgColor = Color(0xFFFDF4FF),
                        textColor = Color(0xFF86198F)
                    )
                    MealCalorieItem(
                        modifier = Modifier.weight(1f),
                        title = "Selingan",
                        percentage = "15%",
                        calories = bmrTdeeResult.snackCalories,
                        bgColor = Color(0xFFF0FDF4),
                        textColor = Color(0xFF15803D)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "💡 ${bmrTdeeResult.targetAdvice}",
                    fontSize = 11.sp,
                    color = Color(0xFF475569),
                    lineHeight = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Existing Daily Summary & Recommendation (Jika sudah ada data tersimpan)
        if (savedAdvice != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("daily_advice_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = EmeraldDark,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ringkasan & Rekomendasi Kesehatan Hari Ini",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = EmeraldDark
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = savedAdvice!!,
                        fontSize = 12.sp,
                        color = Color(0xFF1E293B),
                        lineHeight = 18.sp
                    )

                    if (isReadOnly) {
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = { isEditing = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp), tint = EmeraldDark)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Perbarui Kuisioner Hari Ini", color = EmeraldDark, fontSize = 13.sp)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // --- Kartu 2: Kuisioner Singkat Kesehatan Harian (Bagian 1: Pola Makan & Hidrasi) ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(EmeraldContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Restaurant, contentDescription = null, tint = EmeraldDark, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Kuisioner Singkat: Pola Makan & Hidrasi",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF0F172A)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Q1: Sarapan
                DailyYesNoQuestion(
                    question = "1. Apakah hari ini kamu sarapan pagi?",
                    isYes = hadBreakfast,
                    enabled = !isReadOnly,
                    onAnswer = { hadBreakfast = it }
                )

                HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))

                // Q2: Frekuensi Makan
                DailyChoiceQuestion(
                    question = "2. Berapa kali kamu makan hari ini?",
                    options = listOf("1 kali", "2 kali", "3 kali", "Lebih dari 3"),
                    selectedOption = when (mealCount) {
                        1 -> "1 kali"
                        2 -> "2 kali"
                        3 -> "3 kali"
                        else -> "Lebih dari 3"
                    },
                    enabled = !isReadOnly,
                    onOptionSelected = {
                        mealCount = when (it) {
                            "1 kali" -> 1
                            "2 kali" -> 2
                            "3 kali" -> 3
                            else -> 4
                        }
                    }
                )

                HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))

                // Q3: Sayur
                DailyYesNoQuestion(
                    question = "3. Apakah hari ini kamu mengonsumsi sayur?",
                    isYes = ateVegetable,
                    enabled = !isReadOnly,
                    onAnswer = { ateVegetable = it }
                )

                HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))

                // Q4: Buah
                DailyYesNoQuestion(
                    question = "4. Apakah hari ini kamu mengonsumsi buah segar?",
                    isYes = ateFruit,
                    enabled = !isReadOnly,
                    onAnswer = { ateFruit = it }
                )

                HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))

                // Q5: Asupan Air Putih (Hidrasi)
                DailyChoiceQuestion(
                    question = "5. Asupan air putih (hidrasi) hari ini?",
                    options = listOf("< 4 gelas", "4–6 gelas", "7–8 gelas (Optimal)", "> 8 gelas"),
                    selectedOption = waterGlassCount,
                    enabled = !isReadOnly,
                    onOptionSelected = { waterGlassCount = it }
                )

                HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))

                // Q6: Minuman Manis
                DailyChoiceQuestion(
                    question = "6. Konsumsi minuman manis / es sirup / boba hari ini?",
                    options = listOf("Tidak ada", "1 kali", "2 kali", "Lebih dari 2 kali"),
                    selectedOption = sugaryDrinkCount,
                    enabled = !isReadOnly,
                    onOptionSelected = { sugaryDrinkCount = it }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- Kartu 3: Kuisioner Singkat Kesehatan Harian (Bagian 2: Aktivitas Fisik, Istirahat & Gejala) ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(Color(0xFFEFF6FF), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Kuisioner Singkat: Aktivitas & Kebugaran Fisik",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF0F172A)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Q7: Aktivitas Fisik
                DailyYesNoQuestion(
                    question = "7. Apakah hari ini kamu melakukan aktivitas fisik / olahraga?",
                    isYes = didPhysicalActivity,
                    enabled = !isReadOnly,
                    onAnswer = { didPhysicalActivity = it }
                )

                HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))

                // Q8: Durasi Aktivitas Fisik (Mempengaruhi TDEE!)
                DailyChoiceQuestion(
                    question = "8. Durasi aktivitas fisik (Mempengaruhi pengali TDEE):",
                    options = listOf("< 30 menit", "30–60 menit", "> 60 menit"),
                    selectedOption = physicalActivityDuration,
                    enabled = !isReadOnly,
                    onOptionSelected = { physicalActivityDuration = it }
                )

                HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))

                // Q9: Waktu Duduk Santai
                DailyChoiceQuestion(
                    question = "9. Lama waktu duduk santai / menatap gawai di luar belajar:",
                    options = listOf("< 3 jam", "3–6 jam", "> 6 jam"),
                    selectedOption = sedentaryDuration,
                    enabled = !isReadOnly,
                    onOptionSelected = { sedentaryDuration = it }
                )

                HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))

                // Q10: Durasi Tidur
                DailyChoiceQuestion(
                    question = "10. Berapa jam durasi tidurmu semalam?",
                    options = listOf("< 6 jam", "6–8 jam", "> 8 jam"),
                    selectedOption = sleepDuration,
                    enabled = !isReadOnly,
                    onOptionSelected = { sleepDuration = it }
                )

                HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))

                // Q11: Kondisi Tubuh & Gejala Fisik
                DailyChoiceQuestion(
                    question = "11. Bagaimana kondisi fisik & kebugaran tubuhmu hari ini?",
                    options = listOf("Sangat Segar", "Cukup Bugar", "Lemas / Lelah", "Kurang Enak Badan"),
                    selectedOption = bodyCondition,
                    enabled = !isReadOnly,
                    onOptionSelected = { bodyCondition = it }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tombol Simpan / Perbarui Data
        if (!isReadOnly) {
            Button(
                onClick = {
                    isSaving = true
                    scope.launch {
                        val aiAdvice = repository.generateDailyAdvice(
                            hadBreakfast = hadBreakfast,
                            ateVegetable = ateVegetable,
                            ateFruit = ateFruit,
                            sugaryDrink = sugaryDrinkCount,
                            physicalActivityDuration = physicalActivityDuration,
                            sleepDuration = sleepDuration,
                            bodyCondition = bodyCondition
                        )

                        val completeAdvice = buildString {
                            append("📊 Perhitungan Energi Kemenkes Hari Ini:\n")
                            append("• BMT (Kebutuhan Basal): ${bmrTdeeResult.bmr.toInt()} kkal\n")
                            append("• TDEE (Kebutuhan Total): ${bmrTdeeResult.tdee.toInt()} kkal/hari (Aktivitas: ${bmrTdeeResult.activityLevelName})\n")
                            append("• Anjuran Santap: Sarapan ~${bmrTdeeResult.breakfastCalories} kkal, Siang ~${bmrTdeeResult.lunchCalories} kkal, Malam ~${bmrTdeeResult.dinnerCalories} kkal, Selingan ~${bmrTdeeResult.snackCalories} kkal.\n")
                            append("• Hidrasi Air Putih: $waterGlassCount\n\n")
                            append("🤖 Rekomendasi NutriMind AI:\n")
                            append(aiAdvice)
                        }

                        val entity = DailyCheckEntity(
                            id = existingCheck?.id ?: 0,
                            studentId = student.id,
                            dateString = todayDateString,
                            hadBreakfast = hadBreakfast,
                            mealCount = mealCount,
                            ateVegetable = ateVegetable,
                            ateFruit = ateFruit,
                            sugaryDrinkCount = sugaryDrinkCount,
                            didPhysicalActivity = didPhysicalActivity,
                            physicalActivityDuration = physicalActivityDuration,
                            sedentaryDuration = sedentaryDuration,
                            sleepDuration = sleepDuration,
                            bodyCondition = bodyCondition,
                            summaryAdvice = completeAdvice,
                            isLocked = true
                        )

                        if (existingCheck != null) {
                            repository.updateDailyCheck(entity)
                        } else {
                            repository.saveDailyCheck(entity)
                        }

                        savedAdvice = completeAdvice
                        isSaving = false
                        isEditing = false
                        showSuccessMessage = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_daily_check_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldDark)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.5.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Menghitung BMT, TDEE & Menganalisis AI...", color = Color.White, fontSize = 13.sp)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (existingCheck != null) "Perbarui & Kunci Data Hari Ini" else "Simpan & Hitung BMT / TDEE Hari Ini",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        NutriMindDisclaimerCard()
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun MealCalorieItem(
    modifier: Modifier = Modifier,
    title: String,
    percentage: String,
    calories: Int,
    bgColor: Color,
    textColor: Color
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = textColor)
            Text(percentage, fontSize = 9.sp, color = Color(0xFF64748B))
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "$calories",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = textColor
            )
            Text("kkal", fontSize = 8.sp, color = textColor)
        }
    }
}

@Composable
fun DailyYesNoQuestion(
    question: String,
    isYes: Boolean,
    enabled: Boolean,
    onAnswer: (Boolean) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(
            text = question,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            color = Color(0xFF1E293B)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable(enabled = enabled) { onAnswer(true) }
                    .padding(vertical = 2.dp)
            ) {
                RadioButton(
                    selected = isYes,
                    onClick = { if (enabled) onAnswer(true) },
                    enabled = enabled,
                    colors = RadioButtonDefaults.colors(selectedColor = EmeraldDark)
                )
                Text("Ya", fontSize = 13.sp, color = Color(0xFF334155))
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable(enabled = enabled) { onAnswer(false) }
                    .padding(vertical = 2.dp)
            ) {
                RadioButton(
                    selected = !isYes,
                    onClick = { if (enabled) onAnswer(false) },
                    enabled = enabled,
                    colors = RadioButtonDefaults.colors(selectedColor = EmeraldDark)
                )
                Text("Tidak", fontSize = 13.sp, color = Color(0xFF334155))
            }
        }
    }
}

@Composable
fun DailyChoiceQuestion(
    question: String,
    options: List<String>,
    selectedOption: String,
    enabled: Boolean,
    onOptionSelected: (String) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(
            text = question,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            color = Color(0xFF1E293B)
        )
        Spacer(modifier = Modifier.height(4.dp))
        options.chunked(2).forEach { rowOptions ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowOptions.forEach { option ->
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(enabled = enabled) { onOptionSelected(option) }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedOption == option,
                            onClick = { if (enabled) onOptionSelected(option) },
                            enabled = enabled,
                            colors = RadioButtonDefaults.colors(selectedColor = EmeraldDark)
                        )
                        Text(
                            text = option,
                            fontSize = 12.sp,
                            color = Color(0xFF334155)
                        )
                    }
                }
            }
        }
    }
}
