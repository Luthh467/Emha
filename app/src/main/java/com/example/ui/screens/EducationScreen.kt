package com.example.ui.screens

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.AppRepository
import com.example.data.model.EducationArticle
import com.example.ui.components.NutriMindDisclaimerCard
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.HealthAmber
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EducationScreen(
    repository: AppRepository
) {
    val context = LocalContext.current
    val currentUser by repository.currentUser.collectAsState()
    val studentId = currentUser?.id ?: ""
    val latestCheck by repository.getLatestNutritionCheck(studentId).collectAsState(initial = null)
    val articles by repository.getAllArticles().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Semua") }
    var showOnlyFavorites by remember { mutableStateOf(false) }
    var selectedArticleForRead by remember { mutableStateOf<EducationArticle?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val categories = listOf(
        "Semua",
        "Gizi Seimbang",
        "Sarapan",
        "Buah dan Sayur",
        "Makanan dan Minuman",
        "Aktivitas Fisik",
        "Tidur & Sehat"
    )

    // Filter articles
    val filteredArticles = articles.filter { article ->
        val matchesSearch = article.title.contains(searchQuery, ignoreCase = true) ||
                article.summary.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedCategory == "Semua" || article.category == selectedCategory
        val matchesFavorite = !showOnlyFavorites || article.isFavorite
        matchesSearch && matchesCategory && matchesFavorite
    }

    // Recommended article based on student's BMI category
    val recommendedArticle = remember(latestCheck, articles) {
        val bmiCat = latestCheck?.bmiCategory.orEmpty()
        articles.firstOrNull {
            it.recommendedForStatus.equals(bmiCat, ignoreCase = true) ||
                    (bmiCat.contains("Kurang", ignoreCase = true) && it.category == "Sarapan") ||
                    (bmiCat.contains("Lebih", ignoreCase = true) && it.category == "Makanan dan Minuman")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp)
    ) {
        Text(
            text = "Edukasi Gizi Madrasah",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = EmeraldDark
            )
        )
        Text(
            text = "Panduan praktis gizi seimbang, sarapan, dan pola hidup sehat bagi pelajar madrasah.",
            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B)),
            modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
        )

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Cari topik gizi (misal: sarapan, piringku, gula)...") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = EmeraldDark)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Hapus")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("education_search_input"),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                cursorColor = Color.Black,
                focusedBorderColor = EmeraldDark,
                unfocusedBorderColor = Color(0xFFCBD5E1),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Horizontal Category Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = showOnlyFavorites,
                onClick = { showOnlyFavorites = !showOnlyFavorites },
                label = { Text("Favorit Saya ★") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = HealthAmber,
                    selectedLabelColor = Color.White
                )
            )

            categories.forEach { cat ->
                FilterChip(
                    selected = selectedCategory == cat && !showOnlyFavorites,
                    onClick = {
                        selectedCategory = cat
                        showOnlyFavorites = false
                    },
                    label = { Text(cat) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = EmeraldDark,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Recommended Banner if available
        if (recommendedArticle != null && searchQuery.isEmpty() && selectedCategory == "Semua" && !showOnlyFavorites) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedArticleForRead = recommendedArticle }
                    .testTag("recommended_article_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = EmeraldContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(getCategoryDrawableRes(recommendedArticle.category))
                            .crossfade(true)
                            .placeholder(R.drawable.ic_placeholder_article)
                            .error(R.drawable.ic_placeholder_article)
                            .build(),
                        contentDescription = recommendedArticle.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(76.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = EmeraldDark,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Rekomendasi Sesuai Status Gizimu",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = EmeraldDark
                            )
                        }
                        Text(
                            text = recommendedArticle.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF0F172A),
                            maxLines = 2,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                        Text(
                            text = "${recommendedArticle.readTimeMinutes} menit baca • Kategori ${recommendedArticle.category}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF334155)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Articles List
        if (filteredArticles.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tidak ada artikel yang cocok",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF475569)
                    )
                    Text(
                        text = "Coba ubah kata kunci pencarian atau kategori filter.",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredArticles, key = { it.id }) { article ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedArticleForRead = article }
                            .testTag("article_card_${article.id}"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(136.dp)
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(getCategoryDrawableRes(article.category))
                                        .crossfade(true)
                                        .placeholder(R.drawable.ic_placeholder_article)
                                        .error(R.drawable.ic_placeholder_article)
                                        .build(),
                                    contentDescription = article.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(136.dp)
                                        .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                                )

                                // Category badge overlay on top-left of the image
                                Box(
                                    modifier = Modifier
                                        .padding(10.dp)
                                        .align(Alignment.TopStart)
                                        .background(Color.White.copy(alpha = 0.95f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = article.category,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldDark
                                    )
                                }

                                // Favorite bookmark button on top-right of the image
                                IconButton(
                                    onClick = {
                                        scope.launch {
                                            repository.toggleArticleFavorite(article.id, !article.isFavorite)
                                        }
                                    },
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .align(Alignment.TopEnd)
                                        .background(Color.White.copy(alpha = 0.92f), CircleShape)
                                        .size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = if (article.isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                        contentDescription = "Simpan Favorit",
                                        tint = if (article.isFavorite) HealthAmber else Color(0xFF64748B),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = article.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF0F172A),
                                    lineHeight = 20.sp
                                )

                                Text(
                                    text = article.summary,
                                    fontSize = 12.sp,
                                    color = Color(0xFF475569),
                                    lineHeight = 17.sp,
                                    modifier = Modifier.padding(top = 5.dp, bottom = 10.dp)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Timer,
                                            contentDescription = null,
                                            tint = Color(0xFF64748B),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${article.readTimeMinutes} menit baca",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF64748B)
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Baca Artikel",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldDark
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Icon(
                                            imageVector = Icons.Default.ArrowForward,
                                            contentDescription = null,
                                            tint = EmeraldDark,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
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
    }

    // Modal Bottom Sheet Detail Bacaan Artikel
    if (selectedArticleForRead != null) {
        val reading = selectedArticleForRead!!
        ModalBottomSheet(
            onDismissRequest = { selectedArticleForRead = null },
            sheetState = sheetState,
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                // Coil Hero Image for the Article
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(getCategoryDrawableRes(reading.category))
                            .crossfade(true)
                            .placeholder(R.drawable.ic_placeholder_article)
                            .error(R.drawable.ic_placeholder_article)
                            .build(),
                        contentDescription = reading.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(EmeraldContainer, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = reading.category,
                            color = EmeraldDark,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(onClick = {
                        scope.launch {
                            repository.toggleArticleFavorite(reading.id, !reading.isFavorite)
                            selectedArticleForRead = reading.copy(isFavorite = !reading.isFavorite)
                        }
                    }) {
                        Icon(
                            imageVector = if (reading.isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Favorit",
                            tint = if (reading.isFavorite) HealthAmber else Color(0xFF94A3B8)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = reading.title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                )

                Row(
                    modifier = Modifier.padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Estimasi membaca: ${reading.readTimeMinutes} menit",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF64748B)
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0xFFE2E8F0))
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = reading.content,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF1E293B),
                        fontSize = 14.sp,
                        lineHeight = 22.sp
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))
                NutriMindDisclaimerCard()
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

private fun getCategoryDrawableRes(category: String): Int {
    return when (category) {
        "Gizi Seimbang" -> R.drawable.img_isi_piringku
        "Sarapan" -> R.drawable.img_sarapan_sehat
        "Buah dan Sayur" -> R.drawable.img_sayur_buah
        "Makanan dan Minuman" -> R.drawable.img_minuman_sehat
        "Aktivitas Fisik" -> R.drawable.img_aktivitas_fisik
        "Tidur & Sehat" -> R.drawable.img_tidur_sehat
        else -> R.drawable.banner_nutrimind
    }
}
