package com.example.ui.components

import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight

val InputTextColor = Color(0xFF0F172A) // Bold dark slate / near black for maximum contrast
val InputLabelColor = Color(0xFF334155) // Clear dark gray label
val InputPlaceholderColor = Color(0xFF64748B) // Visible slate placeholder
val InputBorderFocused = EmeraldDark
val InputBorderUnfocused = Color(0xFF94A3B8) // Visible boundary, never washed out
val InputContainerColor = Color.White // Solid white background

/**
 * Material 3 OutlinedTextField colors with guaranteed high contrast,
 * preventing invisible or washed out text on all devices and system themes.
 */
@Composable
fun nutriMindTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = InputTextColor,
    unfocusedTextColor = InputTextColor,
    disabledTextColor = Color(0xFF64748B),
    errorTextColor = Color(0xFFE11D48),
    focusedContainerColor = InputContainerColor,
    unfocusedContainerColor = InputContainerColor,
    disabledContainerColor = Color(0xFFF1F5F9),
    errorContainerColor = InputContainerColor,
    cursorColor = EmeraldDark,
    errorCursorColor = Color(0xFFE11D48),
    focusedBorderColor = InputBorderFocused,
    unfocusedBorderColor = InputBorderUnfocused,
    disabledBorderColor = Color(0xFFCBD5E1),
    errorBorderColor = Color(0xFFE11D48),
    focusedLabelColor = EmeraldDark,
    unfocusedLabelColor = InputLabelColor,
    disabledLabelColor = Color(0xFF94A3B8),
    errorLabelColor = Color(0xFFE11D48),
    focusedPlaceholderColor = InputPlaceholderColor,
    unfocusedPlaceholderColor = InputPlaceholderColor,
    disabledPlaceholderColor = Color(0xFFCBD5E1),
    focusedLeadingIconColor = EmeraldDark,
    unfocusedLeadingIconColor = Color(0xFF475569),
    focusedTrailingIconColor = EmeraldDark,
    unfocusedTrailingIconColor = Color(0xFF475569),
    selectionColors = TextSelectionColors(
        handleColor = EmeraldDark,
        backgroundColor = EmeraldLight.copy(alpha = 0.5f)
    )
)

/**
 * Consistent high-contrast text style for input fields.
 */
fun nutriMindInputTextStyle() = TextStyle(
    color = InputTextColor,
    fontSize = 15.sp,
    fontWeight = FontWeight.SemiBold
)
