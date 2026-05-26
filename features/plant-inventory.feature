Feature: Plant inventory
  As a container gardener
  I want to record each plant as an instance tied to a container and garden space
  So that all care reasoning can use container and microclimate context

  Background:
    Given I am signed in as user "owner"
    And the plant catalog contains profiles for "Passion fruit", "Tomato", "Tomatillo", "Strawberry", and "Basil"
    And a garden space "West Balcony" exists for user "owner"
    And a container "Blue barrel" exists for user "owner" with volume 19 liters and material "plastic"

  @slice-1 @happy-path
  Scenario: Add a passion fruit plant in a 5-gallon barrel with a last-watered date
    When I add a plant with:
      | profile        | Passion fruit             |
      | nickname       | Pasi                      |
      | container      | Blue barrel               |
      | garden space   | West Balcony              |
      | placement      | floor                     |
      | growth stage   | vegetative                |
      | last watered   | 2026-05-26T07:00:00Z      |
    Then a plant instance is persisted with the supplied profile, container, and garden space
    And the plant instance records lastWateredAt = "2026-05-26T07:00:00Z"
    And listing my plants returns the new plant exactly once
    And fetching the plant detail returns nickname "Pasi", placement "floor", and growth stage "vegetative"

  @slice-1 @happy-path
  Scenario: Initial water task is anchored on the supplied lastWateredAt
    Given the care-engine version is "0.1.0"
    When I add a plant with:
      | profile        | Tomato                    |
      | container      | Blue barrel               |
      | garden space   | West Balcony              |
      | last watered   | 2026-05-26T07:00:00Z      |
    Then exactly one CareTask of kind "water" is generated for that plant
    And the CareTask has a non-empty rationale that mentions the baseline timestamp
    And the CareTask has engineVersion "0.1.0"
    And the CareTask has a non-empty inputsHash
    And the CareTask sourceInputs references the plant's profileId, containerId, and gardenSpaceId
    And the CareTask sourceInputs records a clockUtc timestamp
    And the CareTask sourceInputs records wateringBaselineAt = "2026-05-26T07:00:00Z"
    And the CareTask dueAt equals "2026-05-26T07:00:00Z" plus the species base interval scaled by the container factor
    And running the engine again with the same inputs produces a CareTask with the same inputsHash

  @slice-1 @happy-path
  Scenario: Initial water task falls back to createdAt when lastWateredAt is omitted
    Given the care-engine version is "0.1.0"
    When I add a plant with profile "Tomato" in container "Blue barrel" in garden space "West Balcony" without a last-watered date
    Then exactly one CareTask of kind "water" is generated for that plant
    And the CareTask sourceInputs records wateringBaselineAt equal to the plant's createdAt
    And the CareTask dueAt equals the plant's createdAt plus the species base interval scaled by the container factor
    And the CareTask rationale mentions that the baseline is the plant's creation time

  @slice-1 @determinism
  Scenario: Two plants identical except for lastWateredAt produce different inputsHash and dueAt
    Given the care-engine version is "0.1.0"
    When I add a plant with profile "Tomato" in container "Blue barrel" in garden space "West Balcony" and lastWateredAt "2026-05-20T07:00:00Z"
    And I add a second plant with profile "Tomato" in container "Blue barrel" in garden space "West Balcony" and lastWateredAt "2026-05-26T07:00:00Z"
    Then the two CareTasks have different inputsHash values
    And the second CareTask's dueAt is later than the first CareTask's dueAt by exactly the difference in their wateringBaselineAt values

  @slice-1 @negative
  Scenario: A plant cannot be added without a container
    When I try to add a plant with profile "Tomato" in garden space "West Balcony" with no container
    Then the request is rejected with a validation error referencing field "containerId"
    And no plant instance is created
    And no CareTask is created

  @slice-1 @negative
  Scenario: A plant cannot be added without a garden space
    When I try to add a plant with profile "Tomato" in container "Blue barrel" with no garden space
    Then the request is rejected with a validation error referencing field "gardenSpaceId"
    And no plant instance is created
    And no CareTask is created

  @slice-1 @negative
  Scenario: A plant cannot be added with an unknown profile id
    When I try to add a plant with profile id "does-not-exist" in container "Blue barrel" in garden space "West Balcony"
    Then the request is rejected with a validation error referencing field "profileId"
    And no plant instance is created
    And no CareTask is created

  @slice-1 @negative
  Scenario: A container cannot be created with a non-positive volume
    When I try to create a container with volume 0 liters
    Then the request is rejected with a validation error referencing field "volumeLiters"
    And no container is created

  @slice-1 @negative
  Scenario: A container cannot be created with an unknown material
    When I try to create a container with volume 10 liters and material "moon-rock"
    Then the request is rejected with a validation error referencing field "material"
    And no container is created

  @slice-1 @negative
  Scenario: A garden space cannot be created without a name or kind
    When I try to create a garden space with empty name and no kind
    Then the request is rejected with validation errors referencing fields "name" and "kind"
    And no garden space is created

  @slice-1 @authorization
  Scenario: A user cannot read another user's plants
    Given another user "intruder" exists
    When user "intruder" requests the plants of user "owner"
    Then the request is rejected as unauthorized
    And no plant data is returned

  @slice-1 @determinism
  Scenario: Deleting and re-adding the same plant produces a fresh CareTask with an equal-shape sourceInputs reference set
    Given a plant exists for profile "Tomato" in container "Blue barrel" in garden space "West Balcony" with lastWateredAt "2026-05-26T07:00:00Z"
    And its initial water CareTask is recorded
    When I delete the plant and add it again at a later clock with the same lastWateredAt and the same container and garden space
    Then the new CareTask has a different inputsHash because clockUtc and plantInstanceId differ
    And the new CareTask sourceInputs references the new plantInstanceId
    And the new CareTask sourceInputs records the same wateringBaselineAt "2026-05-26T07:00:00Z"
    And the new CareTask dueAt equals the first CareTask's dueAt (the baseline did not change)
    And the new CareTask carries engineVersion "0.1.0"
