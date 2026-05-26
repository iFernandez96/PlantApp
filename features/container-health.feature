Feature: Container and plant health advisories
  As a container gardener
  I want advisories about container fit, support, and species-specific constraints
  So that long-term plant health is preserved beyond day-to-day watering

  Background:
    Given I am signed in
    And the plant catalog is data-driven; no species-specific code branches exist

  @slice-2
  Scenario: Passion fruit in a 5-gallon container surfaces a larger-container advisory
    Given the "Passion fruit" profile recommends a minimum container of 95 liters and ideal of 95-190 liters
    And I have a passion fruit in a 19-liter container
    When advisories are computed
    Then a container-size advisory is surfaced with severity "high"
    And the advisory cites the profile's recommended minimum and ideal range
    And the advisory suggests target sizes, not a specific brand

  @slice-2
  Scenario: Vining species without support recorded surfaces a support advisory
    Given the active plant profile has requiresSupport = true
    And the PlantInstance does not record any trellis or stake
    When advisories are computed
    Then a support advisory is surfaced

  @slice-2
  Scenario: Tomatillo single-plant household triggers a pollination warning
    Given the "Tomatillo" profile has selfFruitful = false and pollinationPartnersRequired = 2
    And the user has exactly one tomatillo
    When advisories are computed
    Then a pollination warning is surfaced
    And the warning explains that tomatillos are not self-fruitful and recommends a second compatible plant

  @slice-2
  Scenario: Adding the required partner clears the warning
    Given the pollination warning is active
    When I add a second tomatillo
    Then the pollination warning is dismissed automatically

  @slice-2 @invariant
  Scenario: Advisories never auto-schedule corrective tasks
    Given any advisory is active
    Then no CareTask is created without explicit user acceptance of a recommendation
