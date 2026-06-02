package dev.plantapp.feature.inventory.addplant

import androidx.annotation.DrawableRes
import dev.plantapp.feature.inventory.R

/** Maps wizard choices to custom (non-emoji) vector drawables. Kept separate from the pure
 *  [AddPlantWizardModel] because it references R.drawable. Species icons are per-id with a generic
 *  fallback; locations are per-kind; one generic pot icon serves all sizes (the label distinguishes). */
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

    @DrawableRes
    fun locationIconRes(kind: String): Int = when (kind) {
        "windowsill" -> R.drawable.ic_loc_windowsill
        "balcony" -> R.drawable.ic_loc_balcony
        "yard" -> R.drawable.ic_loc_backyard
        "indoor" -> R.drawable.ic_loc_indoors
        else -> R.drawable.ic_loc_backyard
    }

    @DrawableRes
    fun potIconRes(): Int = R.drawable.ic_pot
}
