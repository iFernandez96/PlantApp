package dev.plantapp.feature.inventory

/** Plain-language labels for engine vocabulary (task kinds mirror the DB check constraint in
 *  supabase/migrations/0003). Beginner-first copy: no horticulture or engine jargon. */
object DisplayText {
    /** "water" -> "Water", "repot" -> "Move to a bigger pot", etc. */
    fun taskKindLabel(kind: String): String = when (kind) {
        "water" -> "Water"
        "feed" -> "Feed"
        "prune" -> "Trim"
        "repot" -> "Move to a bigger pot"
        "scout-pests" -> "Check for bugs"
        "harvest" -> "Harvest"
        "support" -> "Add a support"
        "rotate" -> "Turn the pot"
        "seasonal-prep" -> "Get ready for the season"
        else -> kind.replace('-', ' ').replaceFirstChar { it.uppercase() }
    }

    /** Last-resort species name when nickname and profile name are both missing: de-slug the
     *  profile id ("solanum-lycopersicum" -> "Solanum lycopersicum"). */
    fun speciesFallbackName(profileId: String): String =
        profileId.replace('-', ' ').replaceFirstChar { it.uppercase() }

    /** "vegetative" -> "Growing well", etc. */
    fun growthStageLabel(stage: String): String = when (stage) {
        "seedling" -> "Just starting out"
        "vegetative" -> "Growing well"
        "flowering" -> "Flowering"
        "fruiting" -> "Making fruit"
        "dormant" -> "Resting for now"
        else -> stage.replace('-', ' ').replaceFirstChar { it.uppercase() }
    }
}
