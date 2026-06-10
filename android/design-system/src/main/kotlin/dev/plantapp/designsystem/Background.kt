package dev.plantapp.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance

@Composable
fun PlantAppBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val isDark = colorScheme.background.luminance() < 0.5f
    val linearBackdrop = remember(colorScheme, isDark) {
        val greenhouseTint = if (isDark) lerp(colorScheme.background, colorScheme.primary, 0.12f)
            else lerp(colorScheme.surface, colorScheme.primaryContainer, 0.28f)
        val accentTint = if (isDark) lerp(colorScheme.surface, colorScheme.tertiary, 0.10f)
            else lerp(colorScheme.background, colorScheme.tertiaryContainer, 0.18f)
        Brush.linearGradient(
            colors = listOf(colorScheme.background, colorScheme.surface, greenhouseTint, accentTint, colorScheme.background),
            start = Offset(0f, 0f), end = Offset(1200f, 1800f),
        )
    }
    val topGlow = remember(colorScheme, isDark) {
        val glowColor = if (isDark) colorScheme.primary else colorScheme.tertiary
        Brush.radialGradient(
            colors = listOf(glowColor.copy(alpha = if (isDark) 0.11f else 0.12f), glowColor.copy(alpha = if (isDark) 0.05f else 0.06f), Color.Transparent),
            center = Offset(280f, 90f), radius = 900f,
        )
    }
    Box(modifier = modifier.fillMaxSize().background(linearBackdrop)) {
        Box(modifier = Modifier.matchParentSize().background(topGlow))
        content()
    }
}
