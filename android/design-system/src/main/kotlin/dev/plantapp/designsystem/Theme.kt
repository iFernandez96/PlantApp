package dev.plantapp.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/** The Verdant Glasshouse Material 3 theme: brand light/dark color schemes (no dynamic color),
 *  Fraunces/Manrope typography, and rounded shapes. Every screen reads `MaterialTheme.colorScheme/
 *  typography/shapes`, so this re-skins the whole app. */
@Composable
fun PlantAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) VerdantDarkColorScheme else VerdantLightColorScheme,
        typography = PlantAppTypography,
        shapes = PlantAppShapes,
        content = content,
    )
}
