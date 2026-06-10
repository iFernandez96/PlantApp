package dev.plantapp.designsystem

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import dev.plantapp.designsystem.R

// Both families ship as a single variable TTF, so each weight is realised by setting the `wght`
// axis explicitly (FontVariation) rather than relying on the bare Font(res, weight) mapping.
@OptIn(ExperimentalTextApi::class)
private fun frauncesFont(weight: FontWeight, wght: Int) = Font(
    resId = R.font.fraunces,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(wght)),
)

@OptIn(ExperimentalTextApi::class)
private fun manropeFont(weight: FontWeight, wght: Int) = Font(
    resId = R.font.manrope,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(wght)),
)

/** Fraunces (display/headline) — expressive high-contrast serif. */
val FrauncesFontFamily = FontFamily(
    frauncesFont(FontWeight.Light, 300),
    frauncesFont(FontWeight.Normal, 400),
    frauncesFont(FontWeight.Medium, 500),
    frauncesFont(FontWeight.SemiBold, 600),
    frauncesFont(FontWeight.Bold, 700),
)

/** Manrope (title/body/label) — clean geometric sans. */
val ManropeFontFamily = FontFamily(
    manropeFont(FontWeight.Normal, 400),
    manropeFont(FontWeight.Medium, 500),
    manropeFont(FontWeight.SemiBold, 600),
    manropeFont(FontWeight.Bold, 700),
)

val PlantAppTypography = Typography(
    displayLarge = TextStyle(fontFamily = FrauncesFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 56.sp, lineHeight = 64.sp),
    displayMedium = TextStyle(fontFamily = FrauncesFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 44.sp, lineHeight = 52.sp),
    displaySmall = TextStyle(fontFamily = FrauncesFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 36.sp, lineHeight = 44.sp),
    headlineLarge = TextStyle(fontFamily = FrauncesFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 32.sp, lineHeight = 40.sp),
    headlineMedium = TextStyle(fontFamily = FrauncesFontFamily, fontWeight = FontWeight.Medium, fontSize = 28.sp, lineHeight = 36.sp),
    headlineSmall = TextStyle(fontFamily = FrauncesFontFamily, fontWeight = FontWeight.Medium, fontSize = 24.sp, lineHeight = 32.sp),
    titleLarge = TextStyle(fontFamily = ManropeFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontFamily = ManropeFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, lineHeight = 24.sp, letterSpacing = 0.15.sp),
    titleSmall = TextStyle(fontFamily = ManropeFontFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
    bodyLarge = TextStyle(fontFamily = ManropeFontFamily, fontWeight = FontWeight.Normal, fontSize = 17.sp, lineHeight = 28.sp, letterSpacing = 0.5.sp),
    bodyMedium = TextStyle(fontFamily = ManropeFontFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 22.sp, letterSpacing = 0.25.sp),
    bodySmall = TextStyle(fontFamily = ManropeFontFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 18.sp, letterSpacing = 0.4.sp),
    labelLarge = TextStyle(fontFamily = ManropeFontFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, lineHeight = 22.sp, letterSpacing = 0.1.sp),
    labelMedium = TextStyle(fontFamily = ManropeFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp),
    labelSmall = TextStyle(fontFamily = ManropeFontFamily, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp),
)
