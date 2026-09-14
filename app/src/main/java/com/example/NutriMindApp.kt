package com.example

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppRepository
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.CompleteProfileScreen
import com.example.ui.screens.DailyCheckScreen
import com.example.ui.screens.EducationScreen
import com.example.ui.screens.FoodScanScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.NutritionCheckScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.UksDashboardScreen
import com.example.ui.theme.EmeraldDark

sealed class Screen(val route: String, val title: String) {
    object Auth : Screen("auth", "Masuk")
    object CompleteProfile : Screen("complete_profile", "Lengkapi Profil")
    object Home : Screen("home", "NutriMind AI")
    object NutritionCheck : Screen("nutrition_check", "Cek Gizi")
    object FoodScan : Screen("food_scan", "Foto Makanan")
    object DailyCheck : Screen("daily_check", "Cek Harian")
    object Education : Screen("education", "Belajar Gizi")
    object Chat : Screen("chat", "Tanya NutriMind AI")
    object History : Screen("history", "Riwayat")
    object Profile : Screen("profile", "Profil Siswa")
    object UksDashboard : Screen("uks_dashboard", "Dashboard UKS")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutriMindApp() {
    val context = LocalContext.current
    val repository = remember { AppRepository(context) }
    val currentUser by repository.currentUser.collectAsState()

    var currentScreen by remember { mutableStateOf<Screen>(Screen.Auth) }

    // Check auth state
    if (currentUser == null && currentScreen != Screen.Auth) {
        currentScreen = Screen.Auth
    }

    Scaffold(
        topBar = {
            if (currentScreen !in listOf(Screen.Auth, Screen.CompleteProfile, Screen.UksDashboard)) {
                TopAppBar(
                    title = {
                        Text(
                            text = currentScreen.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                    },
                    navigationIcon = {
                        if (currentScreen !in listOf(Screen.Home)) {
                            IconButton(onClick = { currentScreen = Screen.Home }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                            }
                        }
                    },
                    actions = {
                        if (currentScreen != Screen.Chat) {
                            IconButton(onClick = { currentScreen = Screen.Chat }, modifier = Modifier.testTag("top_chat_button")) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = "Tanya AI", tint = Color.White)
                            }
                        }
                        if (currentScreen != Screen.History) {
                            IconButton(onClick = { currentScreen = Screen.History }, modifier = Modifier.testTag("top_history_button")) {
                                Icon(Icons.Default.History, contentDescription = "Riwayat", tint = Color.White)
                            }
                        }
                        if (currentScreen != Screen.Profile) {
                            IconButton(onClick = { currentScreen = Screen.Profile }, modifier = Modifier.testTag("top_profile_button")) {
                                Icon(Icons.Default.AccountCircle, contentDescription = "Profil", tint = Color.White)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = EmeraldDark)
                )
            }
        },
        bottomBar = {
            if (currentScreen !in listOf(Screen.Auth, Screen.CompleteProfile, Screen.UksDashboard)) {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = currentScreen == Screen.Home,
                        onClick = { currentScreen = Screen.Home },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Beranda", modifier = Modifier.size(20.dp)) },
                        label = { Text("Beranda", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = EmeraldDark,
                            selectedTextColor = EmeraldDark,
                            indicatorColor = Color(0xFFCCFBF1)
                        )
                    )

                    NavigationBarItem(
                        selected = currentScreen == Screen.NutritionCheck,
                        onClick = { currentScreen = Screen.NutritionCheck },
                        icon = { Icon(Icons.Default.MonitorWeight, contentDescription = "Cek Gizi", modifier = Modifier.size(20.dp)) },
                        label = { Text("Cek Gizi", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = EmeraldDark,
                            selectedTextColor = EmeraldDark,
                            indicatorColor = Color(0xFFCCFBF1)
                        )
                    )

                    NavigationBarItem(
                        selected = currentScreen == Screen.FoodScan,
                        onClick = { currentScreen = Screen.FoodScan },
                        icon = { Icon(Icons.Default.CameraAlt, contentDescription = "Foto Makanan", modifier = Modifier.size(20.dp)) },
                        label = { Text("Foto Makanan", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = EmeraldDark,
                            selectedTextColor = EmeraldDark,
                            indicatorColor = Color(0xFFCCFBF1)
                        )
                    )

                    NavigationBarItem(
                        selected = currentScreen == Screen.DailyCheck,
                        onClick = { currentScreen = Screen.DailyCheck },
                        icon = { Icon(Icons.Default.Today, contentDescription = "Cek Harian", modifier = Modifier.size(20.dp)) },
                        label = { Text("Cek Harian", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = EmeraldDark,
                            selectedTextColor = EmeraldDark,
                            indicatorColor = Color(0xFFCCFBF1)
                        )
                    )

                    NavigationBarItem(
                        selected = currentScreen == Screen.Education,
                        onClick = { currentScreen = Screen.Education },
                        icon = { Icon(Icons.Default.MenuBook, contentDescription = "Belajar Gizi", modifier = Modifier.size(20.dp)) },
                        label = { Text("Edukasi", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = EmeraldDark,
                            selectedTextColor = EmeraldDark,
                            indicatorColor = Color(0xFFCCFBF1)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                Screen.Auth -> AuthScreen(
                    repository = repository,
                    onStudentLoggedIn = { needsProfile ->
                        currentScreen = if (needsProfile) Screen.CompleteProfile else Screen.Home
                    },
                    onUksLoggedIn = {
                        currentScreen = Screen.UksDashboard
                    }
                )

                Screen.CompleteProfile -> CompleteProfileScreen(
                    repository = repository,
                    onProfileCompleted = {
                        currentScreen = Screen.Home
                    },
                    onLogout = {
                        repository.setCurrentUser(null)
                        currentScreen = Screen.Auth
                    }
                )

                Screen.Home -> HomeScreen(
                    repository = repository,
                    onNavigateTo = { route ->
                        currentScreen = when (route) {
                            "nutrition_check" -> Screen.NutritionCheck
                            "food_scan" -> Screen.FoodScan
                            "daily_check" -> Screen.DailyCheck
                            "education" -> Screen.Education
                            "chat" -> Screen.Chat
                            "history" -> Screen.History
                            "profile" -> Screen.Profile
                            else -> Screen.Home
                        }
                    }
                )

                Screen.NutritionCheck -> NutritionCheckScreen(
                    repository = repository,
                    onFinished = {
                        currentScreen = Screen.History
                    }
                )

                Screen.FoodScan -> FoodScanScreen(
                    repository = repository,
                    onNavigateToHistory = {
                        currentScreen = Screen.History
                    }
                )

                Screen.DailyCheck -> DailyCheckScreen(
                    repository = repository,
                    onFinished = {
                        currentScreen = Screen.Home
                    }
                )

                Screen.Education -> EducationScreen(
                    repository = repository
                )

                Screen.Chat -> ChatScreen(
                    repository = repository
                )

                Screen.History -> HistoryScreen(
                    repository = repository
                )

                Screen.Profile -> ProfileScreen(
                    repository = repository,
                    onEditProfile = {
                        currentScreen = Screen.CompleteProfile
                    },
                    onLogout = {
                        currentScreen = Screen.Auth
                    }
                )

                Screen.UksDashboard -> UksDashboardScreen(
                    repository = repository,
                    onLogout = {
                        repository.setCurrentUser(null)
                        currentScreen = Screen.Auth
                    }
                )
            }
        }
    }
}
