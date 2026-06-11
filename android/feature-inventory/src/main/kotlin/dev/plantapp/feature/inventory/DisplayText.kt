package dev.plantapp.feature.inventory

import dev.plantapp.domain.SessionExpiredException

/** Plain-language labels for engine vocabulary (task kinds mirror the DB check constraint in
 *  supabase/migrations/0003). Beginner-first copy: no horticulture or engine jargon. */
object DisplayText {
    /** Screen-safe error copy. Names session expiry; otherwise uses the screen's fallback.
     *  NEVER surfaces e.message — raw exception text (HTTP codes, LAN IPs) is not for users. */
    fun friendlyError(e: Throwable, fallback: String): String = when (e) {
        is SessionExpiredException -> "Your session ended. Please sign in again."
        else -> fallback
    }

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

    /** Catalog category -> beginner browse label ("ornamental" -> "Flowers"). */
    fun categoryLabel(category: String): String = when (category) {
        "houseplant" -> "Houseplants"
        "herb" -> "Herbs"
        "vegetable" -> "Vegetables"
        "fruit" -> "Fruit"
        "berry" -> "Berries"
        "ornamental" -> "Flowers"
        "succulent" -> "Succulents"
        "root" -> "Root vegetables"
        "vine" -> "Vines & climbers"
        "other" -> "More"
        else -> category.replace('-', ' ').replaceFirstChar { it.uppercase() }
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
