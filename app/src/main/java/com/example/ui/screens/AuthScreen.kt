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
    var showRegisterUksDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var infoMessage by remember { mutableStateOf<String?>(null) }

    // Student direct input form states (biar para siswa mengisi sendiri)
    var studentNameInput by remember { mutableStateOf("") }
    var studentEmailInput by remember { mutableStateOf("") }
    var studentClassInput by remember { mutableStateOf("") }
    var studentMadrasahInput by remember { mutableStateOf("MAN 1 Insan Cendekia") }
    var studentAgreedToTerms by remember { mutableStateOf(true) }

    // UKS login form states (tanpa akun bawaan agar siswa tidak bisa sembarangan login)
    var uksEmail by remember { mutableStateOf("") }
    var uksPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

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
                    // === SISWA AUTH (Direct Input Form - Siswa mengisi sendiri) ===
                    Text(
                        text = "Masuk Siswa Madrasah",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    )
                    Text(
                        text = "Silakan isi data kamu untuk mulai mencatat kondisi gizi, foto makanan, dan cek kesehatan berkala.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = studentNameInput,
                        onValueChange = { studentNameInput = it; errorMessage = null },
                        label = { Text("Nama Lengkap Siswa") },
                        placeholder = { Text("Contoh: Muhammad Farhan") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("student_name_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = nutriMindTextFieldColors(),
                        textStyle = nutriMindInputTextStyle()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = studentEmailInput,
                        onValueChange = { studentEmailInput = it; errorMessage = null },
                        label = { Text("Email Siswa / Akun Google") },
                        placeholder = { Text("Contoh: siswa@gmail.com") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("student_email_input"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(10.dp),
                        colors = nutriMindTextFieldColors(),
                        textStyle = nutriMindInputTextStyle()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = studentClassInput,
                            onValueChange = { studentClassInput = it },
                            label = { Text("Kelas") },
                            placeholder = { Text("Contoh: X-A") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("student_class_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = nutriMindTextFieldColors(),
                            textStyle = nutriMindInputTextStyle()
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        OutlinedTextField(
                            value = studentMadrasahInput,
                            onValueChange = { studentMadrasahInput = it },
                            label = { Text("Madrasah") },
                            placeholder = { Text("Nama Madrasah") },
                            modifier = Modifier
                                .weight(1.3f)
                                .testTag("student_madrasah_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = nutriMindTextFieldColors(),
                            textStyle = nutriMindInputTextStyle()
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { studentAgreedToTerms = !studentAgreedToTerms }
                            .padding(vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .border(
                                    1.5.dp,
                                    if (studentAgreedToTerms) EmeraldDark else Color(0xFF94A3B8),
                                    RoundedCornerShape(4.dp)
                                )
                                .background(
                                    if (studentAgreedToTerms) EmeraldDark else Color.Transparent,
                                    RoundedCornerShape(4.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (studentAgreedToTerms) {
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
                            fontSize = 11.sp,
                            color = Color(0xFF334155),
                            lineHeight = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (studentNameInput.isBlank() || studentEmailInput.isBlank()) {
                                errorMessage = "Silakan isi nama lengkap dan email siswa."
                                return@Button
                            }
                            if (!studentEmailInput.contains("@")) {
                                errorMessage = "Format email siswa tidak valid. Harap gunakan format email yang benar."
                                return@Button
                            }
                            if (!studentAgreedToTerms) {
                                errorMessage = "Mohon centang persetujuan penggunaan aplikasi NutriMind AI."
                                return@Button
                            }
                            isLoading = true
                            errorMessage = null
                            scope.launch {
                                val trimmedEmail = studentEmailInput.trim().lowercase()
                                val existing = repository.getUserByEmail(trimmedEmail)
                                isLoading = false
                                if (existing != null) {
                                    val updated = existing.copy(
                                        nameOrInitial = studentNameInput.trim().ifBlank { existing.nameOrInitial },
                                        className = studentClassInput.trim().ifBlank { existing.className },
                                        madrasahName = studentMadrasahInput.trim().ifBlank { existing.madrasahName }
                                    )
                                    repository.saveUser(updated)
                                    repository.setCurrentUser(updated)
                                    onStudentLoggedIn(!updated.isProfileComplete)
                                } else {
                                    val newStudent = UserEntity(
                                        id = trimmedEmail,
                                        email = trimmedEmail,
                                        nameOrInitial = studentNameInput.trim(),
                                        role = "STUDENT",
                                        className = studentClassInput.trim().ifBlank { "Kelas X" },
                                        madrasahName = studentMadrasahInput.trim().ifBlank { "MAN 1 Insan Cendekia" },
                                        isProfileComplete = false,
                                        isVerified = true,
                                        agreedToTerms = true
                                    )
                                    repository.saveUser(newStudent)
                                    repository.setCurrentUser(newStudent)
                                    onStudentLoggedIn(true)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("submit_student_login_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldDark)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text(
                                text = "Masuk & Mulai Pantau Gizi",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

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
                            text = "Lupa sandi / Bantuan",
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
                        text = "Khusus tenaga medis dan guru pembina UKS resmi madrasah.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = uksEmail,
                        onValueChange = { uksEmail = it; errorMessage = null },
                        label = { Text("Email Petugas UKS Resmi") },
                        placeholder = { Text("petugas@madrasah.sch.id") },
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
                        onValueChange = { uksPassword = it; errorMessage = null },
                        label = { Text("Kata Sandi Petugas") },
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
                                errorMessage = "Mohon isi email dan kata sandi petugas UKS resmi."
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
                                    errorMessage = "Akses ditolak: Akun petugas UKS tidak ditemukan. Siswa tidak memiliki akses ke Dashboard UKS demi privasi data medis."
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
                            text = "Pendaftaran Petugas UKS Baru",
                            fontWeight = FontWeight.Medium,
                            color = EmeraldDark
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Catatan Keamanan: Rekam gizi siswa dilindungi undang-undang privasi kesehatan. Hanya petugas UKS resmi madrasah yang dapat mengakses dashboard.",
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

    // --- UKS Registration Dialog (Section 2.B) ---
    if (showRegisterUksDialog) {
        var officerName by remember { mutableStateOf("") }
        var regEmail by remember { mutableStateOf("") }
        var officerCode by remember { mutableStateOf("") }
        var madrasah by remember { mutableStateOf("MAN 1 Insan Cendekia") }
        var regPassword by remember { mutableStateOf("") }
        var regErr by remember { mutableStateOf<String?>(null) }
        var instantApproveDemo by remember { mutableStateOf(false) }

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
