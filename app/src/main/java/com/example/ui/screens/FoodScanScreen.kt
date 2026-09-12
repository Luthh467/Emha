package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.R
import com.example.data.AppRepository
import com.example.data.model.FoodScanEntity
import com.example.data.remote.FoodAnalysisResponse
import com.example.data.remote.GeminiService
import com.example.ui.components.NutriMindDisclaimerCard
import com.example.ui.components.nutriMindTextFieldColors
import com.example.ui.components.nutriMindInputTextStyle
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.HealthAmber
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FoodScanScreen(
    repository: AppRepository,
    onNavigateToHistory: () -> Unit
) {
    val context = LocalContext.current
    val currentUser by repository.currentUser.collectAsState()
    val scope = rememberCoroutineScope()

    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var selectedSampleType by remember { mutableStateOf<String?>(null) }
    var selectedMealType by remember { mutableStateOf("Makan Siang (Bekal)") }
    var studentNotes by remember { mutableStateOf("") }
    var portionMultiplier by remember { mutableFloatStateOf(1.0f) }
    var selectedPlateSection by remember { mutableStateOf("all") }
    var isAnalyzing by remember { mutableStateOf(false) }
    var analysisResult by remember { mutableStateOf<FoodAnalysisResponse?>(null) }
    var isSaved by remember { mutableStateOf(false) }

    var tempPhotoFilePath by rememberSaveable { mutableStateOf<String?>(null) }
    var lastSavedPhotoPath by rememberSaveable { mutableStateOf<String?>(null) }
    var showCameraHelpDialog by remember { mutableStateOf(false) }
    var showPhotoSourceDialog by remember { mutableStateOf(false) }

    // Auto-restore photo if activity was recreated
    LaunchedEffect(lastSavedPhotoPath) {
        if (selectedBitmap == null && lastSavedPhotoPath != null) {
            val restored = decodeAndProcessCapturedPhoto(lastSavedPhotoPath!!)
            if (restored != null) {
                selectedBitmap = restored
                selectedSampleType = "Foto Kamera Siswa"
            }
        }
    }

    // Quick Preview Camera Launcher (fallback direct Bitmap from system camera)
    val takePicturePreviewLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            selectedBitmap = bitmap
            selectedSampleType = "Foto Kamera Siswa"
            analysisResult = null
            isSaved = false
            Toast.makeText(context, "Foto makanan berhasil diambil!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Batal mengambil foto.", Toast.LENGTH_SHORT).show()
        }
    }

    // Camera App launcher via TakePicture (Full Resolution File)
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        val filePath = tempPhotoFilePath
        val file = filePath?.let { File(it) }
        val fileHasData = file != null && file.exists() && file.length() > 0L
        if (success || fileHasData) {
            val processedBitmap = filePath?.let { decodeAndProcessCapturedPhoto(it) }
            if (processedBitmap != null) {
                selectedBitmap = processedBitmap
                lastSavedPhotoPath = filePath
                selectedSampleType = "Foto Kamera Siswa"
                analysisResult = null
                isSaved = false
                Toast.makeText(context, "Foto makanan berhasil diambil!", Toast.LENGTH_SHORT).show()
                return@rememberLauncherForActivityResult
            }
        }
        // Fallback gracefully if primary camera app didn't save file
        try {
            takePicturePreviewLauncher.launch(null)
        } catch (e: Exception) {
            showCameraHelpDialog = true
        }
    }

    fun launchCameraApp() {
        try {
            val storageDir = File(context.cacheDir, "camera_food").apply { mkdirs() }
            val photoFile = File(storageDir, "food_${System.currentTimeMillis()}.jpg")
            tempPhotoFilePath = photoFile.absolutePath
            val photoUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )
            takePictureLauncher.launch(photoUri)
        } catch (e: Exception) {
            // Fallback to TakePicturePreview without requiring FileProvider or external storage
            try {
                takePicturePreviewLauncher.launch(null)
            } catch (fallbackError: Exception) {
                showCameraHelpDialog = true
            }
        }
    }

    // Modern Photo Picker launcher with zero-permission requirement
    val visualMediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            val bmp = decodeBitmapFromUri(context, it)
            if (bmp != null) {
                selectedBitmap = bmp
                selectedSampleType = "Foto dari Galeri HP"
                analysisResult = null
                isSaved = false
                Toast.makeText(context, "Foto makanan berhasil dipilih dari galeri!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Gagal memproses gambar dari galeri", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Legacy Gallery Picker launcher as backup
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val bmp = decodeBitmapFromUri(context, it)
            if (bmp != null) {
                selectedBitmap = bmp
                selectedSampleType = "Foto dari Galeri HP"
                analysisResult = null
                isSaved = false
                Toast.makeText(context, "Foto makanan berhasil dipilih dari galeri!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Gagal memproses gambar dari galeri", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Camera permission request
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            launchCameraApp()
        } else {
            Toast.makeText(context, "Izin kamera diperlukan untuk memotret makanan", Toast.LENGTH_LONG).show()
        }
    }

    val onOpenCameraClick: () -> Unit = {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            launchCameraApp()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val onOpenGalleryClick: () -> Unit = {
        try {
            visualMediaPickerLauncher.launch(
                androidx.activity.result.PickVisualMediaRequest(
                    ActivityResultContracts.PickVisualMedia.ImageOnly
                )
            )
        } catch (e: Exception) {
            galleryLauncher.launch("image/*")
        }
    }

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
                    text = "AI Analisis Foto Makanan",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = EmeraldDark
                    )
                )
                Text(
                    text = "Foto makanan atau bekalmu untuk mengenali komposisi 'Isi Piringku' dan evaluasi gizi otomatis.",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B)),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            OutlinedButton(
                onClick = onNavigateToHistory,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("nav_history_button")
            ) {
                Icon(Icons.Default.History, contentDescription = null, tint = EmeraldDark, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Riwayat", fontSize = 12.sp, color = EmeraldDark, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- Photo Picker & Preview Card ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("food_photo_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (selectedBitmap != null) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Image(
                            bitmap = selectedBitmap!!.asImageBitmap(),
                            contentDescription = "Foto Makanan Terpilih",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(230.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )

                        // Status Tag di atas foto
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(10.dp)
                                .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = selectedSampleType ?: "Foto Siap Dianalisis",
                                fontSize = 11.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Keterangan Teks Foto Makanan di Bawah Foto yang Rapi & Terstruktur
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(EmeraldContainer, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Restaurant,
                                    contentDescription = null,
                                    tint = EmeraldDark,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "Foto Makanan Terpilih",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF64748B)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .background(EmeraldContainer, RoundedCornerShape(6.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Siap Pindai ✓",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldDark
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = selectedSampleType ?: "Hasil Jepretan Kamera Siswa",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Kategori: $selectedMealType • Siap dianalisis oleh AI",
                                    fontSize = 10.sp,
                                    color = Color(0xFF475569)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Tombol Analisis Langsung Tepat di Bawah Foto (Langsung Muncul Setelah Memotret)
                    Button(
                        onClick = {
                            isAnalyzing = true
                            scope.launch {
                                val result = repository.analyzeFoodImage(selectedBitmap, selectedMealType, studentNotes)
                                analysisResult = result
                                isAnalyzing = false
                            }
                        },
                        enabled = !isAnalyzing,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("immediate_analyze_food_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldDark)
                    ) {
                        if (isAnalyzing) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("NutriMind AI sedang menganalisis foto...", color = Color.White, fontSize = 13.sp)
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (analysisResult != null) "Analisis Ulang Foto Makanan" else "⚡ Analisis Gizi Makanan Sekarang",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(190.dp)
                            .border(2.dp, Color(0xFFCBD5E1), RoundedCornerShape(12.dp))
                            .background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
                            .clickable { showPhotoSourceDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .background(EmeraldContainer, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Buka Kamera",
                                    tint = EmeraldDark,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Ambil Foto Makanan atau Bekal",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldDark
                            )
                            Text(
                                text = "Buka kamera tegak lurus dari atas piring atau pilih dari galeri",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action Buttons: Kamera, Galeri, & Pilihan Sumber
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onOpenCameraClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("open_camera_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldDark),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (selectedBitmap != null) "Foto Ulang" else "Buka Kamera",
                            fontSize = 13.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = onOpenGalleryClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("open_gallery_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp), tint = EmeraldDark)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pilih Galeri", fontSize = 13.sp, color = EmeraldDark, fontWeight = FontWeight.SemiBold)
                    }

                    OutlinedButton(
                        onClick = { showPhotoSourceDialog = true },
                        modifier = Modifier.size(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Opsi Sumber Kamera",
                            tint = EmeraldDark,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // --- Dialog Pilihan Sumber Kamera & Foto ---
        if (showPhotoSourceDialog) {
            AlertDialog(
                onDismissRequest = { showPhotoSourceDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = EmeraldDark)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Pilih Sumber Foto", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Silakan pilih metode pengambilan foto makanan madrasah:",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )

                        // Opsi 1: Kamera Utama Resolusi Penuh
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showPhotoSourceDialog = false
                                    onOpenCameraClick()
                                },
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(EmeraldContainer, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = EmeraldDark, modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Kamera Utama (Full HD)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                    Text("Aplikasi kamera bawaan perangkat", fontSize = 10.sp, color = Color(0xFF64748B))
                                }
                            }
                        }

                        // Opsi 2: Kamera Cepat (Preview Langsung)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showPhotoSourceDialog = false
                                    try {
                                        takePicturePreviewLauncher.launch(null)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Kamera cepat tidak didukung", Toast.LENGTH_SHORT).show()
                                    }
                                },
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color(0xFFFEF3C7), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.FlashOn, contentDescription = null, tint = HealthAmber, modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Kamera Cepat (Instan)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                    Text("Tangkapan cepat tanpa simpan file", fontSize = 10.sp, color = Color(0xFF64748B))
                                }
                            }
                        }

                        // Opsi 3: Pilih dari Galeri HP
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showPhotoSourceDialog = false
                                    onOpenGalleryClick()
                                },
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color(0xFFEFF6FF), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Pilih dari Galeri HP", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                    Text("Pilih foto dari penyimpanan album", fontSize = 10.sp, color = Color(0xFF64748B))
                                }
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showPhotoSourceDialog = false }) {
                        Text("Tutup", color = Color(0xFF64748B))
                    }
                }
            )
        }

        // --- Dialog Bantuan Kamera Jika Bermasalah ---
        if (showCameraHelpDialog) {
            AlertDialog(
                onDismissRequest = { showCameraHelpDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = EmeraldDark)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Bantuan Kamera Makanan", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                },
                text = {
                    Text(
                        text = "Aplikasi kamera utama tidak dapat menyimpan foto di lingkungan perangkat ini. Kamu dapat mencoba Kamera Cepat (Preview), memilih foto piring dari Galeri, atau memakai contoh menu gizi seimbang yang sudah disiapkan.",
                        fontSize = 13.sp,
                        color = Color(0xFF334155),
                        lineHeight = 18.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showCameraHelpDialog = false
                            try {
                                takePicturePreviewLauncher.launch(null)
                            } catch (e: Exception) {
                                onOpenGalleryClick()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldDark)
                    ) {
                        Text("Coba Kamera Cepat / Galeri")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCameraHelpDialog = false }) {
                        Text("Tutup", color = Color(0xFF64748B))
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- Pilihan Waktu Santap / Konteks Makanan ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = EmeraldDark,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Waktu Santap Makanan:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                val mealOptions = listOf(
                    "🌅 Sarapan",
                    "🍱 Makan Siang (Bekal)",
                    "🌙 Makan Malam",
                    "🥪 Jajanan Kantin"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    mealOptions.take(2).forEach { meal ->
                        val isSelected = selectedMealType == meal
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) EmeraldContainer else Color(0xFFF1F5F9))
                                .border(
                                    1.dp,
                                    if (isSelected) EmeraldDark else Color(0xFFE2E8F0),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedMealType = meal }
                                .padding(vertical = 8.dp, horizontal = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = meal,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) EmeraldDark else Color(0xFF475569)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    mealOptions.drop(2).forEach { meal ->
                        val isSelected = selectedMealType == meal
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) EmeraldContainer else Color(0xFFF1F5F9))
                                .border(
                                    1.dp,
                                    if (isSelected) EmeraldDark else Color(0xFFE2E8F0),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedMealType = meal }
                                .padding(vertical = 8.dp, horizontal = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = meal,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) EmeraldDark else Color(0xFF475569)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- Presets Contoh Menu Madrasah (Uji Coba Cepat AI) ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Fastfood,
                            contentDescription = null,
                            tint = EmeraldDark,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Contoh Menu Siap Uji Coba AI:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }
                    Text(
                        text = "Ketuk untuk coba",
                        fontSize = 10.sp,
                        color = Color(0xFF64748B)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                val sampleMealPresets = listOf(
                    SampleMealPreset(
                        title = "Bekal Nasi Ayam & Sayur",
                        subtitle = "Nasi, ayam rempah, buncis wortel, tempe & pisang",
                        mealType = "Makan Siang (Bekal)",
                        badge = "Seimbang (Isi Piringku)",
                        badgeColor = Color(0xFFDCFCE7),
                        badgeTextColor = Color(0xFF16A34A),
                        drawableRes = R.drawable.sample_food_balanced
                    ),
                    SampleMealPreset(
                        title = "Gorengan Kantin & Es Teh",
                        subtitle = "Bakwan, tahu isi tepung & es teh manis",
                        mealType = "Jajanan Kantin",
                        badge = "Tinggi Minyak & Gula",
                        badgeColor = Color(0xFFFEF3C7),
                        badgeTextColor = Color(0xFFB45309),
                        drawableRes = R.drawable.sample_food_snack
                    ),
                    SampleMealPreset(
                        title = "Gado-Gado Lontong & Telur",
                        subtitle = "Kangkung, tauge, lontong, tempe & telur rebus",
                        mealType = "Makan Siang (Bekal)",
                        badge = "Kaya Serat & Protein",
                        badgeColor = Color(0xFFEFF6FF),
                        badgeTextColor = Color(0xFF2563EB),
                        drawableRes = R.drawable.img_sayur_buah
                    ),
                    SampleMealPreset(
                        title = "Sarapan Telur & Sayur",
                        subtitle = "Nasi hangat, telur rebus, tumis sayur bergizi",
                        mealType = "Sarapan",
                        badge = "Tinggi Zat Besi & Protein",
                        badgeColor = Color(0xFFFDF2F8),
                        badgeTextColor = Color(0xFFBE185D),
                        drawableRes = R.drawable.img_sarapan_sehat
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    sampleMealPresets.take(2).forEach { sample ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFF8FAFC))
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                                .clickable {
                                    selectedBitmap = loadSampleMealBitmap(context, sample.drawableRes, sample.title)
                                    selectedSampleType = sample.title
                                    selectedMealType = sample.mealType
                                    analysisResult = GeminiService.getSampleFoodAnalysis(sample.title)
                                    isSaved = false
                                }
                                .padding(8.dp)
                        ) {
                            Column {
                                Image(
                                    painter = painterResource(sample.drawableRes),
                                    contentDescription = sample.title,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(65.dp)
                                        .clip(RoundedCornerShape(6.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .background(sample.badgeColor, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = sample.badge,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = sample.badgeTextColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = sample.title,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = sample.subtitle,
                                    fontSize = 9.sp,
                                    color = Color(0xFF64748B),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    lineHeight = 12.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    sampleMealPresets.drop(2).forEach { sample ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFF8FAFC))
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                                .clickable {
                                    selectedBitmap = loadSampleMealBitmap(context, sample.drawableRes, sample.title)
                                    selectedSampleType = sample.title
                                    selectedMealType = sample.mealType
                                    analysisResult = GeminiService.getSampleFoodAnalysis(sample.title)
                                    isSaved = false
                                }
                                .padding(8.dp)
                        ) {
                            Column {
                                Image(
                                    painter = painterResource(sample.drawableRes),
                                    contentDescription = sample.title,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(65.dp)
                                        .clip(RoundedCornerShape(6.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .background(sample.badgeColor, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = sample.badge,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = sample.badgeTextColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = sample.title,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = sample.subtitle,
                                    fontSize = 9.sp,
                                    color = Color(0xFF64748B),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    lineHeight = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- Input Catatan Tambahan Siswa (Bekal/Minuman) ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Catatan Tambahan Bekal/Minuman (Opsional):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = studentNotes,
                    onValueChange = { studentNotes = it },
                    placeholder = {
                        Text(
                            "Contoh: Bekal masak sendiri, bawa tumis kangkung dan minum air mineral 600ml",
                            fontSize = 12.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("food_notes_input"),
                    shape = RoundedCornerShape(8.dp),
                    colors = nutriMindTextFieldColors(),
                    textStyle = nutriMindInputTextStyle(),
                    maxLines = 2
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- Tombol Utama: Analisis Foto Makanan dengan AI ---
        Button(
            onClick = {
                if (selectedBitmap == null) {
                    Toast.makeText(context, "Silakan ambil foto makanan atau pilih contoh menu di atas terlebih dahulu", Toast.LENGTH_LONG).show()
                    return@Button
                }
                isAnalyzing = true
                scope.launch {
                    val result = repository.analyzeFoodImage(selectedBitmap, selectedMealType, studentNotes)
                    analysisResult = result
                    isAnalyzing = false
                }
            },
            enabled = !isAnalyzing,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("analyze_food_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldDark)
        ) {
            if (isAnalyzing) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.5.dp)
                Spacer(modifier = Modifier.width(10.dp))
                Text("NutriMind AI sedang menganalisis foto...", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (analysisResult != null) "Analisis Ulang Foto Ini" else "Analisis Foto Makanan",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }
            }
        }

        // --- TAMPILAN HASIL ANALISIS AI (Jika sudah dianalisis) ---
        if (analysisResult != null) {
            val res = analysisResult!!
            Spacer(modifier = Modifier.height(16.dp))

            // Card 1: Diagram Interaktif Isi Piringku & Porsi
            IsiPiringkuInteractivePlate(
                plateScore = res.plateScore,
                carbsSource = res.carbsSource,
                proteinSource = res.proteinSource,
                vegFruitSource = res.vegFruitSource,
                selectedSection = selectedPlateSection,
                onSectionSelected = { selectedPlateSection = it }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Card 2: Pengatur Skala Porsi Santap
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = EmeraldDark, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Sesuaikan Ukuran Porsi Santapmu:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                        }
                        Text(
                            text = "${(portionMultiplier * 100).toInt()}% Standar",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = EmeraldDark
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            0.5f to "0.5x (Kecil)",
                            1.0f to "1.0x (Standar)",
                            1.5f to "1.5x (Besar)",
                            2.0f to "2.0x (Ganda)"
                        ).forEach { (mult, label) ->
                            val isSelected = portionMultiplier == mult
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) EmeraldContainer else Color(0xFFF1F5F9))
                                    .border(
                                        1.dp,
                                        if (isSelected) EmeraldDark else Color(0xFFE2E8F0),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { portionMultiplier = mult }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) EmeraldDark else Color(0xFF475569)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Card 3: Rincian Lengkap Hasil Analisis Gizi AI
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("food_analysis_result_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Header Hasil
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Hasil Evaluasi Gizi AI",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldDark
                                )
                            )
                            Text(
                                text = "${res.foodCategory} • $selectedMealType",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .background(
                                    if (res.balanceAssessment.contains("Seimbang", ignoreCase = true)) Color(0xFFDCFCE7) else Color(0xFFFEF3C7),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = res.balanceAssessment,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (res.balanceAssessment.contains("Seimbang", ignoreCase = true)) Color(0xFF16A34A) else HealthAmber
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Grid Estimasi 5 Makronutrien (Terskalakan Porsi)
                    val displayedCal = (res.estimatedCalories * portionMultiplier).toInt()
                    val displayedCarb = (res.carbGrams * portionMultiplier).toInt()
                    val displayedProtein = (res.proteinGrams * portionMultiplier).toInt()
                    val displayedFat = (res.fatGrams * portionMultiplier).toInt()
                    val displayedFiber = (res.fiberGrams * portionMultiplier).toInt()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Kandungan Gizi Porsi Santap:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        if (portionMultiplier != 1.0f) {
                            Text(
                                text = "Porsi ${(portionMultiplier * 100).toInt()}%",
                                fontSize = 10.sp,
                                color = EmeraldDark,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        MacroNutrientPill(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.LocalFireDepartment,
                            label = "Energi",
                            value = "$displayedCal kkal",
                            color = Color(0xFFEA580C),
                            bgColor = Color(0xFFFFF7ED)
                        )
                        MacroNutrientPill(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Fastfood,
                            label = "Karbo",
                            value = "${displayedCarb}g",
                            color = Color(0xFF2563EB),
                            bgColor = Color(0xFFEFF6FF)
                        )
                        MacroNutrientPill(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Restaurant,
                            label = "Protein",
                            value = "${displayedProtein}g",
                            color = Color(0xFFDC2626),
                            bgColor = Color(0xFFFEF2F2)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        MacroNutrientPill(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Opacity,
                            label = "Lemak",
                            value = "${displayedFat}g",
                            color = Color(0xFFD97706),
                            bgColor = Color(0xFFFFFBEB)
                        )
                        MacroNutrientPill(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Grass,
                            label = "Serat Pangan",
                            value = "${displayedFiber}g",
                            color = Color(0xFF16A34A),
                            bgColor = Color(0xFFF0FDF4)
                        )
                    }

                    // Card 4: Rincian Item Bahan Makanan Terdeteksi
                    if (res.items.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Rincian Komposisi Bahan Makanan:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                res.items.forEachIndexed { idx, itm ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(itm.name, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
                                            Text("${itm.portion} • ${itm.group}", fontSize = 10.sp, color = Color(0xFF64748B))
                                        }
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFFE2E8F0), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = "~${(itm.calories * portionMultiplier).toInt()} kkal",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = EmeraldDark
                                            )
                                        }
                                    }
                                    if (idx < res.items.size - 1) {
                                        HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 0.8.dp, modifier = Modifier.padding(vertical = 2.dp))
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Card 5: Indeks Kesiapan & Fokus Belajar Madrasah (Alertness & Food Coma Index)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                res.alertnessLevel.contains("Stabil", ignoreCase = true) || res.alertnessLevel.contains("Fokus", ignoreCase = true) -> Color(0xFFF0FDF4)
                                res.alertnessLevel.contains("Sedang", ignoreCase = true) -> Color(0xFFFFFBEB)
                                else -> Color(0xFFFEF2F2)
                            }
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Bolt,
                                        contentDescription = null,
                                        tint = when {
                                            res.alertnessLevel.contains("Stabil", ignoreCase = true) || res.alertnessLevel.contains("Fokus", ignoreCase = true) -> Color(0xFF16A34A)
                                            res.alertnessLevel.contains("Sedang", ignoreCase = true) -> HealthAmber
                                            else -> Color(0xFFDC2626)
                                        },
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Kesiapan & Fokus Belajar",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .background(Color.White, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = res.alertnessLevel,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when {
                                            res.alertnessLevel.contains("Stabil", ignoreCase = true) || res.alertnessLevel.contains("Fokus", ignoreCase = true) -> Color(0xFF16A34A)
                                            res.alertnessLevel.contains("Sedang", ignoreCase = true) -> HealthAmber
                                            else -> Color(0xFFDC2626)
                                        }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = res.alertnessDetail,
                                fontSize = 11.sp,
                                color = Color(0xFF334155),
                                lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Schedule, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Ketahanan Rasa Kenyang: ~${res.satietyHours} Jam",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF0F172A)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Card 6: Skrining Mikronutrien (Zat Besi, Kalsium, Vitamin C)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F2))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Favorite,
                                        contentDescription = null,
                                        tint = Color(0xFFE11D48),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Skrining Zat Besi & Anemia Remaja",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF9F1239)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .background(Color.White, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = res.ironAssessment,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFE11D48)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = res.ironAdvice,
                                fontSize = 11.sp,
                                color = Color(0xFF881337),
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("🦴 Kalsium Tulang", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D4ED8))
                                Text(res.calciumStatus, fontSize = 10.sp, color = Color(0xFF2563EB), fontWeight = FontWeight.Medium)
                            }
                        }
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFDF4FF))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("🍊 Vitamin C Imun", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7E22CE))
                                Text(res.vitaminCStatus, fontSize = 10.sp, color = Color(0xFF9333EA), fontWeight = FontWeight.Medium)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Card 7: Anjuran Hidrasi & Minuman Pendamping
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDFA))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Opacity, contentDescription = null, tint = Color(0xFF0D9488), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Anjuran Hidrasi & Minuman Pendamping:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F766E)
                                )
                                Text(
                                    text = res.hydrationBeverageAdvice,
                                    fontSize = 11.sp,
                                    color = Color(0xFF115E59),
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Card 8: Evaluasi Minyak & Gula
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.WarningAmber,
                                contentDescription = null,
                                tint = HealthAmber,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Evaluasi Minyak & Gula: ${res.oilSugarAssessment}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF92400E)
                                )
                                Text(
                                    text = "Batasi konsumsi gorengan berlebih dan minuman manis untuk menjaga konsentrasi belajar serta berat badan ideal.",
                                    fontSize = 11.sp,
                                    color = Color(0xFFB45309),
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Card 9: Saran Perbaikan Piring Konkret
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Saran Perbaikan Piring Selanjutnya:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E40AF)
                                )
                                Text(
                                    text = res.actionableImprovement,
                                    fontSize = 11.sp,
                                    color = Color(0xFF1D4ED8),
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Card 10: Edukasi Gizi Madrasah Kemenkes
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = EmeraldContainer.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Shield, contentDescription = null, tint = EmeraldDark, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Edukasi Gizi Madrasah Kemenkes:",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldDark
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = res.educationalFeedback,
                                fontSize = 12.sp,
                                color = Color(0xFF0F766E),
                                lineHeight = 18.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Tombol Simpan ke Riwayat Makanan
                    Button(
                        onClick = {
                            val student = currentUser ?: return@Button
                            scope.launch {
                                val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                                val foodEntity = FoodScanEntity(
                                    studentId = student.id,
                                    imagePath = selectedSampleType ?: "Foto Makanan",
                                    detectedFoods = res.detectedFoods,
                                    foodCategory = "${res.foodCategory} • $selectedMealType (Porsi ${(portionMultiplier * 100).toInt()}%)",
                                    carbsSource = res.carbsSource,
                                    proteinSource = res.proteinSource,
                                    vegFruitSource = res.vegFruitSource,
                                    approxNutrients = "$displayedCal kkal | K:${displayedCarb}g, P:${displayedProtein}g, L:${displayedFat}g, Serat:${displayedFiber}g",
                                    balanceAssessment = "${res.balanceAssessment} (Skor: ${res.plateScore}/100)",
                                    educationalFeedback = "${res.educationalFeedback}\n\n⚡ Indeks Fokus: ${res.alertnessLevel}\n💡 Saran Perbaikan: ${res.actionableImprovement}\n🩸 Info Zat Besi: ${res.ironAdvice}\n💧 Minuman: ${res.hydrationBeverageAdvice}",
                                    dateString = dateStr
                                )
                                repository.saveFoodScan(foodEntity)
                                isSaved = true
                                Toast.makeText(context, "Berhasil disimpan ke riwayat makanan!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("save_food_history_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSaved) Color(0xFF16A34A) else EmeraldDark
                        )
                    ) {
                        if (isSaved) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Tersimpan di Riwayat Foto Makanan", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Text("Simpan ke Riwayat Makanan", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            // --- TEKS PANDUAN BAWAH YANG DIRAPIKAN (Saat belum ada hasil analisis) ---
            Spacer(modifier = Modifier.height(14.dp))

            // Card 1: Panduan Mengambil Foto Makanan Optimal
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(EmeraldContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = EmeraldDark, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Panduan Foto Makanan Jelas",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    PhotoGuideTipItem(
                        number = "1",
                        title = "Pencahayaan Cukup",
                        description = "Pastikan makanan terlihat terang agar warna sayur dan jenis lauk dapat dikenali dengan akurat."
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    PhotoGuideTipItem(
                        number = "2",
                        title = "Sudut Pengambilan Atas (45° - 90°)",
                        description = "Arahkan kamera mencakup seluruh piring, mangkuk, atau wadah bekal sekolahmu."
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    PhotoGuideTipItem(
                        number = "3",
                        title = "Sertakan Seluruh Porsi",
                        description = "Tampilkan nasi/pokok, lauk pauk, sayur, buah, serta minuman pendamping yang hendak kamu santap."
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Card 2: Pedoman Edukasi Gizi "Isi Piringku" Madrasah
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0xFFFEF3C7), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Restaurant, contentDescription = null, tint = HealthAmber, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Konsep 'Isi Piringku' Kemenkes",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Pedoman porsi seimbang dalam satu piring makan remaja madrasah:",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Baris 1: Makanan Pokok & Lauk Pauk
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PlateConceptCard(
                            modifier = Modifier.weight(1f),
                            portion = "2/3 dari 1/2 Piring",
                            label = "Makanan Pokok",
                            desc = "Karbohidrat kompleks (Nasi, Jagung, Ubi)",
                            bgColor = Color(0xFFEFF6FF),
                            accentColor = Color(0xFF1E40AF),
                            icon = Icons.Default.Grain
                        )
                        PlateConceptCard(
                            modifier = Modifier.weight(1f),
                            portion = "1/3 dari 1/2 Piring",
                            label = "Lauk Pauk",
                            desc = "Protein hewani & nabati (Ayam, Ikan, Telur, Tempe)",
                            bgColor = Color(0xFFFEF2F2),
                            accentColor = Color(0xFF991B1B),
                            icon = Icons.Default.Restaurant
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Baris 2: Sayuran & Buah-buahan
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PlateConceptCard(
                            modifier = Modifier.weight(1f),
                            portion = "2/3 dari 1/2 Piring",
                            label = "Sayuran",
                            desc = "Serat pangan, mineral & antioksidan",
                            bgColor = Color(0xFFF0FDF4),
                            accentColor = Color(0xFF166534),
                            icon = Icons.Default.Eco
                        )
                        PlateConceptCard(
                            modifier = Modifier.weight(1f),
                            portion = "1/3 dari 1/2 Piring",
                            label = "Buah-buahan",
                            desc = "Vitamin alami, cairan & enzim",
                            bgColor = Color(0xFFFFFBEB),
                            accentColor = Color(0xFFB45309),
                            icon = Icons.Default.Star
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Mandatory Disclaimer Card
        NutriMindDisclaimerCard()

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun FoodNutrientItem(
    icon: ImageVector,
    label: String,
    value: String,
    badgeColor: Color,
    iconTint: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(badgeColor, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 11.sp, color = Color(0xFF64748B))
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
        }
    }
}

@Composable
fun PhotoGuideTipItem(
    number: String,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .background(EmeraldContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(number, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldDark)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
            Text(description, fontSize = 11.sp, color = Color(0xFF64748B), lineHeight = 16.sp)
        }
    }
}

@Composable
fun PlateConceptCard(
    modifier: Modifier = Modifier,
    portion: String,
    label: String,
    desc: String,
    bgColor: Color,
    accentColor: Color,
    icon: ImageVector
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .background(accentColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = portion,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                }
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
            Text(
                text = desc,
                fontSize = 10.sp,
                color = Color(0xFF64748B),
                lineHeight = 13.sp
            )
        }
    }
}

@Composable
fun PlateConceptPill(
    modifier: Modifier = Modifier,
    title: String,
    label: String,
    desc: String,
    bgColor: Color,
    textColor: Color
) {
    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(10.dp))
            .padding(vertical = 8.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = textColor)
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
            Text(desc, fontSize = 9.sp, color = Color(0xFF64748B))
        }
    }
}

@Composable
fun MacroNutrientPill(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    color: Color,
    bgColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF64748B)
            )
        }
    }
}

private fun decodeBitmapFromUri(context: Context, uri: Uri): Bitmap? {
    return try {
        val boundsOptions = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, boundsOptions)
        }

        var sampleSize = 1
        val maxDimension = 1280
        val width = boundsOptions.outWidth
        val height = boundsOptions.outHeight
        if (width > maxDimension || height > maxDimension) {
            val halfW = width / 2
            val halfH = height / 2
            while ((halfW / sampleSize) >= maxDimension && (halfH / sampleSize) >= maxDimension) {
                sampleSize *= 2
            }
        }

        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val rawBitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, decodeOptions)
        } ?: return null

        var orientation = ExifInterface.ORIENTATION_NORMAL
        try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val exif = ExifInterface(stream)
                orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
            }
        } catch (_: Exception) {}

        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        }

        if (!matrix.isIdentity) {
            Bitmap.createBitmap(rawBitmap, 0, 0, rawBitmap.width, rawBitmap.height, matrix, true)
        } else {
            rawBitmap
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private fun decodeAndProcessCapturedPhoto(filePath: String): Bitmap? {
    return try {
        val boundsOptions = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(filePath, boundsOptions)

        var sampleSize = 1
        val maxDimension = 1280
        val width = boundsOptions.outWidth
        val height = boundsOptions.outHeight
        if (width > maxDimension || height > maxDimension) {
            val halfW = width / 2
            val halfH = height / 2
            while ((halfW / sampleSize) >= maxDimension && (halfH / sampleSize) >= maxDimension) {
                sampleSize *= 2
            }
        }

        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val rawBitmap = BitmapFactory.decodeFile(filePath, decodeOptions) ?: return null

        val exif = ExifInterface(filePath)
        val orientation = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        }

        if (!matrix.isIdentity) {
            Bitmap.createBitmap(rawBitmap, 0, 0, rawBitmap.width, rawBitmap.height, matrix, true)
        } else {
            rawBitmap
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

data class SampleMealPreset(
    val title: String,
    val subtitle: String,
    val mealType: String,
    val badge: String,
    val badgeColor: Color,
    val badgeTextColor: Color,
    val drawableRes: Int
)

fun loadSampleMealBitmap(context: Context, drawableRes: Int, fallbackTitle: String): Bitmap {
    return try {
        val options = BitmapFactory.Options().apply {
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        BitmapFactory.decodeResource(context.resources, drawableRes, options) ?: createSampleMealBitmap(fallbackTitle)
    } catch (e: Exception) {
        createSampleMealBitmap(fallbackTitle)
    }
}

fun createSampleMealBitmap(mealName: String): Bitmap {
    val width = 480
    val height = 480
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)

    val bgPaint = android.graphics.Paint().apply {
        color = android.graphics.Color.rgb(248, 250, 252)
        style = android.graphics.Paint.Style.FILL
    }
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

    val plateBorderPaint = android.graphics.Paint().apply {
        color = android.graphics.Color.rgb(226, 232, 240)
        style = android.graphics.Paint.Style.FILL
        isAntiAlias = true
    }
    canvas.drawCircle(width / 2f, height / 2f, 210f, plateBorderPaint)

    val plateInnerPaint = android.graphics.Paint().apply {
        color = android.graphics.Color.rgb(255, 255, 255)
        style = android.graphics.Paint.Style.FILL
        isAntiAlias = true
    }
    canvas.drawCircle(width / 2f, height / 2f, 195f, plateInnerPaint)

    val rect = android.graphics.RectF(55f, 55f, 425f, 425f)
    val arcPaint = android.graphics.Paint().apply {
        style = android.graphics.Paint.Style.FILL
        isAntiAlias = true
    }

    // Karbo (Pokok) - Yellow-Orange
    arcPaint.color = android.graphics.Color.rgb(254, 215, 170)
    canvas.drawArc(rect, 180f, 120f, true, arcPaint)

    // Protein (Lauk) - Red-Rose
    arcPaint.color = android.graphics.Color.rgb(254, 202, 202)
    canvas.drawArc(rect, 300f, 60f, true, arcPaint)

    // Sayuran - Green
    arcPaint.color = android.graphics.Color.rgb(187, 247, 208)
    canvas.drawArc(rect, 0f, 120f, true, arcPaint)

    // Buah - Orange
    arcPaint.color = android.graphics.Color.rgb(253, 230, 138)
    canvas.drawArc(rect, 120f, 60f, true, arcPaint)

    val centerPaint = android.graphics.Paint().apply {
        color = android.graphics.Color.WHITE
        style = android.graphics.Paint.Style.FILL
        isAntiAlias = true
    }
    canvas.drawCircle(width / 2f, height / 2f, 60f, centerPaint)

    val textPaint = android.graphics.Paint().apply {
        color = android.graphics.Color.rgb(15, 23, 42)
        textSize = 20f
        textAlign = android.graphics.Paint.Align.CENTER
        isFakeBoldText = true
        isAntiAlias = true
    }
    canvas.drawText("Isi Piringku", width / 2f, height / 2f - 4f, textPaint)

    val subTextPaint = android.graphics.Paint().apply {
        color = android.graphics.Color.rgb(5, 150, 105)
        textSize = 13f
        textAlign = android.graphics.Paint.Align.CENTER
        isFakeBoldText = true
        isAntiAlias = true
    }
    canvas.drawText("KEMENKES", width / 2f, height / 2f + 16f, subTextPaint)

    val titlePaint = android.graphics.Paint().apply {
        color = android.graphics.Color.rgb(30, 41, 59)
        textSize = 17f
        textAlign = android.graphics.Paint.Align.CENTER
        isFakeBoldText = true
        isAntiAlias = true
    }
    canvas.drawText(mealName, width / 2f, height - 16f, titlePaint)

    return bitmap
}

@Composable
fun IsiPiringkuInteractivePlate(
    plateScore: Int,
    carbsSource: String,
    proteinSource: String,
    vegFruitSource: String,
    selectedSection: String,
    onSectionSelected: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Restaurant,
                        contentDescription = null,
                        tint = EmeraldDark,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Diagram Visual 'Isi Piringku'",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                }

                Box(
                    modifier = Modifier
                        .background(
                            when {
                                plateScore >= 80 -> Color(0xFFDCFCE7)
                                plateScore >= 60 -> Color(0xFFFEF3C7)
                                else -> Color(0xFFFEE2E2)
                            },
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "Skor: $plateScore/100",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = when {
                            plateScore >= 80 -> Color(0xFF16A34A)
                            plateScore >= 60 -> HealthAmber
                            else -> Color(0xFFDC2626)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Canvas drawing the 4 sectors of Isi Piringku
            Box(
                modifier = Modifier.size(180.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(180.dp)) {
                    val strokeRim = 4.dp.toPx()
                    val diameter = size.minDimension - strokeRim * 2
                    val arcSize = Size(diameter, diameter)
                    val topLeft = Offset(strokeRim, strokeRim)

                    // Plate outer rim
                    drawCircle(
                        color = Color(0xFFE2E8F0),
                        radius = size.minDimension / 2f
                    )
                    drawCircle(
                        color = Color.White,
                        radius = (size.minDimension / 2f) - strokeRim
                    )

                    // 1. Makanan Pokok (2/3 dari 1/2 = 120°): Blue
                    drawArc(
                        color = if (selectedSection == "all" || selectedSection == "pokok") Color(0xFF3B82F6) else Color(0xFF93C5FD).copy(alpha = 0.45f),
                        startAngle = 180f,
                        sweepAngle = 120f,
                        useCenter = true,
                        topLeft = topLeft,
                        size = arcSize
                    )

                    // 2. Lauk Pauk (1/3 dari 1/2 = 60°): Red
                    drawArc(
                        color = if (selectedSection == "all" || selectedSection == "lauk") Color(0xFFEF4444) else Color(0xFFFCA5A5).copy(alpha = 0.45f),
                        startAngle = 300f,
                        sweepAngle = 60f,
                        useCenter = true,
                        topLeft = topLeft,
                        size = arcSize
                    )

                    // 3. Sayuran (2/3 dari 1/2 = 120°): Emerald Green
                    drawArc(
                        color = if (selectedSection == "all" || selectedSection == "sayur") Color(0xFF10B981) else Color(0xFF6EE7B7).copy(alpha = 0.45f),
                        startAngle = 0f,
                        sweepAngle = 120f,
                        useCenter = true,
                        topLeft = topLeft,
                        size = arcSize
                    )

                    // 4. Buah-buahan (1/3 dari 1/2 = 60°): Orange
                    drawArc(
                        color = if (selectedSection == "all" || selectedSection == "buah") Color(0xFFF59E0B) else Color(0xFFFDE68A).copy(alpha = 0.45f),
                        startAngle = 120f,
                        sweepAngle = 60f,
                        useCenter = true,
                        topLeft = topLeft,
                        size = arcSize
                    )

                    // Center disc
                    drawCircle(
                        color = Color.White,
                        radius = diameter * 0.22f
                    )
                    drawCircle(
                        color = Color(0xFFE2E8F0),
                        radius = diameter * 0.22f,
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Isi Piring", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EmeraldDark)
                    Text("KEMENKES", fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF64748B))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Interactive Selector Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(
                    "all" to "Semua",
                    "pokok" to "🌾 Pokok",
                    "lauk" to "🍗 Lauk",
                    "sayur" to "🥦 Sayur",
                    "buah" to "🍎 Buah"
                ).forEach { (key, label) ->
                    val isSelected = selectedSection == key
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) EmeraldContainer else Color(0xFFF1F5F9))
                            .border(
                                1.dp,
                                if (isSelected) EmeraldDark else Color(0xFFE2E8F0),
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { onSectionSelected(key) }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) EmeraldDark else Color(0xFF475569)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Detail penjelasan section yang dipilih
            val (sectionTitle, sectionDesc, detectedItem) = when (selectedSection) {
                "pokok" -> Triple(
                    "Makanan Pokok (2/3 dari 1/2 piring):",
                    "Sumber energi utama berbasis karbohidrat kompleks. Dianjurkan nasi merah, jagung, ubi, atau kentang.",
                    carbsSource
                )
                "lauk" -> Triple(
                    "Lauk Pauk (1/3 dari 1/2 piring):",
                    "Sumber protein hewani & nabati pembangun sel tubuh dan pencegah anemia (ikan, telur, tahu, tempe, daging).",
                    proteinSource
                )
                "sayur" -> Triple(
                    "Sayur-Mayur (2/3 dari 1/2 piring):",
                    "Sumber serat pangan, vitamin, dan mineral untuk imunitas serta menjaga pencernaan sehat.",
                    vegFruitSource
                )
                "buah" -> Triple(
                    "Buah-Buahan (1/3 dari 1/2 piring):",
                    "Penyedia vitamin C, antioksidan, dan cairan alami penunjang kebugaran belajar.",
                    "Sertakan buah potong segar seperti pisang, pepaya, jeruk, atau semangka."
                )
                else -> Triple(
                    "Konsep Standar Gizi Seimbang Kemenkes:",
                    "Setengah piring terdiri atas sayur & buah, setengah piring lainnya makanan pokok & lauk pauk.",
                    "Karbo: $carbsSource | Protein: $proteinSource | Sayur/Buah: $vegFruitSource"
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(text = sectionTitle, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    Text(text = sectionDesc, fontSize = 10.sp, color = Color(0xFF64748B), lineHeight = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Terdeteksi: $detectedItem",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = EmeraldDark
                    )
                }
            }
        }
    }
}
