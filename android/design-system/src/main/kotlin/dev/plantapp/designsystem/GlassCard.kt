package dev.plantapp.designsystem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Garden Hearth surface: mostly opaque warm card (decorative translucency only). */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large,
    tonalElevation: Dp = 6.dp,
    shadowElevation: Dp = 10.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val isDark = cs.surface.luminance() < 0.5f
    val container = lerp(cs.surfaceColorAtElevation(tonalElevation), cs.primaryContainer, if (isDark) 0.08f else 0.10f)
        .copy(alpha = if (isDark) 0.90f else 0.94f)
    Card(
        modifier = modifier, shape = shape,
        colors = CardDefaults.cardColors(containerColor = container, contentColor = cs.onSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = shadowElevation),
        border = BorderStroke(1.dp, cs.outline.copy(alpha = if (isDark) 0.24f else 0.16f)),
        content = content,
    )
}

@Composable
fun GlassCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = MaterialTheme.shapes.large,
    tonalElevation: Dp = 6.dp,
    shadowElevation: Dp = 10.dp,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable ColumnScope.() -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val isDark = cs.surface.luminance() < 0.5f
    val container = lerp(cs.surfaceColorAtElevation(tonalElevation), cs.primaryContainer, if (isDark) 0.10f else 0.12f)
        .copy(alpha = if (isDark) 0.90f else 0.94f)
    Card(
        onClick = onClick, modifier = modifier, enabled = enabled, shape = shape,
        colors = CardDefaults.cardColors(containerColor = container, contentColor = cs.onSurface,
            disabledContainerColor = container.copy(alpha = 0.42f), disabledContentColor = cs.onSurface.copy(alpha = 0.38f)),
        elevation = CardDefaults.cardElevation(defaultElevation = shadowElevation, pressedElevation = 4.dp,
            focusedElevation = shadowElevation + 2.dp, hoveredElevation = shadowElevation + 2.dp, disabledElevation = 0.dp),
        border = BorderStroke(1.dp, cs.outline.copy(alpha = if (isDark) 0.26f else 0.18f)),
        interactionSource = interactionSource, content = content,
    )
}
