package com.shadow.shineyverse.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// ShinyVerse/Kid Mode is always the bright pastel palette regardless of system theme -
// the web app's Kid Mode page never defines a dark variant, so neither does this one.
private val KidColors = lightColorScheme(
    background = KidBackground,
    surface = KidPanel,
    onBackground = KidText,
    onSurface = KidText,
    primary = KidAccent,
    secondary = KidAccent2,
    onPrimary = KidPanel,
    outline = KidBorder,
    error = KidDanger,
)

@Composable
fun mutedTextColor() = KidMuted

@Composable
fun ShinyVerseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = KidColors,
        typography = UltraTypography,
        content = content,
    )
}
