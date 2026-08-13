package com.shadow.shineyverse.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** The purple->cyan gradient used throughout the web app's primary buttons and accents. */
@Composable
fun ultraGradient(): Brush = Brush.linearGradient(
    listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary),
)

/** "SHINY VERSE" wordmark - SHINY in the normal text color, VERSE in the accent color,
 * matching the web app's `h1` styling on the Kid Mode page. */
@Composable
fun BrandWordmark(modifier: Modifier = Modifier, fontSize: androidx.compose.ui.unit.TextUnit = 20.sp) {
    Row(modifier = modifier) {
        Text(
            "SHINY ",
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.ExtraBold,
            fontSize = fontSize,
            letterSpacing = 1.sp,
        )
        Text(
            "VERSE",
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.ExtraBold,
            fontSize = fontSize,
            letterSpacing = 1.sp,
        )
    }
}

/** A gradient-filled primary action button, matching the web app's `.primaryBtn`. */
@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (enabled) ultraGradient() else Brush.linearGradient(listOf(Color.Gray, Color.Gray)))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(PaddingValues(horizontal = 24.dp, vertical = 14.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            modifier = Modifier.alpha(if (enabled) 1f else 0.7f),
        )
    }
}

/** A prominent gradient-filled pill FAB for launching Pip, matching the web app's accent
 * gradient - meant to read unmistakably as "tap here to chat", not blend into the app bar. */
@Composable
fun PipFab(
    onClick: () -> Unit,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    label: String = "Pip",
) {
    Row(
        modifier = modifier
            .shadow(elevation = 8.dp, shape = CircleShape, clip = false)
            .clip(CircleShape)
            .background(ultraGradient())
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.width(22.dp).height(22.dp))
        Text(
            label,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}

/** A bordered, subtly-elevated panel matching the web app's translucent glass panels. */
@Composable
fun GlassPanel(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        contentColor = LocalContentColor.current,
    ) {
        Box(modifier = Modifier.padding(16.dp)) { content() }
    }
}
