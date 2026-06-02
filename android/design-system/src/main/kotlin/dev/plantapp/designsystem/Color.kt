package dev.plantapp.designsystem

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

val VerdantPrimary = Color(0xFF1F6F4A)
val VerdantPrimaryDark = Color(0xFF7ED9A4)
val VerdantOnPrimary = Color(0xFFFFFFFF)
val VerdantOnPrimaryDark = Color(0xFF062013)
val VerdantPrimaryContainer = Color(0xFFC9F2D8)
val VerdantPrimaryContainerDark = Color(0xFF16472F)
val VerdantSecondary = Color(0xFF8A6F3D)
val VerdantSecondaryDark = Color(0xFFDCC58A)
val VerdantTertiary = Color(0xFF2F7E8C)
val VerdantTertiaryDark = Color(0xFF8CDCE6)
val VerdantBackground = Color(0xFFF6F3E9)
val VerdantBackgroundDark = Color(0xFF07130D)
val VerdantSurface = Color(0xFFFFFBF2)
val VerdantSurfaceDark = Color(0xFF0F1D16)
val VerdantOnSurface = Color(0xFF1E241F)
val VerdantOnSurfaceDark = Color(0xFFE7F0E9)

val VerdantLightColorScheme = lightColorScheme(
    primary = VerdantPrimary, onPrimary = VerdantOnPrimary,
    primaryContainer = VerdantPrimaryContainer, onPrimaryContainer = Color(0xFF07351F),
    inversePrimary = VerdantPrimaryDark,
    secondary = VerdantSecondary, onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFF6E5B3), onSecondaryContainer = Color(0xFF2D2108),
    tertiary = VerdantTertiary, onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFC4EDF2), onTertiaryContainer = Color(0xFF062F36),
    background = VerdantBackground, onBackground = VerdantOnSurface,
    surface = VerdantSurface, onSurface = VerdantOnSurface,
    surfaceVariant = Color(0xFFE2E8DD), onSurfaceVariant = Color(0xFF434B45), surfaceTint = VerdantPrimary,
    inverseSurface = Color(0xFF293129), inverseOnSurface = Color(0xFFF0F7EF),
    error = Color(0xFFBA1A1A), onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6), onErrorContainer = Color(0xFF410002),
    outline = Color(0xFF737B73), outlineVariant = Color(0xFFC3CBC1), scrim = Color(0xFF000000),
)

val VerdantDarkColorScheme = darkColorScheme(
    primary = VerdantPrimaryDark, onPrimary = VerdantOnPrimaryDark,
    primaryContainer = VerdantPrimaryContainerDark, onPrimaryContainer = Color(0xFFC9F2D8),
    inversePrimary = VerdantPrimary,
    secondary = VerdantSecondaryDark, onSecondary = Color(0xFF3B2D0A),
    secondaryContainer = Color(0xFF5E4A20), onSecondaryContainer = Color(0xFFF6E5B3),
    tertiary = VerdantTertiaryDark, onTertiary = Color(0xFF00363E),
    tertiaryContainer = Color(0xFF145B66), onTertiaryContainer = Color(0xFFC4EDF2),
    background = VerdantBackgroundDark, onBackground = VerdantOnSurfaceDark,
    surface = VerdantSurfaceDark, onSurface = VerdantOnSurfaceDark,
    surfaceVariant = Color(0xFF3F4A42), onSurfaceVariant = Color(0xFFC4CEC5), surfaceTint = VerdantPrimaryDark,
    inverseSurface = Color(0xFFE7F0E9), inverseOnSurface = Color(0xFF253027),
    error = Color(0xFFFFB4AB), onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A), onErrorContainer = Color(0xFFFFDAD6),
    outline = Color(0xFF8D978E), outlineVariant = Color(0xFF3F4A42), scrim = Color(0xFF000000),
)
