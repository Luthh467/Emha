package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.AppRepository
import com.example.ui.components.NutriMindDisclaimerCard
import com.example.ui.components.RiskCategoryBadge
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GreenContainer
import com.example.ui.theme.HealthAmber
import com.example.ui.theme.SafeGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    repository: AppRepository,
    onNavigateTo: (String) -> Unit
) {
    val currentUser by repository.currentUser.collectAsState()
    val studentId = currentUser?.id ?: ""

    val latestCheck by repository.getLatestNutritionCheck(studentId).collectAsState(initial = null)

    val todayDateString = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }
    val todayCheck by repository.getDailyCheckForDate(studentId, todayDateString).collectAsState(initial = null)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // --- Sapaan Pengguna ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Assalamu’alaikum,",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF64748B)
                    )
                )
                Text(
                    text = currentUser?.nameOrInitial ?: "Siswa Madrasah",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = EmeraldDark
                    )
                )
                Text(
                    text = "Kelas ${currentUser?.className.orEmpty().ifEmpty { "X" }} • ${currentUser?.madrasahName ?: "Madrasah"}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF475569)
                    )
                )
            }

            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(EmeraldContainer)
                    .clickable { onNavigateTo("profile") },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (currentUser?.nameOrInitial?.take(2) ?: "SM").uppercase(),
                    fontWeight = FontWeight.Bold,
                    color = EmeraldDark,
                    fontSize = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- Hero Banner Art ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(id = R.drawable.banner_nutrimind),
                    contentDescription = "NutriMind Banner",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xDD0F766E),
                                    Color(0x880D9488),
                                    Color.Transparent
                                )
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.CenterStart)
                ) {
                    Text(
                        text = "Jaga Gizimu, Raih Prestasimu",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Skrining rutin bantu deteksi awal & optimalkan konsentrasi belajar.",
                        color = Color(0xFFCCFBF1),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Kartu Ringkasan Utama ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("summary_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ringkasan Pemantauan Gizi",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    )
                    if (latestCheck != null) {
                        RiskCategoryBadge(category = latestCheck!!.riskCategory)
                    } else {
                        RiskCategoryBadge(category = "Belum Ada Data")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (latestCheck != null) {
                    val check = latestCheck!!
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Indeks IMT", fontSize = 11.sp, color = Color(0xFF64748B))
                            Text(
                                text = "${check.bmi}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldDark
                            )
                            Text(check.bmiCategory, fontSize = 11.sp, color = Color(0xFF334155))
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(40.dp)
                                .background(Color(0xFFE2E8F0))
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Berat / Tinggi", fontSize = 11.sp, color = Color(0xFF64748B))
                            Text(
                                text = "${check.weightKg} kg / ${check.heightCm.toInt()} cm",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1E293B)
                            )
                            Text("Terakhir: ${check.dateString}", fontSize = 11.sp, color = Color(0xFF64748B))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Saran Terakhir: ${check.factorsToWatch}",
                        fontSize = 12.sp,
                        color = Color(0xFF475569),
                        lineHeight = 16.sp
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFEF3C7), RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = HealthAmber,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Belum Melakukan Cek Gizi",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF78350F)
                                )
                                Text(
                                    text = "Isi data tinggi dan berat badan untuk mengetahui kategori gizi dan risiko kamu.",
                                    fontSize = 12.sp,
                                    color = Color(0xFF92400E)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Status Cek Hari Ini
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (todayCheck != null) GreenContainer else AmberContainer,
                            RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (todayCheck != null) Icons.Default.CheckCircle else Icons.Default.Today,
                            contentDescription = null,
                            tint = if (todayCheck != null) SafeGreen else HealthAmber,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (todayCheck != null) "Cek harian hari ini: Sudah diisi ✓" else "Cek kesehatan hari ini: Belum diisi",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (todayCheck != null) SafeGreen else HealthAmber
                        )
                    }

                    Text(
                        text = if (todayCheck != null) "Lihat" else "Isi Sekarang",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (todayCheck != null) SafeGreen else HealthAmber,
                        modifier = Modifier.clickable { onNavigateTo("daily_check") }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- Banner Chatbot NutriMind AI (Gemini) ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateTo("chat") }
                .testTag("home_chat_banner"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F766E)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(0xFF14B8A6), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Chatbot AI",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Tanya NutriMind AI",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF2DD4BF)
                            ) {
                                Text(
                                    text = "Online & Cepat",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldDark,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Tanya tips sarapan, bekal sehat, atau atasi lemas di kelas.",
                            color = Color(0xFFCCFBF1),
                            fontSize = 11.5.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Tombol Cepat Navigasi Fitur Utama ---
        Text(
            text = "Fitur Pemantauan Siswa",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
        )
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            HomeQuickActionCard(
                title = "Cek Gizi",
                subtitle = "Antropometri IMT/U",
                icon = Icons.Default.MonitorWeight,
                backgroundColor = Color(0xFFCCFBF1),
                iconTint = EmeraldDark,
                modifier = Modifier.weight(1f),
                onClick = { onNavigateTo("nutrition_check") }
            )

            HomeQuickActionCard(
                title = "Foto Makanan",
                subtitle = "AI Analisis Piring",
                icon = Icons.Default.CameraAlt,
                backgroundColor = Color(0xFFFEF3C7),
                iconTint = HealthAmber,
                modifier = Modifier.weight(1f),
                onClick = { onNavigateTo("food_scan") }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            HomeQuickActionCard(
                title = "Cek Harian",
                subtitle = "10 Pertanyaan Cepat",
                icon = Icons.Default.Today,
                backgroundColor = Color(0xFFE0E7FF),
                iconTint = Color(0xFF4338CA),
                modifier = Modifier.weight(1f),
                onClick = { onNavigateTo("daily_check") }
            )

            HomeQuickActionCard(
                title = "Edukasi Gizi",
                subtitle = "6 Kategori Belajar",
                icon = Icons.Default.MenuBook,
                backgroundColor = Color(0xFFDCFCE7),
                iconTint = SafeGreen,
                modifier = Modifier.weight(1f),
                onClick = { onNavigateTo("education") }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Riwayat Lengkap Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateTo("history") }
                .testTag("nav_history_shortcut"),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFFF1F5F9), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = EmeraldDark,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Riwayat & Tren Perubahan Gizi",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Grafik perubahan berat badan & riwayat foto makanan",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Mandatory Disclaimer Card
        NutriMindDisclaimerCard()

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun HomeQuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    backgroundColor: Color,
    iconTint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(backgroundColor, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = Color(0xFF64748B),
                    maxLines = 1
                )
            }
        }
    }
}
