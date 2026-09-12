package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Wc
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppRepository
import com.example.ui.components.NutriMindDisclaimerCard
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldDark

@Composable
fun ProfileScreen(
    repository: AppRepository,
    onEditProfile: () -> Unit,
    onLogout: () -> Unit
) {
    val currentUser by repository.currentUser.collectAsState()
    val user = currentUser ?: return
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Top Header with Title and Quick Logout Action
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Profil Pengguna",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = EmeraldDark
                    )
                )
                Text(
                    text = "Data akun dan pengaturan pemantauan gizi.",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B)),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            OutlinedButton(
                onClick = { showLogoutDialog = true },
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                modifier = Modifier.testTag("header_logout_button")
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = "Log Out",
                    tint = Color(0xFFDC2626),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Log Out",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFDC2626)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Avatar Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(EmeraldContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.nameOrInitial.take(2).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = EmeraldDark,
                        fontSize = 24.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = user.nameOrInitial,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF0F172A)
                )

                Text(
                    text = if (user.role == "STUDENT") "Siswa Madrasah • Kelas ${user.className}" else "Petugas UKS Madrasah",
                    fontSize = 13.sp,
                    color = Color(0xFF64748B)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (user.role == "STUDENT") {
                        OutlinedButton(
                            onClick = onEditProfile,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("edit_profile_button")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp), tint = EmeraldDark)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Edit Profil", color = EmeraldDark, fontSize = 13.sp)
                        }
                    }

                    OutlinedButton(
                        onClick = { showLogoutDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFFECACA)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                        modifier = Modifier.testTag("avatar_logout_button")
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFFDC2626))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Log Out", color = Color(0xFFDC2626), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Data Rincian
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Informasi Akun Terdaftar",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(10.dp))

                ProfileInfoRow(Icons.Default.Email, "Email Akun", user.email)
                if (user.role == "STUDENT") {
                    ProfileInfoRow(Icons.Default.Badge, "Nomor Identitas Siswa (NIS)", user.studentIdNumber.ifEmpty { "-" })
                    ProfileInfoRow(Icons.Default.Cake, "Usia", "${user.age} Tahun")
                    ProfileInfoRow(Icons.Default.Wc, "Jenis Kelamin", user.gender)
                    ProfileInfoRow(Icons.Default.School, "Madrasah", user.madrasahName)
                } else {
                    ProfileInfoRow(Icons.Default.Badge, "Kode / NIP Petugas", user.studentIdNumber.ifEmpty { "-" })
                    ProfileInfoRow(Icons.Default.School, "Instansi Madrasah", user.madrasahName)
                    ProfileInfoRow(Icons.Default.Security, "Status Verifikasi", if (user.isVerified) "Terverifikasi Aktif ✓" else "Menunggu Persetujuan")
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Kartu Utama Pengaturan Akun & Log Out
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Sesi & Keamanan Akun",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "Keluar dari akun saat ini untuk mengakhiri sesi atau berganti ke akun lain.",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                )

                Button(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("logout_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Log Out",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Log Out (Keluar Akun)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Menu Informasi Tambahan
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAboutDialog = true }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = EmeraldDark, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Tentang NutriMind AI", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        NutriMindDisclaimerCard()
        Spacer(modifier = Modifier.height(30.dp))
    }

    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color(0xFFFEE2E2), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = null,
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "Konfirmasi Log Out",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = if (user.role == "STUDENT")
                            "Apakah kamu yakin ingin keluar dari akun ini?"
                        else
                            "Apakah Anda yakin ingin keluar dari akun petugas UKS?",
                        fontSize = 14.sp,
                        color = Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Akun aktif: ${user.email}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF334155)
                            )
                            Text(
                                text = "Nama: ${user.nameOrInitial}",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Data rekam dan riwayat gizimu tetap tersimpan aman. Kamu dapat masuk kembali kapan saja.",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B),
                        lineHeight = 16.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        repository.setCurrentUser(null)
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Ya, Log Out", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Batal", color = Color(0xFF64748B))
                }
            }
        )
    }

    // About App Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("Tentang NutriMind AI") },
            text = {
                Column {
                    Text(
                        text = "Karya Ilmiah:\n“NutriMind AI: Sistem Deteksi Dini Berbasis Artificial Intelligence untuk Penanganan Risiko Masalah Gizi Siswa Madrasah.”\n\nFokus Aplikasi:\nHanya pada gizi dan kesehatan fisik yang berkaitan dengan gizi siswa madrasah.\n\nSifat Aplikasi:\nNutriMind AI merupakan sistem skrining dan pemantauan awal, bukan alat diagnosis medis.\n\nVersi 1.0.0 (Tahun 2026)",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF334155),
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showAboutDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldDark)
                ) {
                    Text("Tutup")
                }
            }
        )
    }
}

@Composable
fun ProfileInfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(label, fontSize = 11.sp, color = Color(0xFF64748B))
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
        }
    }
}
