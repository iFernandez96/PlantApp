Feature: Watering reminders
  As a container gardener
  I want deterministic watering reminders that account for container size, weather, and feedback
  So that I water reliably without over- or under-watering

  Background:
    Given I am signed in
    And the care-engine version is recorded on every generated task

  @slice-3
  Scenario: Tomato in a small container during a hot week is reminded sooner
    Given I have a tomato plant in a 7-liter container on "West Balcony"
    And the local forecast for the next 3 days shows highs above 32°C with no rain
    And the plant was last watered yesterday
    When the care-engine computes the next watering
    Then a CareTask of kind "water" is created
    And the dueAt is within the next 24 hours
    And the rationale mentions hot-weather adjustment and container volume
    And the engineVersion is stamped on the task

  @slice-6
  Scenario: Cool, rainy forecast extends the watering interval
    Given I have a tomato plant in a 19-liter container on "West Balcony"
    And the local forecast shows 15mm of rain in the next 24 hours and highs below 22°C
    And the plant was last watered today
    When the care-engine computes the next watering
    Then the dueAt is at least 48 hours away
    And the rationale references upcoming rain

  @slice-4
  Scenario: "Soil still wet" feedback pushes the next watering later
    Given a watering CareTask is due today for my tomato
    When I mark the task done with feedback "soil-still-wet"
    Then the next generated watering CareTask is pushed at least 24 hours later than the default interval
    And a CareLogEvent records the feedback

  @slice-4
  Scenario: Skipped watering escalates priority but does not silently reschedule
    Given a watering CareTask is overdue by 24 hours
    When the engine recomputes
    Then the task remains and its priority is raised to "high"
    And a new CareTask is not created until the user resolves the current one

  @slice-1 @determinism @invariant
  Scenario: The engine output is purely a function of inputs
    Given identical PlantInstance, PlantProfile, Container, GardenSpace, weather window, feedback log, and clock
    When the engine runs twice
    Then both runs produce equal CareTask outputs
    And both outputs carry the same inputsHash
