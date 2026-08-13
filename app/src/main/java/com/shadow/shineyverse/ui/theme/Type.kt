package com.shadow.shineyverse.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val UltraTypography = Typography(
    titleLarge = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, letterSpacing = 0.5.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 15.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 13.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 13.sp),
    labelSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 11.sp),
)
