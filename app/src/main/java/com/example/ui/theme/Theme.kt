package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
  primary = EmeraldLight,
  onPrimary = Color(0xFF0F172A),
  primaryContainer = EmeraldDark,
  onPrimaryContainer = Color.White,
  secondary = HealthAmber,
  tertiary = MintTertiary,
  background = Color(0xFF0F172A),
  surface = Color(0xFF1E293B),
  onBackground = Color(0xFFF8FAFC),
  onSurface = Color(0xFFF8FAFC),
)

private val LightColorScheme = lightColorScheme(
  primary = EmeraldDark,
  onPrimary = Color.White,
  primaryContainer = EmeraldContainer,
  onPrimaryContainer = EmeraldDark,
  secondary = HealthAmber,
  onSecondary = Color.White,
  secondaryContainer = AmberContainer,
  onSecondaryContainer = Color(0xFF92400E),
  tertiary = MintTertiary,
  onTertiary = Color.White,
  tertiaryContainer = MintContainer,
  onTertiaryContainer = Color(0xFF065F46),
  background = BackgroundLight,
  surface = SurfaceLight,
  surfaceVariant = SurfaceVariantLight,
  onBackground = Color(0xFF0F172A),
  onSurface = Color(0xFF0F172A),
  onSurfaceVariant = TextSecondary,
  outline = BorderColor,
)

@Composable
fun NutriMindTheme(
  darkTheme: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}

