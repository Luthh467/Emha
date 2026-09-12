package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.example.ui.components.NutriMindDisclaimerCard
import com.example.ui.components.nutriMindInputTextStyle
import com.example.ui.components.nutriMindTextFieldColors
import com.example.ui.theme.EmeraldDark
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompleteProfileScreen(
    repository: AppRepository,
    onProfileCompleted: () -> Unit,
    onLogout: () -> Unit = {}
) {
    val currentUser by repository.currentUser.collectAsState()
    val scope = rememberCoroutineScope()

    var nameOrInitial by remember(currentUser) { mutableStateOf(currentUser?.nameOrInitial ?: "") }
    var className by remember(currentUser) { mutableStateOf(currentUser?.className ?: "X-1") }
    var ageString by remember(currentUser) { mutableStateOf(currentUser?.age?.toString() ?: "16") }
    var gender by remember(currentUser) { mutableStateOf(currentUser?.gender ?: "Laki-laki") }
    var studentIdNumber by remember(currentUser) { mutableStateOf(currentUser?.studentIdNumber ?: "") }
    var madrasahName by remember(currentUser) { mutableStateOf(currentUser?.madrasahName ?: "MAN 1 Insan Cendekia") }
    var errorText by remember { mutableStateOf<String?>(null) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Lengkapi Profil Siswa",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                actions = {
                    IconButton(
                        onClick = { showLogoutDialog = true },
                        modifier = Modifier.testTag("complete_profile_logout_action")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Log Out",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = EmeraldDark)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Identitas Pemantauan Gizi",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = EmeraldDark
                        )
                    )
                    Text(
                        text = "Data ini digunakan untuk menghitung standar antropometri (IMT/U) dan pemantauan berkala oleh UKS.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B)),
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                    )

                    // Nama / Inisial
                    OutlinedTextField(
                        value = nameOrInitial,
                        onValueChange = { nameOrInitial = it },
                        label = { Text("Nama Lengkap atau Inisial") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = EmeraldDark) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_name_input"),
                        singleLine = true,
                        colors = nutriMindTextFieldColors(),
                        textStyle = nutriMindInputTextStyle()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Kelas
                    OutlinedTextField(
                        value = className,
                        onValueChange = { className = it },
                        label = { Text("Kelas (Contoh: X-A, XI-IPA 2, XII-IPS)") },
                        leadingIcon = { Icon(Icons.Default.School, contentDescription = null, tint = EmeraldDark) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_class_input"),
                        singleLine = true,
                        colors = nutriMindTextFieldColors(),
                        textStyle = nutriMindInputTextStyle()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Umur
                    OutlinedTextField(
                        value = ageString,
                        onValueChange = { ageString = it },
                        label = { Text("Usia (Tahun, contoh: 16)") },
                        leadingIcon = { Icon(Icons.Default.Cake, contentDescription = null, tint = EmeraldDark) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_age_input"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = nutriMindTextFieldColors(),
                        textStyle = nutriMindInputTextStyle()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // NIS
                    OutlinedTextField(
                        value = studentIdNumber,
                        onValueChange = { studentIdNumber = it },
                        label = { Text("Nomor Identitas Siswa (NIS / NISN)") },
                        leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = EmeraldDark) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_nis_input"),
                        singleLine = true,
                        colors = nutriMindTextFieldColors(),
                        textStyle = nutriMindInputTextStyle()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Madrasah
                    OutlinedTextField(
                        value = madrasahName,
                        onValueChange = { madrasahName = it },
                        label = { Text("Nama Madrasah") },
                        leadingIcon = { Icon(Icons.Default.School, contentDescription = null, tint = EmeraldDark) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = nutriMindTextFieldColors(),
                        textStyle = nutriMindInputTextStyle()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Jenis Kelamin
                    Text(
                        text = "Jenis Kelamin:",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1E293B)
                        )
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { gender = "Laki-laki" }
                                .padding(end = 24.dp)
                        ) {
                            RadioButton(
                                selected = gender == "Laki-laki",
                                onClick = { gender = "Laki-laki" },
                                colors = RadioButtonDefaults.colors(selectedColor = EmeraldDark)
                            )
                            Text("Laki-laki", fontSize = 14.sp)
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { gender = "Perempuan" }
                        ) {
                            RadioButton(
                                selected = gender == "Perempuan",
                                onClick = { gender = "Perempuan" },
                                colors = RadioButtonDefaults.colors(selectedColor = EmeraldDark)
                            )
                            Text("Perempuan", fontSize = 14.sp)
                        }
                    }

                    if (errorText != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorText!!,
                            color = Color(0xFFE11D48),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (nameOrInitial.isBlank()) {
                                errorText = "Nama atau inisial tidak boleh kosong."
                                return@Button
                            }
                            val age = ageString.toIntOrNull()
                            if (age == null || age < 10 || age > 25) {
                                errorText = "Masukkan usia yang valid (10-25 tahun)."
                                return@Button
                            }

                            scope.launch {
                                val current = currentUser
                                if (current != null) {
                                    val updated = current.copy(
                                        nameOrInitial = nameOrInitial.trim(),
                                        className = className.trim(),
                                        age = age,
                                        gender = gender,
                                        studentIdNumber = studentIdNumber.trim(),
                                        madrasahName = madrasahName.trim(),
                                        isProfileComplete = true
                                    )
                                    repository.saveUser(updated)
                                    onProfileCompleted()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("save_profile_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldDark)
                    ) {
                        Text(
                            text = "Simpan Profil & Mulai",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = { showLogoutDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("complete_profile_logout_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Log Out",
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Keluar / Ganti Akun Lain",
                            color = Color(0xFFDC2626),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            NutriMindDisclaimerCard()
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = null,
                    tint = Color(0xFFDC2626),
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = "Konfirmasi Keluar Akun",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "Apakah kamu yakin ingin keluar dari sesi akun ini? Kamu dapat masuk kembali kapan saja.",
                    fontSize = 14.sp,
                    color = Color(0xFF334155)
                )
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
}
