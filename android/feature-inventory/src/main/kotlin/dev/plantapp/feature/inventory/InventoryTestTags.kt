package dev.plantapp.feature.inventory

/** Stable semantics tags so UI tests can assert against the inventory screens. */
object InventoryTestTags {
    const val EMPTY_STATE = "inventory_empty_state"
    const val ADD_PLANT_BUTTON = "inventory_add_plant_button"
    const val PLANT_LIST = "inventory_plant_list"
    const val PLANT_ROW_PREFIX = "plant_row_"

    const val FIELD_PROFILE_SELECTOR = "field_profile_selector"
    const val FIELD_CONTAINER_SELECTOR = "field_container_selector"
    const val CONTAINER_CREATE_ITEM = "container_create_item"
    const val FIELD_NEW_CONTAINER_NAME = "field_new_container_name"
    const val FIELD_NEW_CONTAINER_VOLUME = "field_new_container_volume"
    const val FIELD_NEW_CONTAINER_MATERIAL = "field_new_container_material"
    const val FIELD_NEW_CONTAINER_DRAINAGE = "field_new_container_drainage"
    const val CONTAINER_CREATE_BUTTON = "container_create_button"
    const val FIELD_GARDEN_SPACE_SELECTOR = "field_garden_space_selector"
    const val GARDEN_SPACE_CREATE_ITEM = "garden_space_create_item"
    const val FIELD_NEW_GARDEN_SPACE_NAME = "field_new_garden_space_name"
    const val FIELD_NEW_GARDEN_SPACE_KIND = "field_new_garden_space_kind"
    const val GARDEN_SPACE_CREATE_BUTTON = "garden_space_create_button"
    const val FIELD_GROWTH_STAGE = "field_growth_stage"
    const val FIELD_LAST_WATERED_AT = "field_last_watered_at"
    const val SUBMIT_BUTTON = "add_plant_submit_button"
    const val CONTAINER_ERROR = "add_plant_container_error"

    const val TASK_KIND = "detail_task_kind"
    const val TASK_RATIONALE = "detail_task_rationale"
    const val TASK_DUE_AT = "detail_task_due_at"

    const val ADVISORY_SECTION = "detail_advisories"
    const val ADVISORY_ACCEPT_BUTTON_PREFIX = "advisory_accept_"

    const val FIELD_SIGNIN_EMAIL = "field_signin_email"
    const val SIGNIN_SEND_CODE_BUTTON = "signin_send_code_button"
    const val FIELD_SIGNIN_CODE = "field_signin_code"
    const val SIGNIN_VERIFY_BUTTON = "signin_verify_button"
    const val SIGNIN_ERROR = "signin_error"

    // Beginner add-plant wizard (stable suffixes: profileId / location kind / pot label-slug).
    const val WIZARD_SPECIES_TILE_PREFIX = "wizard_species_"
    const val WIZARD_LOCATION_TILE_PREFIX = "wizard_location_"
    const val WIZARD_POT_TILE_PREFIX = "wizard_pot_"
    const val WIZARD_ADD_BUTTON = "wizard_add_button"
    const val WIZARD_BACK_BUTTON = "wizard_back_button"
    const val WIZARD_ERROR = "wizard_error"
}
