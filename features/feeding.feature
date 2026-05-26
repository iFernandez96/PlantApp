Feature: Feeding reminders
  As a container gardener
  I want feeding reminders that account for container constraints and growth stage
  So that I fertilize appropriately for plants whose nutrients leach faster in pots

  Background:
    Given I am signed in
    And the care-engine version is recorded on every generated task

  @slice-5
  Scenario: Container tomato in fruiting stage gets more frequent feeding reminders
    Given I have a tomato plant in a 19-liter container
    And its growth stage is "fruiting"
    And the profile's feedingProfile specifies higher frequency during fruiting in containers
    When the care-engine computes feeding
    Then a CareTask of kind "feed" is created with an interval shorter than the base species interval
    And the rationale mentions container nutrient leaching and fruiting stage

  @slice-5
  Scenario: User-reported over-feeding pushes next feeding later
    Given a feed CareTask was completed yesterday
    When I record feedback "fertilizer-too-strong"
    Then the next generated feed CareTask is delayed by at least one interval
    And the rationale references the recent strong-feed feedback

  @slice-5
  Scenario: Strawberry post-harvest flush triggers a follow-up feeding
    Given I have a strawberry plant in a container
    And I log a harvest event
    When the care-engine recomputes
    Then a feed CareTask is created within the species-defined post-harvest window
    And the rationale mentions post-harvest recovery feeding

  @slice-5
  Scenario: Feeding is suppressed during dormancy
    Given I have a plant whose growth stage is "dormant"
    When the care-engine recomputes
    Then no feed CareTask is created for that plant
    And the rationale log shows the suppression reason "dormancy"
