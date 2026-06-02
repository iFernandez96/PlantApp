package dev.plantapp.feature.inventory

/** Stable semantics tags so UI tests can assert against the inventory screens. */
object InventoryTestTags {
    const val EMPTY_STATE = "inventory_empty_state"
    const val ADD_PLANT_BUTTON = "inventory_add_plant_button"
    const val PLANT_LIST = "inventory_plant_list"

    const val FIELD_PROFILE_ID = "field_profile_id"
    const val FIELD_CONTAINER_ID = "field_container_id"
    const val FIELD_GARDEN_SPACE_ID = "field_garden_space_id"
    const val FIELD_GROWTH_STAGE = "field_growth_stage"
    const val FIELD_LAST_WATERED_AT = "field_last_watered_at"
    const val SUBMIT_BUTTON = "add_plant_submit_button"
    const val CONTAINER_ERROR = "add_plant_container_error"

    const val TASK_KIND = "detail_task_kind"
    const val TASK_RATIONALE = "detail_task_rationale"
    const val ENGINE_VERSION_BADGE = "detail_engine_version_badge"
    const val TASK_DUE_AT = "detail_task_due_at"

    const val ADVISORY_SECTION = "detail_advisories"
}
