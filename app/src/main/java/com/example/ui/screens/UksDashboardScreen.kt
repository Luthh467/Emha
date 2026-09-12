package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppRepository
import com.example.data.model.NutritionCheckEntity
import com.example.data.model.UksFollowUpEntity
import com.example.data.model.UserEntity
import com.example.ui.components.NutriMindDisclaimerCard
import com.example.ui.components.RiskCategoryBadge
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.CoralContainer
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.GreenContainer
import com.example.ui.theme.HealthAmber
import com.example.ui.theme.HealthCoral
import com.example.ui.theme.SafeGreen
import com.example.util.PdfReportGenerator
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UksDashboardScreen(
    repository: AppRepository,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val currentUser by repository.currentUser.collectAsState()
    val scope = rememberCoroutineScope()

    val allStudents by repository.getAllStudents().collectAsState(initial = emptyList())
    val allNutritionChecks by repository.getAllNutritionChecks().collectAsState(initial = emptyList())
    val allFollowUps by repository.getAllFollowUps().collectAsState(initial = emptyList())

    // Filters
    var classFilter by remember { mutableStateOf("Semua") }
    var genderFilter by remember { mutableStateOf("Semua") }
    var riskFilter by remember { mutableStateOf("Semua") }

    // Dialog & Sheets
    var selectedStudentForDetail by remember { mutableStateOf<UserEntity?>(null) }
    var showAddFollowUpDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    val detailSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Match each student with their latest nutrition check
    val studentWithLatestChecks = allStudents.map { student ->
        val latestCheck = allNutritionChecks.filter { it.studentId == student.id }.maxByOrNull { it.timestamp }
        Pair(student, latestCheck)
    }

    // Filtered list
    val filteredStudents = studentWithLatestChecks.filter { (student, check) ->
        val matchesClass = classFilter == "Semua" || student.className.startsWith(classFilter, ignoreCase = true)
        val matchesGender = genderFilter == "Semua" || student.gender.equals(genderFilter, ignoreCase = true)
        val matchesRisk = riskFilter == "Semua" || (check != null && check.riskCategory.contains(riskFilter, ignoreCase = true))
        matchesClass && matchesGender && matchesRisk
    }

    // Stats
    val totalStudents = allStudents.size
    val lowRiskCount = studentWithLatestChecks.count { it.second?.riskCategory?.contains("Rendah", ignoreCase = true) == true }
    val attentionCount = studentWithLatestChecks.count { it.second?.riskCategory?.contains("Perhatian", ignoreCase = true) == true }
    val highRiskCount = studentWithLatestChecks.count { it.second?.riskCategory?.contains("Tinggi", ignoreCase = true) == true }
    val unmonitoredCount = studentWithLatestChecks.count { it.second == null }

    var selectedDiagramTab by remember { mutableStateOf("Risiko") }

    // BMI Stats
    val bmiUnderweight = studentWithLatestChecks.count { it.second?.bmiCategory?.contains("Kurang", ignoreCase = true) == true }
    val bmiNormal = studentWithLatestChecks.count { it.second?.bmiCategory?.contains("Normal", ignoreCase = true) == true || it.second?.bmiCategory?.contains("Baik", ignoreCase = true) == true }
    val bmiOverweight = studentWithLatestChecks.count { it.second?.bmiCategory?.contains("Lebih", ignoreCase = true) == true }
    val bmiObese = studentWithLatestChecks.count { it.second?.bmiCategory?.contains("Obesitas", ignoreCase = true) == true }

    // Habit Stats
    val screenedStudentsCount = studentWithLatestChecks.count { it.second != null }.coerceAtLeast(1)
    val breakfastCount = studentWithLatestChecks.count { it.second?.breakfastHabit?.contains("Rutin", ignoreCase = true) == true || it.second?.breakfastHabit?.contains("Setiap", ignoreCase = true) == true }
    val vegFruitCount = studentWithLatestChecks.count { it.second?.vegHabit?.contains("Setiap", ignoreCase = true) == true || it.second?.vegHabit?.contains("Sering", ignoreCase = true) == true }
    val activeCount = studentWithLatestChecks.count { it.second?.physicalActivity?.contains("Aktif", ignoreCase = true) == true || it.second?.physicalActivity?.contains("Rutin", ignoreCase = true) == true }
    val lowSweetDrinkCount = studentWithLatestChecks.count { it.second?.sugaryDrinkHabit?.contains("Jarang", ignoreCase = true) == true || it.second?.sugaryDrinkHabit?.contains("Tidak", ignoreCase = true) == true }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp)
    ) {
        // --- Header Petugas UKS ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f, fill = false)) {
                Text(
                    text = "Dashboard Petugas UKS",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = EmeraldDark
                    )
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFF10B981), androidx.compose.foundation.shape.CircleShape)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Cloud Sync Aktif • Semua Perangkat Siswa Terhubung",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF047857),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        )
                    )
                }
                Text(
                    text = "${currentUser?.nameOrInitial ?: "Petugas UKS"} • ${currentUser?.madrasahName ?: "Madrasah"}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF1E293B),
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = {
                        PdfReportGenerator.generateAndOpenReport(
                            context = context,
                            madrasahName = currentUser?.madrasahName ?: "Madrasah",
                            officerName = currentUser?.nameOrInitial ?: "Petugas UKS",
                            students = allStudents,
                            nutritionChecks = allNutritionChecks,
                            lowRiskCount = lowRiskCount,
                            attentionCount = attentionCount,
                            highRiskCount = highRiskCount,
                            unmonitoredCount = unmonitoredCount
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmeraldDark,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("export_data_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = "Unduh PDF Laporan",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Unduh PDF",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(onClick = onLogout, modifier = Modifier.testTag("uks_logout_button")) {
                    Icon(Icons.Default.ExitToApp, contentDescription = "Keluar", tint = Color(0xFFE11D48))
                }
            }
        }

        // --- Banner Kerahasiaan Data (Section 10.7) ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7))
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color(0xFF92400E),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "“Data ini bersifat rahasia dan hanya digunakan untuk pemantauan serta tindak lanjut kesehatan siswa.”",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF78350F),
                    lineHeight = 15.sp
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // --- Kartu Statistik & Distribusi Risiko (Diagrams) ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.PieChart,
                                    contentDescription = null,
                                    tint = EmeraldDark,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Diagram & Statistik UKS",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF0F172A)
                                )
                            }
                            Text(
                                text = "Total: $totalStudents Siswa",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = EmeraldDark
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Diagram Selector Chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                Pair("Risiko", "🍩 Risiko Gizi"),
                                Pair("IMT", "📊 Status IMT"),
                                Pair("Kelas", "🏫 Per Kelas"),
                                Pair("Kebiasaan", "🥗 Pola Hidup")
                            ).forEach { (tabKey, tabLabel) ->
                                FilterChip(
                                    selected = selectedDiagramTab == tabKey,
                                    onClick = { selectedDiagramTab = tabKey },
                                    label = { Text(tabLabel, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = EmeraldDark,
                                        selectedLabelColor = Color.White,
                                        containerColor = Color(0xFFF1F5F9),
                                        labelColor = Color(0xFF334155)
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Render Selected Diagram
                        when (selectedDiagramTab) {
                            "Risiko" -> {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // Donut Chart
                                    UksDonutChart(
                                        lowCount = lowRiskCount,
                                        attentionCount = attentionCount,
                                        highCount = highRiskCount,
                                        unmonitoredCount = unmonitoredCount
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Proportional Bar
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(12.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFFE2E8F0))
                                    ) {
                                        val totalWeight = totalStudents.coerceAtLeast(1).toFloat()
                                        if (lowRiskCount > 0) {
                                            Box(modifier = Modifier.weight(lowRiskCount / totalWeight).fillMaxSize().background(SafeGreen))
                                        }
                                        if (attentionCount > 0) {
                                            Box(modifier = Modifier.weight(attentionCount / totalWeight).fillMaxSize().background(HealthAmber))
                                        }
                                        if (highRiskCount > 0) {
                                            Box(modifier = Modifier.weight(highRiskCount / totalWeight).fillMaxSize().background(HealthCoral))
                                        }
                                        if (unmonitoredCount > 0) {
                                            Box(modifier = Modifier.weight(unmonitoredCount / totalWeight).fillMaxSize().background(Color(0xFF94A3B8)))
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Badges
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        UksStatBadge("Rendah", lowRiskCount, Color(0xFF065F46), GreenContainer)
                                        UksStatBadge("Perhatian", attentionCount, Color(0xFF92400E), AmberContainer)
                                        UksStatBadge("Tinggi", highRiskCount, Color(0xFF9F1239), CoralContainer)
                                        UksStatBadge("Belum Cek", unmonitoredCount, Color(0xFF0F172A), Color(0xFFF1F5F9))
                                    }
                                }
                            }
                            "IMT" -> {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = "Distribusi Indeks Massa Tubuh (IMT / Usia Siswa)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF475569)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    UksBmiBarChart(
                                        underweight = bmiUnderweight,
                                        normal = bmiNormal,
                                        overweight = bmiOverweight,
                                        obese = bmiObese
                                    )
                                }
                            }
                            "Kelas" -> {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = "Proporsi Risiko Kesehatan Berdasarkan Jenjang Kelas",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF475569)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    UksClassBreakdownChart(
                                        studentsWithChecks = studentWithLatestChecks
                                    )
                                }
                            }
                            "Kebiasaan" -> {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = "Capaian Perilaku Hidup Sehat Siswa (Aksi Bergizi)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF475569)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    UksHabitProgressChart(
                                        breakfastCount = breakfastCount,
                                        vegFruitCount = vegFruitCount,
                                        activeCount = activeCount,
                                        sweetDrinkSafeCount = lowSweetDrinkCount,
                                        totalScreened = screenedStudentsCount
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // --- Filter Controls ---
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Kelas Filter
                    listOf("Semua", "X", "XI", "XII").forEach { cls ->
                        FilterChip(
                            selected = classFilter == cls,
                            onClick = { classFilter = cls },
                            label = { Text(if (cls == "Semua") "Semua Kelas" else "Kelas $cls") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldDark,
                                selectedLabelColor = Color.White,
                                labelColor = Color(0xFF0F172A)
                            )
                        )
                    }

                    // Risiko Filter
                    listOf("Semua", "Rendah", "Perhatian", "Tinggi").forEach { r ->
                        FilterChip(
                            selected = riskFilter == r,
                            onClick = { riskFilter = r },
                            label = { Text(if (r == "Semua") "Semua Risiko" else "Risiko $r") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldDark,
                                selectedLabelColor = Color.White,
                                labelColor = Color(0xFF0F172A)
                            )
                        )
                    }
                }
            }

            // --- Daftar Siswa Terpantau ---
            item {
                Text(
                    text = "Daftar Siswa Terpantau (${filteredStudents.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF0F172A)
                )
            }

            if (filteredStudents.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.HealthAndSafety,
                                contentDescription = null,
                                tint = EmeraldDark,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Menunggu Siswa Mengisi Cek Gizi",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF0F172A)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Setiap siswa yang mengisi cek gizi mandiri dari HP masing-masing akan otomatis langsung muncul di sini secara real-time melalui Cloud Sync NutriMind.",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                lineHeight = 17.sp
                            )
                        }
                    }
                }
            }

            items(filteredStudents, key = { it.first.id }) { (student, check) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedStudentForDetail = student }
                        .testTag("student_roster_${student.studentIdNumber}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = student.nameOrInitial,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF0F172A)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "(${student.studentIdNumber})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF334155)
                                )
                            }
                            Text(
                                text = "Kelas ${student.className} • ${student.gender}, ${student.age} th",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF1E293B)
                            )
                            if (check != null) {
                                Text(
                                    text = "IMT: ${check.bmi} (${check.bmiCategory}) • Cek: ${check.dateString}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF0F172A),
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            } else {
                                Text(
                                    text = "⚠️ Belum pernah melakukan cek gizi",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF92400E),
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            if (check != null) {
                                RiskCategoryBadge(category = check.riskCategory)
                            } else {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFFEF3C7), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("Belum Cek", fontSize = 10.sp, color = Color(0xFF92400E), fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Detail", fontSize = 11.sp, color = EmeraldDark, fontWeight = FontWeight.Bold)
                                Icon(Icons.Default.ArrowForward, contentDescription = null, tint = EmeraldDark, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(10.dp))
                NutriMindDisclaimerCard()
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    // Modal Bottom Sheet Detail Siswa & Tindak Lanjut UKS
    if (selectedStudentForDetail != null) {
        val student = selectedStudentForDetail!!
        val studentChecks = allNutritionChecks.filter { it.studentId == student.id }.sortedByDescending { it.timestamp }
        val studentFollowUps = allFollowUps.filter { it.studentId == student.id }

        ModalBottomSheet(
            onDismissRequest = { selectedStudentForDetail = null },
            sheetState = detailSheetState,
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = student.nameOrInitial,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = EmeraldDark
                            )
                        )
                        Text(
                            text = "NIS: ${student.studentIdNumber} • Kelas ${student.className} • ${student.gender}, ${student.age} th",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1E293B)
                        )
                    }

                    Button(
                        onClick = { showAddFollowUpDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldDark,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NoteAdd,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Catat Tindak Lanjut",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Riwayat Cek Gizi Siswa:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFF0F172A)
                )

                if (studentChecks.isEmpty()) {
                    Text(
                        text = "Siswa ini belum pernah mengisi data antropometri.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF334155),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    studentChecks.forEach { check ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(check.dateString, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF0F172A))
                                    RiskCategoryBadge(category = check.riskCategory)
                                }
                                Text(
                                    text = "BB: ${check.weightKg} kg, TB: ${check.heightCm.toInt()} cm, IMT: ${check.bmi} (${check.bmiCategory})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF0F172A),
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                                Text(
                                    text = "Saran: ${check.generalAdvice}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF064E3B),
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Catatan Tindak Lanjut Petugas UKS:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFF0F172A)
                )

                if (studentFollowUps.isEmpty()) {
                    Text(
                        text = "Belum ada catatan tindak lanjut untuk siswa ini.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF334155),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    studentFollowUps.forEach { fu ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = fu.actionType,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = EmeraldDark
                                    )
                                    Text(fu.dateString, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF334155))
                                }
                                Text(
                                    text = fu.note,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = Color(0xFF0F172A),
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                                Text(
                                    text = "Petugas: ${fu.officerName}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF1E293B),
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                NutriMindDisclaimerCard()
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    // Dialog Catat Tindak Lanjut UKS (Section 10.4: Edukatif, Bukan Medis)
    if (showAddFollowUpDialog && selectedStudentForDetail != null) {
        val student = selectedStudentForDetail!!
        var actionType by remember { mutableStateOf("Konsultasi Kebiasaan Sarapan") }
        var followUpNote by remember { mutableStateOf("") }
        var noteError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showAddFollowUpDialog = false },
            title = { Text("Catat Tindak Lanjut UKS", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A)) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "Siswa: ${student.nameOrInitial} (${student.studentIdNumber})",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "Catatan bersifat edukatif dan pembinaan kebiasaan sehat, bukan diagnosis medis.",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF78350F),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFEF3C7), RoundedCornerShape(6.dp))
                            .padding(6.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Jenis Tindak Lanjut:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF0F172A))
                    listOf(
                        "Konsultasi Kebiasaan Sarapan",
                        "Edukasi Bekal Sehat 'Isi Piringku'",
                        "Edukasi Pembatasan Minuman Manis & Gorengan",
                        "Rujukan Konsultasi Tenaga Kesehatan Puskesmas"
                    ).forEach { opt ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { actionType = opt }
                                .padding(vertical = 2.dp)
                        ) {
                            RadioButton(
                                selected = actionType == opt,
                                onClick = { actionType = opt },
                                colors = RadioButtonDefaults.colors(selectedColor = EmeraldDark)
                            )
                            Text(opt, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF0F172A))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = followUpNote,
                        onValueChange = { followUpNote = it; noteError = null },
                        label = { Text("Catatan Edukasi & Rekomendasi Petugas") },
                        placeholder = { Text("Contoh: Disarankan membawa bekal buah potong dan tidak melewatkan sarapan sebelum ujian.") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            cursorColor = Color.Black,
                            focusedBorderColor = EmeraldDark
                        )
                    )

                    if (noteError != null) {
                        Text(noteError!!, color = Color(0xFFE11D48), fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (followUpNote.isBlank()) {
                            noteError = "Catatan tindak lanjut tidak boleh kosong."
                            return@Button
                        }
                        scope.launch {
                            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                            val fu = UksFollowUpEntity(
                                studentId = student.id,
                                officerName = currentUser?.nameOrInitial ?: "Petugas UKS",
                                note = followUpNote.trim(),
                                actionType = actionType,
                                dateString = todayStr
                            )
                            repository.saveFollowUp(fu)
                            showAddFollowUpDialog = false
                            Toast.makeText(context, "Tindak lanjut berhasil dicatat", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmeraldDark,
                        contentColor = Color.White
                    )
                ) {
                    Text("Simpan Catatan", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddFollowUpDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // Dialog Ekspor Data Sederhana (Section 10.6)
    if (showExportDialog) {
        val exportText = remember(allStudents, allNutritionChecks) {
            val sb = StringBuilder()
            sb.append("LAPORAN PEMANTAUAN RISIKO GIZI SISWA MADRASAH\n")
            sb.append("Aplikasi: NutriMind AI\n")
            sb.append("Tanggal Ekspor: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}\n")
            sb.append("Total Siswa Terdaftar: ${allStudents.size}\n")
            sb.append("Risiko Rendah: $lowRiskCount | Perlu Perhatian: $attentionCount | Risiko Tinggi: $highRiskCount | Belum Cek: $unmonitoredCount\n")
            sb.append("--------------------------------------------------------------------------------\n")
            sb.append("NIS,Nama/Inisial,Kelas,Gender,Umur,IMT,Status Gizi,Kategori Risiko,Terakhir Cek\n")
            allStudents.forEach { st ->
                val check = allNutritionChecks.filter { it.studentId == st.id }.maxByOrNull { it.timestamp }
                sb.append("${st.studentIdNumber},${st.nameOrInitial},${st.className},${st.gender},${st.age},")
                if (check != null) {
                    sb.append("${check.bmi},${check.bmiCategory},${check.riskCategory},${check.dateString}\n")
                } else {
                    sb.append("-,-,Belum Pernah Cek,-\n")
                }
            }
            sb.append("--------------------------------------------------------------------------------\n")
            sb.append("Catatan: NutriMind AI merupakan sistem skrining dan pemantauan awal, bukan alat diagnosis medis.\n")
            sb.toString()
        }

        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Ekspor Laporan Pemantauan UKS", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A)) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "Unduh laporan berformat PDF resmi atau salin teks/CSV untuk pelaporan madrasah:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            PdfReportGenerator.generateAndOpenReport(
                                context = context,
                                madrasahName = currentUser?.madrasahName ?: "Madrasah",
                                officerName = currentUser?.nameOrInitial ?: "Petugas UKS",
                                students = allStudents,
                                nutritionChecks = allNutritionChecks,
                                lowRiskCount = lowRiskCount,
                                attentionCount = attentionCount,
                                highRiskCount = highRiskCount,
                                unmonitoredCount = unmonitoredCount
                            )
                            showExportDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldDark,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Unduh Sebagai File PDF", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = exportText,
                            fontSize = 10.sp,
                            color = Color(0xFF0F172A),
                            lineHeight = 14.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(exportText))
                        Toast.makeText(context, "Laporan berhasil disalin ke clipboard!", Toast.LENGTH_SHORT).show()
                        showExportDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155))
                ) {
                    Text("Salin ke Clipboard")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Tutup", color = Color(0xFF0F172A), fontWeight = FontWeight.Medium)
                }
            }
        )
    }
}

@Composable
fun UksStatBadge(label: String, count: Int, textColor: Color, bgColor: Color) {
    Box(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "$count", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textColor)
            Text(text = label, fontSize = 10.sp, color = textColor)
        }
    }
}

@Composable
fun UksDonutChart(
    lowCount: Int,
    attentionCount: Int,
    highCount: Int,
    unmonitoredCount: Int,
    modifier: Modifier = Modifier
) {
    val total = (lowCount + attentionCount + highCount + unmonitoredCount).coerceAtLeast(1).toFloat()
    val lowAngle = (lowCount / total) * 360f
    val attentionAngle = (attentionCount / total) * 360f
    val highAngle = (highCount / total) * 360f
    val unmonitoredAngle = (unmonitoredCount / total) * 360f

    val strokeWidth = 24.dp

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(150.dp)) {
            val strokePx = strokeWidth.toPx()
            val arcSize = Size(size.width - strokePx, size.height - strokePx)
            val topLeft = Offset(strokePx / 2, strokePx / 2)

            var startAngle = -90f

            if (lowCount > 0) {
                drawArc(
                    color = SafeGreen,
                    startAngle = startAngle,
                    sweepAngle = lowAngle,
                    useCenter = false,
                    style = Stroke(width = strokePx, cap = StrokeCap.Butt),
                    size = arcSize,
                    topLeft = topLeft
                )
                startAngle += lowAngle
            }
            if (attentionCount > 0) {
                drawArc(
                    color = HealthAmber,
                    startAngle = startAngle,
                    sweepAngle = attentionAngle,
                    useCenter = false,
                    style = Stroke(width = strokePx, cap = StrokeCap.Butt),
                    size = arcSize,
                    topLeft = topLeft
                )
                startAngle += attentionAngle
            }
            if (highCount > 0) {
                drawArc(
                    color = HealthCoral,
                    startAngle = startAngle,
                    sweepAngle = highAngle,
                    useCenter = false,
                    style = Stroke(width = strokePx, cap = StrokeCap.Butt),
                    size = arcSize,
                    topLeft = topLeft
                )
                startAngle += highAngle
            }
            if (unmonitoredCount > 0) {
                drawArc(
                    color = Color(0xFF94A3B8),
                    startAngle = startAngle,
                    sweepAngle = unmonitoredAngle,
                    useCenter = false,
                    style = Stroke(width = strokePx, cap = StrokeCap.Butt),
                    size = arcSize,
                    topLeft = topLeft
                )
            }
        }

        // Teks di tengah Donut
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val screenedCount = lowCount + attentionCount + highCount
            val pct = if (total.toInt() > 0) (screenedCount * 100 / total.toInt()) else 0
            Text(
                text = "$pct%",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = EmeraldDark
            )
            Text(
                text = "Terskrining",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF64748B)
            )
        }
    }
}

@Composable
fun UksBmiBarChart(
    underweight: Int,
    normal: Int,
    overweight: Int,
    obese: Int,
    modifier: Modifier = Modifier
) {
    val maxCount = maxOf(underweight, normal, overweight, obese, 1).toFloat()
    val totalChecked = (underweight + normal + overweight + obese).coerceAtLeast(1)

    val categories = listOf(
        Triple("Gizi Kurang", underweight, Color(0xFF3B82F6)),
        Triple("Gizi Baik", normal, SafeGreen),
        Triple("Gizi Lebih", overweight, HealthAmber),
        Triple("Obesitas", obese, HealthCoral)
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            categories.forEach { (label, count, color) ->
                val barHeight = ((count / maxCount) * 100).coerceAtLeast(8f).dp
                val pct = (count * 100) / totalChecked

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(68.dp)
                ) {
                    Text(
                        text = "$count",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                    Text(
                        text = "($pct%)",
                        fontSize = 10.sp,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(36.dp)
                            .height(barHeight)
                            .background(color, RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF334155),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun UksClassBreakdownChart(
    studentsWithChecks: List<Pair<UserEntity, NutritionCheckEntity?>>,
    modifier: Modifier = Modifier
) {
    val classes = studentsWithChecks.map { it.first.className.trim() }.distinct().sorted()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (classes.isEmpty()) {
            Text("Belum ada data kelas siswa.", fontSize = 12.sp, color = Color(0xFF64748B))
        } else {
            classes.forEach { cls ->
                val inClass = studentsWithChecks.filter { it.first.className.trim() == cls }
                val totalInClass = inClass.size.coerceAtLeast(1)
                val low = inClass.count { it.second?.riskCategory?.contains("Rendah", ignoreCase = true) == true }
                val att = inClass.count { it.second?.riskCategory?.contains("Perhatian", ignoreCase = true) == true }
                val high = inClass.count { it.second?.riskCategory?.contains("Tinggi", ignoreCase = true) == true }
                val unmonitored = inClass.count { it.second == null }

                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (cls.startsWith("Kelas", ignoreCase = true)) cls else "Kelas $cls",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "$totalInClass Siswa (${inClass.size - unmonitored} dicek)",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(Color(0xFFE2E8F0))
                    ) {
                        val weightTotal = totalInClass.toFloat()
                        if (low > 0) {
                            Box(
                                modifier = Modifier
                                    .weight(low / weightTotal)
                                    .fillMaxSize()
                                    .background(SafeGreen)
                            )
                        }
                        if (att > 0) {
                            Box(
                                modifier = Modifier
                                    .weight(att / weightTotal)
                                    .fillMaxSize()
                                    .background(HealthAmber)
                            )
                        }
                        if (high > 0) {
                            Box(
                                modifier = Modifier
                                    .weight(high / weightTotal)
                                    .fillMaxSize()
                                    .background(HealthCoral)
                            )
                        }
                        if (unmonitored > 0) {
                            Box(
                                modifier = Modifier
                                    .weight(unmonitored / weightTotal)
                                    .fillMaxSize()
                                    .background(Color(0xFFCBD5E1))
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UksHabitProgressChart(
    breakfastCount: Int,
    vegFruitCount: Int,
    activeCount: Int,
    sweetDrinkSafeCount: Int,
    totalScreened: Int,
    modifier: Modifier = Modifier
) {
    val total = totalScreened.coerceAtLeast(1)

    val habits = listOf(
        Triple("Sarapan Pagi Sehat", breakfastCount, Color(0xFF2563EB)),
        Triple("Makan Sayur & Buah Harian", vegFruitCount, Color(0xFF16A34A)),
        Triple("Aktivitas Fisik Rutin", activeCount, Color(0xFFEA580C)),
        Triple("Membatasi Minuman Manis", sweetDrinkSafeCount, Color(0xFF7C3AED))
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        habits.forEach { (name, count, color) ->
            val pct = (count * 100) / total
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = name,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1E293B)
                    )
                    Text(
                        text = "$pct% ($count/$total siswa)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                LinearProgressIndicator(
                    progress = { (pct / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = color,
                    trackColor = Color(0xFFF1F5F9)
                )
            }
        }
    }
}
