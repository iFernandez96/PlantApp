package dev.plantapp.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/** Minimal Material 3 theme wrapper for the app. Slice 1 uses default M3 color schemes;
 *  brand theming is a later concern. */
@Composable
fun PlantAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(content = content)
}
