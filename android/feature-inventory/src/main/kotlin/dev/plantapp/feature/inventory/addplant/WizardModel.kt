package dev.plantapp.feature.inventory.addplant

import dev.plantapp.domain.model.PlantProfile

/** A pot size the way pots are SOLD (the user never sees litres). [volumeLiters] feeds the care
 *  engine (factor = clamp(vol / recommendedMin, .5, 1.5)). */
data class PotSizeOption(val label: String, val volumeLiters: Double)

/** A friendly place-to-live preset; [kind] is what the backend garden_space.kind stores and
 *  [phrase] is how the confirm step says the place in a sentence ("on the balcony"). */
data class LocationPreset(val label: String, val kind: String, val phrase: String)

/** Pure, JVM-testable data choices for the beginner add-plant wizard. No Android, no UI — the
 *  wizard surfaces plain icon+name choices and derives the technical values the engine/back end
 *  need (litres, material, drainage, growth stage). */
object AddPlantWizardModel {
    val POT_SIZES: List<PotSizeOption> = listOf(
        PotSizeOption("4-inch pot", 0.5),
        PotSizeOption("6-inch pot", 1.5),
        PotSizeOption("1-gallon pot", 4.0),
        PotSizeOption("5-gallon bucket", 19.0),
        PotSizeOption("Window box", 6.0),
        PotSizeOption("Raised bed / in-ground", 75.0),
    )

    // Kinds MUST stay within the garden_spaces_kind_check constraint
    // (supabase/migrations/0002): "Backyard" has no closer enum value than "other".
    val LOCATION_PRESETS: List<LocationPreset> = listOf(
        LocationPreset("Windowsill", "window-ledge", "on the windowsill"),
        LocationPreset("Balcony", "balcony", "on the balcony"),
        LocationPreset("Backyard", "other", "in the backyard"),
        LocationPreset("Indoors", "indoor-room", "indoors"),
    )

    // Icons are custom per-species vector drawables (see WizardIcons) — no emoji.

    /** Friendly browse order for the species-picker category chips. Only categories that are
     *  actually present in the loaded catalog get a chip. */
    val CATEGORY_ORDER: List<String> = listOf(
        "houseplant", "herb", "vegetable", "fruit", "berry",
        "ornamental", "succulent", "root", "vine", "other",
    )

    /** Pure picker filter: case-insensitive substring match on any common name or the
     *  scientific name; optional category; result sorted by display name. */
    fun filterProfiles(
        profiles: List<PlantProfile>,
        query: String,
        category: String?,
    ): List<PlantProfile> {
        val q = query.trim()
        return profiles
            .filter { category == null || it.category == category }
            .filter {
                q.isEmpty() ||
                    it.commonNames.any { n -> n.contains(q, ignoreCase = true) } ||
                    it.scientificName.contains(q, ignoreCase = true)
            }
            .sortedBy { (it.commonNames.firstOrNull() ?: it.scientificName).lowercase() }
    }

    // Hidden defaults the novice never sets (the engine / back end still need them):
    const val DEFAULT_MATERIAL = "plastic"
    const val DEFAULT_DRAINAGE = "good"
    const val DEFAULT_GROWTH_STAGE = "seedling"
}
