package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.ui.theme.NutriMindTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    com.example.data.remote.GeminiService.initialize(applicationContext)
    enableEdgeToEdge()
    setContent {
      NutriMindTheme {
        NutriMindApp()
      }
    }
  }
}

