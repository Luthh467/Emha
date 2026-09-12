package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppRepository
import com.example.data.model.DailyCheckEntity
import com.example.data.model.FoodScanEntity
import com.example.data.model.NutritionCheckEntity
import com.example.ui.components.NutriMindDisclaimerCard
import com.example.ui.components.RiskCategoryBadge
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.HealthAmber

@Composable
fun HistoryScreen(
    repository: AppRepository
) {
    val currentUser by repository.currentUser.collectAsState()
    val studentId = currentUser?.id ?: ""

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Cek Gizi, 1 = Cek Harian, 2 = Foto Makanan
    var selectedRiskFilter by remember { mutableStateOf("Semua") }

    val nutritionChecks by repository.getNutritionChecks(studentId).collectAsState(initial = emptyList())
    val dailyChecks by repository.getDailyChecks(studentId).collectAsState(initial = emptyList())
    val foodScans by repository.getFoodScans(studentId).collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp)
    ) {
        Text(
            text = "Riwayat & Perkembangan Gizi",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = EmeraldDark
            )
        )
        Text(
            text = "Pantau grafik perubahan fisik dan catatan kesehatan harianmu secara runtut.",
            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B)),
            modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
        )

        // Tab Navigation
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(12.dp)),
            indicator = { tabPositions ->
                SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = EmeraldDark
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Cek Gizi (${nutritionChecks.size})", fontSize = 12.sp, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Cek Harian (${dailyChecks.size})", fontSize = 12.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Foto Makanan (${foodScans.size})", fontSize = 12.sp, fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (selectedTab) {
            0 -> {
                // --- Tab Cek Gizi dengan Grafik Perkembangan BB & IMT ---
                val filteredChecks = if (selectedRiskFilter == "Semua") {
                    nutritionChecks
                } else {
                    nutritionChecks.filter { it.riskCategory.contains(selectedRiskFilter, ignoreCase = true) }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Visual Trend Graph Card
                    if (nutritionChecks.size >= 2) {
                        item {
                            BmiTrendGraphCard(checks = nutritionChecks.reversed())
                        }
                    }

                    // Risk Filter Chips
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Semua", "Risiko Rendah", "Perlu Perhatian", "Risiko Tinggi").forEach { filter ->
                                FilterChip(
                                    selected = selectedRiskFilter == filter,
                                    onClick = { selectedRiskFilter = filter },
                                    label = { Text(filter) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = EmeraldDark,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }

                    if (filteredChecks.isEmpty()) {
                        item {
                            EmptyHistoryCard("Belum ada riwayat cek gizi yang sesuai.")
                        }
                    } else {
                        items(filteredChecks, key = { it.id }) { check ->
                            NutritionCheckHistoryItem(check = check)
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        NutriMindDisclaimerCard()
                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }
            }

            1 -> {
                // --- Tab Cek Harian ---
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (dailyChecks.isEmpty()) {
                        item {
                            EmptyHistoryCard("Belum ada data cek kesehatan harian. Mulai isi cek harian hari ini!")
                        }
                    } else {
                        items(dailyChecks, key = { it.id }) { daily ->
                            DailyCheckHistoryItem(daily = daily)
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        NutriMindDisclaimerCard()
                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }
            }

            2 -> {
                // --- Tab Foto Makanan ---
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (foodScans.isEmpty()) {
                        item {
                            EmptyHistoryCard("Belum ada foto makanan yang dianalisis. Ambil foto bekal atau menu kantinmu!")
                        }
                    } else {
                        items(foodScans, key = { it.id }) { scan ->
                            FoodScanHistoryItem(scan = scan)
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        NutriMindDisclaimerCard()
                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun BmiTrendGraphCard(checks: List<NutritionCheckEntity>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("trend_graph_card"),
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ShowChart,
                        contentDescription = null,
                        tint = EmeraldDark,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Grafik Tren Berat Badan & IMT",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = EmeraldDark
                    )
                }
                Text(
                    text = "${checks.size} Pengecekan",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Canvas Line Chart
            val weights = checks.map { it.weightKg }
            val bmis = checks.map { it.bmi }
            val minWeight = (weights.minOrNull() ?: 40f) - 2f
            val maxWeight = (weights.maxOrNull() ?: 70f) + 2f

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(Color(0xFFF8FAFC), RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val stepX = if (weights.size > 1) w / (weights.size - 1) else w / 2

                    // Grid line
                    drawLine(
                        color = Color(0xFFE2E8F0),
                        start = Offset(0f, h / 2),
                        end = Offset(w, h / 2),
                        strokeWidth = 1f
                    )

                    val path = Path()
                    weights.forEachIndexed { index, weight ->
                        val normalizedY = (weight - minWeight) / (maxWeight - minWeight).coerceAtLeast(1f)
                        val x = index * stepX
                        val y = h - (normalizedY * h)

                        if (index == 0) {
                            path.moveTo(x, y)
                        } else {
                            path.lineTo(x, y)
                        }

                        // Point circle
                        drawCircle(
                            color = Color(0xFF0D9488),
                            radius = 4.dp.toPx(),
                            center = Offset(x, y)
                        )
                    }

                    drawPath(
                        path = path,
                        color = Color(0xFF0D9488),
                        style = Stroke(width = 2.5.dp.toPx())
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                checks.forEach { check ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${check.weightKg}kg",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "IMT ${check.bmi}",
                            fontSize = 10.sp,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            text = check.dateString.takeLast(5),
                            fontSize = 9.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NutritionCheckHistoryItem(check: NutritionCheckEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tanggal: ${check.dateString}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFF0F172A)
                )
                RiskCategoryBadge(category = check.riskCategory)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Berat Badan", fontSize = 10.sp, color = Color(0xFF64748B))
                    Text("${check.weightKg} kg", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Tinggi Badan", fontSize = 10.sp, color = Color(0xFF64748B))
                    Text("${check.heightCm.toInt()} cm", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("IMT", fontSize = 10.sp, color = Color(0xFF64748B))
                    Text("${check.bmi}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = EmeraldDark)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Status", fontSize = 10.sp, color = Color(0xFF64748B))
                    Text(check.bmiCategory, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Faktor: ${check.factorsToWatch}",
                fontSize = 11.sp,
                color = Color(0xFF475569),
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
fun DailyCheckHistoryItem(daily: DailyCheckEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Cek Tanggal: ${daily.dateString}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFF0F172A)
                )
                Box(
                    modifier = Modifier
                        .background(Color(0xFFDCFCE7), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = daily.bodyCondition,
                        fontSize = 11.sp,
                        color = Color(0xFF16A34A),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "• Sarapan: ${if (daily.hadBreakfast) "Ya" else "Tidak"} | Makan: ${daily.mealCount}x | Sayur: ${if (daily.ateVegetable) "Ya" else "Tidak"} | Buah: ${if (daily.ateFruit) "Ya" else "Tidak"}",
                fontSize = 11.sp,
                color = Color(0xFF475569)
            )

            Text(
                text = "• Olahraga: ${daily.physicalActivityDuration} | Tidur: ${daily.sleepDuration} | Minuman Manis: ${daily.sugaryDrinkCount}",
                fontSize = 11.sp,
                color = Color(0xFF475569),
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Rekomendasi: ${daily.summaryAdvice}",
                fontSize = 11.sp,
                color = Color(0xFF0F766E),
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
fun FoodScanHistoryItem(scan: FoodScanEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = scan.detectedFoods,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFF0F172A)
                )
                Box(
                    modifier = Modifier
                        .background(Color(0xFFCCFBF1), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = scan.balanceAssessment,
                        fontSize = 10.sp,
                        color = EmeraldDark,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Kategori: ${scan.foodCategory} • ${scan.dateString}",
                fontSize = 11.sp,
                color = Color(0xFF64748B)
            )

            Text(
                text = "Nutrisi: ${scan.approxNutrients}",
                fontSize = 11.sp,
                color = Color(0xFF334155),
                modifier = Modifier.padding(top = 4.dp)
            )

            Text(
                text = "Umpan Balik: ${scan.educationalFeedback}",
                fontSize = 11.sp,
                color = Color(0xFF0F766E),
                lineHeight = 15.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun EmptyHistoryCard(message: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                fontSize = 13.sp,
                color = Color(0xFF64748B)
            )
        }
    }
}
