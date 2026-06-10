package dev.plantapp.feature.inventory.addplant

import androidx.annotation.DrawableRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Balcony
import androidx.compose.material.icons.filled.Compost
import androidx.compose.material.icons.filled.Cottage
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Window
import androidx.compose.material.icons.filled.Yard
import androidx.compose.ui.graphics.vector.ImageVector
import dev.plantapp.feature.inventory.R

/** Maps wizard choices to real sourced icons (no emoji, no hand-drawn art):
 *  - species → CC0 crop vector drawables (openfarmcc/open-crop-icons; passion fruit uses the
 *    generic-plant icon as the set has none).
 *  - pots + locations → Material Symbols (Apache-2.0) ImageVectors, picked so the six pot sizes are
 *    visually distinct (bucket / window-box / raised-bed differ from the small pots).
 *  See feature-inventory/ICON_LICENSES.md. */
object WizardIcons {
    @DrawableRes
    fun speciesIconRes(profileId: String): Int = when (profileId) {
        "solanum-lycopersicum" -> R.drawable.ic_species_tomato
        "ocimum-basilicum" -> R.drawable.ic_species_basil
        "fragaria-x-ananassa" -> R.drawable.ic_species_strawberry
        "passiflora-edulis" -> R.drawable.ic_species_passionfruit
        "physalis-philadelphica" -> R.drawable.ic_species_tomatillo
        else -> R.drawable.ic_species_default
    }

    /** Distinct glyphs per pot size; the 3 small pots share one (the label gives the size). */
    fun potIcon(label: String): ImageVector = when (label) {
        "5-gallon bucket" -> Icons.Filled.Compost
        "Window box" -> Icons.Filled.Window
        "Raised bed / in-ground" -> Icons.Filled.Grass
        else -> Icons.Filled.LocalFlorist // 4-inch / 6-inch / 1-gallon pots
    }

    fun locationIcon(kind: String): ImageVector = when (kind) {
        "window-ledge" -> Icons.Filled.WbSunny
        "balcony" -> Icons.Filled.Balcony
        "other" -> Icons.Filled.Cottage
        "indoor-room" -> Icons.Filled.Home
        else -> Icons.Filled.Yard
    }
}
