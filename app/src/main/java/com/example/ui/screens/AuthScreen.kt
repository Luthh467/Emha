package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.AppRepository
import com.example.data.model.UserEntity
import com.example.ui.components.NutriMindDisclaimerCard
import com.example.ui.components.nutriMindInputTextStyle
import com.example.ui.components.nutriMindTextFieldColors
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.HealthAmber
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    repository: AppRepository,
    onStudentLoggedIn: (needsProfile: Boolean) -> Unit,
    onUksLoggedIn: () -> Unit
) {
    var selectedRoleTab by remember { mutableIntStateOf(0) } // 0 = Siswa, 1 = Petugas UKS
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Dialog states
    var showHelpDialog by remember { mutableStateOf(false) }
    var showGoogleAccountPicker by remember { mutableStateOf(false) }
    var showNewStudentDialog by remember { mutableStateOf(false) }
    var showRegisterUksDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var infoMessage by remember { mutableStateOf<String?>(null) }

    // UKS login form states
    var uksEmail by remember { mutableStateOf("uks@madrasah.sch.id") }
    var uksPassword by remember { mutableStateOf("uks12345") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val allStudents by repository.getAllStudents().collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(scrollState)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // App Logo & Branding
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(EmeraldDark, EmeraldPrimary)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_nutrimind_logo),
                contentDescription = "NutriMind AI Logo",
                modifier = Modifier.size(54.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "NutriMind AI",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = EmeraldDark,
                letterSpacing = 0.5.sp
            )
        )

        Text(
            text = "“Temanmu untuk mengenal dan menjaga gizi setiap hari.”",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color(0xFF0F766E),
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        Text(
            text = "Pantau kondisi gizi, kenali makananmu, dan bangun kebiasaan sehat di lingkungan madrasah.",
            style = MaterialTheme.typography.bodySmall.copy(
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Role Selector Tabs (Masuk sebagai Siswa vs Masuk sebagai Petugas UKS)
        TabRow(
            selectedTabIndex = selectedRoleTab,
            containerColor = Color(0xFFE2E8F0),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp)),
            indicator = { tabPositions ->
                SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedRoleTab]),
                    color = EmeraldDark
                )
            }
        ) {
            Tab(
                selected = selectedRoleTab == 0,
                onClick = { selectedRoleTab = 0; errorMessage = null; infoMessage = null },
                modifier = Modifier.testTag("tab_student"),
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = if (selectedRoleTab == 0) EmeraldDark else Color(0xFF64748B)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Masuk Siswa",
                            fontWeight = if (selectedRoleTab == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedRoleTab == 0) EmeraldDark else Color(0xFF64748B)
                        )
                    }
                }
            )

            Tab(
                selected = selectedRoleTab == 1,
                onClick = { selectedRoleTab = 1; errorMessage = null; infoMessage = null },
                modifier = Modifier.testTag("tab_uks"),
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.HealthAndSafety,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = if (selectedRoleTab == 1) EmeraldDark else Color(0xFF64748B)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Petugas UKS",
                            fontWeight = if (selectedRoleTab == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedRoleTab == 1) EmeraldDark else Color(0xFF64748B)
                        )
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Card Container for Forms
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = Color(0xFFE11D48),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFE4E6), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }

                if (infoMessage != null) {
                    Text(
                        text = infoMessage!!,
                        color = Color(0xFF047857),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFD1FAE5), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }

                if (selectedRoleTab == 0) {
                    // === SISWA AUTH ===
                    Text(
                        text = "Akses Pemantauan Gizi Siswa",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    )
                    Text(
                        text = "Masuk untuk mencatat kondisi fisik, foto makanan, dan cek kesehatan berkala.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
                    )

                    // Tombol Lanjutkan dengan Google
                    Button(
                        onClick = { showGoogleAccountPicker = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("google_login_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldDark
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Stylized Google 'G'
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "G",
                                    color = Color(0xFF4285F4),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Lanjutkan dengan Google",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Tombol Buat Akun Baru (Selalu tersedia)
                    OutlinedButton(
                        onClick = { showNewStudentDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("create_student_account_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = EmeraldDark,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Buat Akun Baru",
                            fontWeight = FontWeight.SemiBold,
                            color = EmeraldDark
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Keterangan login yang ramah dan tidak terlalu diprivasi
                    Text(
                        text = "Masuk dengan akun Google terdaftar atau buat akun baru untuk mulai mencatat dan memantau status gizi madrasahmu.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF64748B),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Lupa sandi
                    TextButton(
                        onClick = { showHelpDialog = true },
                        modifier = Modifier.testTag("login_help_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = EmeraldDark,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Lupa sandi",
                            color = EmeraldDark,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    // === PETUGAS UKS AUTH ===
                    Text(
                        text = "Portal Petugas UKS Madrasah",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    )
                    Text(
                        text = "Dashboard pemantauan gizi dan tindak lanjut kesehatan siswa.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = uksEmail,
                        onValueChange = { uksEmail = it },
                        label = { Text("Email Petugas UKS") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("uks_email_input"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(10.dp),
                        colors = nutriMindTextFieldColors(),
                        textStyle = nutriMindInputTextStyle()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = uksPassword,
                        onValueChange = { uksPassword = it },
                        label = { Text("Kata Sandi") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("uks_password_input"),
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (passwordVisible) "Sembunyikan" else "Tampilkan"
                                )
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = nutriMindTextFieldColors(),
                        textStyle = nutriMindInputTextStyle()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (uksEmail.isBlank() || uksPassword.isBlank()) {
                                errorMessage = "Mohon isi email dan kata sandi petugas UKS."
                                return@Button
                            }
                            isLoading = true
                            scope.launch {
                                val user = repository.getUserByEmail(uksEmail.trim())
                                isLoading = false
                                if (user != null && user.role == "UKS") {
                                    if (!user.isVerified) {
                                        errorMessage = "Akun petugas UKS ini sedang menunggu verifikasi administrator madrasah demi keamanan data siswa."
                                    } else {
                                        repository.setCurrentUser(user)
                                        infoMessage = "Login berhasil sebagai Petugas UKS."
                                        onUksLoggedIn()
                                    }
                                } else {
                                    errorMessage = "Email atau kata sandi petugas UKS tidak cocok. Silakan daftar atau gunakan akun demo."
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("uks_login_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldDark)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                        } else {
                            Text(
                                text = "Masuk sebagai Petugas UKS",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { showRegisterUksDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("create_uks_account_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "Buat Akun Petugas UKS",
                            fontWeight = FontWeight.Medium,
                            color = EmeraldDark
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Data kesehatan siswa bersifat rahasia. Akun UKS baru harus melalui proses persetujuan administrator sebelum dapat mengakses data.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF64748B),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Mandatory Screening Disclaimer Card
        NutriMindDisclaimerCard()

        Spacer(modifier = Modifier.height(20.dp))
    }

    // --- Google Account Picker Dialog (Pilihan Akun Siswa & Tambah Akun Baru) ---
    if (showGoogleAccountPicker) {
        AlertDialog(
            onDismissRequest = { showGoogleAccountPicker = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(Color(0xFF4285F4), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("G", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Pilih Akun Google Siswa",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "Pilih akun Google terdaftar untuk masuk, atau tambahkan akun baru:",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF475569)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (allStudents.isNotEmpty()) {
                        allStudents.forEach { student ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        showGoogleAccountPicker = false
                                        repository.setCurrentUser(student)
                                        if (!student.isProfileComplete) {
                                            onStudentLoggedIn(true)
                                        } else {
                                            onStudentLoggedIn(false)
                                        }
                                    },
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = EmeraldContainer.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountCircle,
                                        contentDescription = null,
                                        tint = EmeraldDark,
                                        modifier = Modifier.size(34.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = student.nameOrInitial,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = EmeraldDark
                                        )
                                        Text(
                                            text = student.email,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF0F172A)
                                        )
                                        if (student.className.isNotBlank() && student.className != "-") {
                                            Text(
                                                text = "Kelas ${student.className} • ${student.madrasahName}",
                                                fontSize = 11.sp,
                                                color = Color(0xFF475569)
                                            )
                                        }
                                    }
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = EmeraldDark,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Opsi Tambah / Buat Akun Baru
                    OutlinedButton(
                        onClick = {
                            showGoogleAccountPicker = false
                            showNewStudentDialog = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = EmeraldDark, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Gunakan / Daftarkan Akun Lain",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = EmeraldDark
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showGoogleAccountPicker = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // --- New Student Registration Step (Daftar Akun Baru Siswa) ---
    if (showNewStudentDialog) {
        var newEmail by remember { mutableStateOf("") }
        var agreedToTerms by remember { mutableStateOf(false) }
        var regError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showNewStudentDialog = false },
            title = {
                Text(
                    text = "Daftar Akun Baru Siswa",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column {
                    Text(
                        text = "Masukkan alamat email Google kamu untuk mulai mencatat riwayat pemantauan dan skrining gizi madrasah:",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF475569)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = newEmail,
                        onValueChange = { newEmail = it; regError = null },
                        label = { Text("Email Google Siswa") },
                        placeholder = { Text("contoh@gmail.com") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_student_email_input"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = nutriMindTextFieldColors(),
                        textStyle = nutriMindInputTextStyle()
                    )

                    if (regError != null) {
                        Text(
                            text = regError!!,
                            color = Color(0xFFE11D48),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { agreedToTerms = !agreedToTerms }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .border(
                                    1.5.dp,
                                    if (agreedToTerms) EmeraldDark else Color(0xFF94A3B8),
                                    RoundedCornerShape(4.dp)
                                )
                                .background(
                                    if (agreedToTerms) EmeraldDark else Color.Transparent,
                                    RoundedCornerShape(4.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (agreedToTerms) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Saya menyetujui penggunaan aplikasi NutriMind AI untuk skrining dan pemantauan gizi.",
                            fontSize = 12.sp,
                            color = Color(0xFF334155),
                            lineHeight = 16.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (!newEmail.contains("@")) {
                            regError = "Silakan masukkan email Google yang valid."
                            return@Button
                        }
                        if (!agreedToTerms) {
                            regError = "Kamu harus menyetujui penggunaan aplikasi."
                            return@Button
                        }
                        scope.launch {
                            val user = UserEntity(
                                id = newEmail.trim(),
                                email = newEmail.trim(),
                                nameOrInitial = newEmail.substringBefore("@"),
                                role = "STUDENT",
                                isProfileComplete = false,
                                agreedToTerms = true
                            )
                            repository.saveUser(user)
                            showNewStudentDialog = false
                            onStudentLoggedIn(true) // Direct to Lengkapi Profil
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldDark),
                    modifier = Modifier.testTag("submit_new_student_button")
                ) {
                    Text("Lanjutkan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewStudentDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // --- UKS Registration Dialog (Section 2.B) ---
    if (showRegisterUksDialog) {
        var officerName by remember { mutableStateOf("") }
        var regEmail by remember { mutableStateOf("") }
        var officerCode by remember { mutableStateOf("") }
        var madrasah by remember { mutableStateOf("MAN 1 Insan Cendekia") }
        var regPassword by remember { mutableStateOf("") }
        var regErr by remember { mutableStateOf<String?>(null) }
        var instantApproveDemo by remember { mutableStateOf(true) }

        AlertDialog(
            onDismissRequest = { showRegisterUksDialog = false },
            title = { Text("Pendaftaran Akun Petugas UKS") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "Daftarkan identitas petugas kesehatan / UKS madrasah:",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF475569)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = officerName,
                        onValueChange = { officerName = it },
                        label = { Text("Nama Lengkap & Gelar") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = nutriMindTextFieldColors(),
                        textStyle = nutriMindInputTextStyle()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = regEmail,
                        onValueChange = { regEmail = it },
                        label = { Text("Email Petugas") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = nutriMindTextFieldColors(),
                        textStyle = nutriMindInputTextStyle()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = officerCode,
                        onValueChange = { officerCode = it },
                        label = { Text("Kode / NIP Petugas") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = nutriMindTextFieldColors(),
                        textStyle = nutriMindInputTextStyle()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = madrasah,
                        onValueChange = { madrasah = it },
                        label = { Text("Nama Madrasah") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = nutriMindTextFieldColors(),
                        textStyle = nutriMindInputTextStyle()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = regPassword,
                        onValueChange = { regPassword = it },
                        label = { Text("Kata Sandi") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = nutriMindTextFieldColors(),
                        textStyle = nutriMindInputTextStyle()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Demo Approval toggle for reviewer ease
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { instantApproveDemo = !instantApproveDemo }
                            .background(Color(0xFFFEF3C7), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .border(
                                    1.dp,
                                    if (instantApproveDemo) HealthAmber else Color.Gray,
                                    RoundedCornerShape(3.dp)
                                )
                                .background(
                                    if (instantApproveDemo) HealthAmber else Color.Transparent,
                                    RoundedCornerShape(3.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (instantApproveDemo) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Mode Demo Pengujian: Langsung verifikasi akun agar bisa masuk.",
                            fontSize = 11.sp,
                            color = Color(0xFF78350F)
                        )
                    }

                    if (regErr != null) {
                        Text(
                            text = regErr!!,
                            color = Color(0xFFE11D48),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (officerName.isBlank() || regEmail.isBlank() || regPassword.isBlank()) {
                            regErr = "Mohon lengkapi semua kolom pendaftaran."
                            return@Button
                        }
                        scope.launch {
                            val newOfficer = UserEntity(
                                id = regEmail.trim(),
                                email = regEmail.trim(),
                                nameOrInitial = officerName.trim(),
                                role = "UKS",
                                studentIdNumber = officerCode.trim(),
                                madrasahName = madrasah.trim(),
                                passwordHash = regPassword.trim(),
                                isVerified = instantApproveDemo,
                                isProfileComplete = true
                            )
                            repository.saveUser(newOfficer)
                            showRegisterUksDialog = false
                            if (instantApproveDemo) {
                                repository.setCurrentUser(newOfficer)
                                onUksLoggedIn()
                            } else {
                                infoMessage = "Pendaftaran berhasil. Akun sedang menunggu verifikasi administrator madrasah."
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldDark)
                ) {
                    Text("Daftar Akun")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRegisterUksDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // --- Lupa Sandi Dialog (Updated from Login Help Dialog) ---
    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = EmeraldDark,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text("Lupa Sandi & Bantuan Masuk", textAlign = TextAlign.Center)
            },
            text = {
                Column {
                    Text(
                        text = "1. Akun Siswa Madrasah:\nAplikasi ini menggunakan autentikasi Google Sign-In langsung (1 orang 1 aplikasi). Aplikasi NutriMind AI tidak pernah menyimpan kata sandi akun Google kamu. Jika kamu lupa kata sandi akun Google, silakan gunakan fitur pemulihan akun di halaman Google (accounts.google.com/signin/recovery).\n\n2. Petugas UKS Madrasah:\nJika lupa kata sandi akun petugas UKS, silakan hubungi tim administrator IT madrasah untuk melakukan reset kredensial akun UKS kamu.\n\n3. Keamanan 1 Siswa 1 Aplikasi:\nAplikasi ini terkunci untuk satu email siswa agar rekam medis gizi tidak tercampur dengan orang lain.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF334155),
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showHelpDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldDark)
                ) {
                    Text("Mengerti")
                }
            }
        )
    }
}
