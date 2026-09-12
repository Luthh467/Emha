package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.CoralContainer
import com.example.ui.theme.GreenContainer
import com.example.ui.theme.HealthAmber
import com.example.ui.theme.HealthCoral
import com.example.ui.theme.SafeGreen

@Composable
fun RiskCategoryBadge(
    category: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, dotColor) = when {
        category.contains("Rendah", ignoreCase = true) || category.contains("Baik", ignoreCase = true) || category.contains("Normal", ignoreCase = true) -> {
            Triple(GreenContainer, Color(0xFF065F46), SafeGreen)
        }
        category.contains("Perhatian", ignoreCase = true) || category.contains("Kurang", ignoreCase = true) -> {
            Triple(AmberContainer, Color(0xFF92400E), HealthAmber)
        }
        else -> {
            Triple(CoralContainer, Color(0xFF9F1239), HealthCoral)
        }
    }

    Box(
        modifier = modifier
            .testTag("risk_category_badge")
            .background(bgColor, RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(dotColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = category,
                color = textColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
