package dev.plantapp.feature.inventory.addplant

/** A pot size the way pots are SOLD (the user never sees litres). [volumeLiters] feeds the care
 *  engine (factor = clamp(vol / recommendedMin, .5, 1.5)). */
data class PotSizeOption(val label: String, val volumeLiters: Double)

/** A friendly place-to-live preset; [kind] is what the backend garden_space.kind stores. */
data class LocationPreset(val label: String, val kind: String)

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
        LocationPreset("Windowsill", "window-ledge"),
        LocationPreset("Balcony", "balcony"),
        LocationPreset("Backyard", "other"),
        LocationPreset("Indoors", "indoor-room"),
    )

    // Icons are custom per-species vector drawables (see WizardIcons) — no emoji.

    // Hidden defaults the novice never sets (the engine / back end still need them):
    const val DEFAULT_MATERIAL = "plastic"
    const val DEFAULT_DRAINAGE = "good"
    const val DEFAULT_GROWTH_STAGE = "seedling"
}
