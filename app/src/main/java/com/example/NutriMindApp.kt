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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
    val isSessionLoading by repository.isSessionLoading.collectAsState()

    var currentScreenRoute by rememberSaveable { mutableStateOf(Screen.Auth.route) }

    val currentScreen = when (currentScreenRoute) {
        Screen.CompleteProfile.route -> Screen.CompleteProfile
        Screen.Home.route -> Screen.Home
        Screen.NutritionCheck.route -> Screen.NutritionCheck
        Screen.FoodScan.route -> Screen.FoodScan
        Screen.DailyCheck.route -> Screen.DailyCheck
        Screen.Education.route -> Screen.Education
        Screen.Chat.route -> Screen.Chat
        Screen.History.route -> Screen.History
        Screen.Profile.route -> Screen.Profile
        Screen.UksDashboard.route -> Screen.UksDashboard
        else -> Screen.Auth
    }

    // If user is logged in and on Auth screen, route forward
    LaunchedEffect(currentUser) {
        if (currentUser != null && currentScreenRoute == Screen.Auth.route) {
            currentScreenRoute = if (currentUser?.role == "UKS") {
                Screen.UksDashboard.route
            } else if (currentUser?.isProfileComplete == false) {
                Screen.CompleteProfile.route
            } else {
                Screen.Home.route
            }
        }
    }

    // Only redirect to Auth if finished loading and no active session exists
    if (!isSessionLoading && currentUser == null && !repository.hasActiveSession && currentScreenRoute != Screen.Auth.route) {
        currentScreenRoute = Screen.Auth.route
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
                            IconButton(onClick = { currentScreenRoute = Screen.Home.route }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                            }
                        }
                    },
                    actions = {
                        if (currentScreen != Screen.Chat) {
                            IconButton(onClick = { currentScreenRoute = Screen.Chat.route }, modifier = Modifier.testTag("top_chat_button")) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = "Tanya AI", tint = Color.White)
                            }
                        }
                        if (currentScreen != Screen.History) {
                            IconButton(onClick = { currentScreenRoute = Screen.History.route }, modifier = Modifier.testTag("top_history_button")) {
                                Icon(Icons.Default.History, contentDescription = "Riwayat", tint = Color.White)
                            }
                        }
                        if (currentScreen != Screen.Profile) {
                            IconButton(onClick = { currentScreenRoute = Screen.Profile.route }, modifier = Modifier.testTag("top_profile_button")) {
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
                        onClick = { currentScreenRoute = Screen.Home.route },
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
                        onClick = { currentScreenRoute = Screen.NutritionCheck.route },
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
                        onClick = { currentScreenRoute = Screen.FoodScan.route },
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
                        onClick = { currentScreenRoute = Screen.DailyCheck.route },
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
                        onClick = { currentScreenRoute = Screen.Education.route },
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
                        currentScreenRoute = if (needsProfile) Screen.CompleteProfile.route else Screen.Home.route
                    },
                    onUksLoggedIn = {
                        currentScreenRoute = Screen.UksDashboard.route
                    }
                )

                Screen.CompleteProfile -> CompleteProfileScreen(
                    repository = repository,
                    onProfileCompleted = {
                        currentScreenRoute = Screen.Home.route
                    },
                    onLogout = {
                        repository.setCurrentUser(null)
                        currentScreenRoute = Screen.Auth.route
                    }
                )

                Screen.Home -> HomeScreen(
                    repository = repository,
                    onNavigateTo = { route ->
                        currentScreenRoute = when (route) {
                            "nutrition_check" -> Screen.NutritionCheck.route
                            "food_scan" -> Screen.FoodScan.route
                            "daily_check" -> Screen.DailyCheck.route
                            "education" -> Screen.Education.route
                            "chat" -> Screen.Chat.route
                            "history" -> Screen.History.route
                            "profile" -> Screen.Profile.route
                            else -> Screen.Home.route
                        }
                    }
                )

                Screen.NutritionCheck -> NutritionCheckScreen(
                    repository = repository,
                    onFinished = {
                        currentScreenRoute = Screen.History.route
                    }
                )

                Screen.FoodScan -> FoodScanScreen(
                    repository = repository,
                    onNavigateToHistory = {
                        currentScreenRoute = Screen.History.route
                    }
                )

                Screen.DailyCheck -> DailyCheckScreen(
                    repository = repository,
                    onFinished = {
                        currentScreenRoute = Screen.Home.route
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
                        currentScreenRoute = Screen.CompleteProfile.route
                    },
                    onLogout = {
                        repository.setCurrentUser(null)
                        currentScreenRoute = Screen.Auth.route
                    }
                )

                Screen.UksDashboard -> UksDashboardScreen(
                    repository = repository,
                    onLogout = {
                        repository.setCurrentUser(null)
                        currentScreenRoute = Screen.Auth.route
                    }
                )
            }
        }
    }
}
