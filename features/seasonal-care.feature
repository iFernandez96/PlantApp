Feature: Seasonal and climate-aware care
  As a container gardener
  I want care reminders and warnings tied to my hardiness zone and the active season
  So that I do not water frost-sensitive plants on a freeze night or skip seasonal prep

  Background:
    Given I am signed in
    And my "West Balcony" garden space resolves to hardiness zone "9b"

  @slice-6
  Scenario: Freeze warning suppresses non-critical tasks and surfaces a protection task
    Given a freeze warning is issued by the local weather provider for tonight
    And I have a frost-sensitive plant on "West Balcony"
    When the care-engine recomputes
    Then any non-critical CareTasks due tonight are deferred
    And a CareTask of kind "seasonal-prep" with rationale "frost protection" is created
    And the rationale cites the weather provider's alert id

  @slice-6
  Scenario: Season transition surfaces a planting-window suggestion (not a task)
    Given the current date is within the spring planting window for "Tomatillo" in zone "9b"
    And I have no tomatillo plants
    Then a planting-window suggestion is surfaced as an advisory
    And the advisory is dismissable and not a CareTask

  @slice-6
  Scenario: Strawberry container scouting during fruiting season
    Given I have a strawberry plant whose growth stage is "fruiting"
    When the engine computes seasonal tasks
    Then a "scout-pests" CareTask is created with the configured species interval
    And the rationale mentions container-grown disease scouting

  @slice-6
  Scenario: Weather provider failure degrades gracefully
    Given the local weather API is unreachable
    When the care-engine computes today's tasks
    Then tasks are still produced using species seasonal defaults
    And each produced task carries a "degraded: true" flag in its rationale metadata
