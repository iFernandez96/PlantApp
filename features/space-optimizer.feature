Feature: AI space optimizer for balconies and small spaces
  As a small-space container gardener
  I want to upload photos and measurements of my balcony and receive a layout plan
  So that I optimize sunlight, footprint, and vertical capacity

  Background:
    Given I am signed in
    And the backend AI gateway is the only caller of LLM providers
    And every AI response is validated against space-plan.schema.json

  @slice-8
  Scenario: Balcony layout plan includes both horizontal and vertical options
    Given my "West Balcony" measures 1.2m wide by 0.8m deep by 2.4m tall
    And it faces south with an estimated 6 sun hours per day
    And I have 3 plants currently placed
    When I request a space optimization
    Then the response conforms to space-plan.schema.json
    And the plan includes at least one horizontal layout option
    And the plan includes at least one vertical layout option
    And the plan lists tradeoffs for each option

  @slice-8
  Scenario: Wind-exposed railing flags hanging-weight tradeoff
    Given my garden space has windExposure "high"
    When I request a space optimization
    Then the plan's vertical option includes a wind/weight tradeoff
    And hanging-heavy-fruit placements are not recommended without a caveat

  @slice-8
  Scenario: Indoor window-ledge space does not recommend outdoor-only species
    Given the garden space kind is "window-ledge" and is indoor
    When I request a space optimization
    Then no placement assigns an outdoor-only species to the indoor space
    And the rationale mentions indoor light constraints

  @slice-8
  Scenario: Vertical capacity is computed when height allows stacking
    Given my garden space height is at least 2.0m
    Then the plan must consider a multi-tier vertical layout

  @slice-8
  Scenario: The plan is presented as a proposal and is not auto-applied
    Given a space plan is returned
    Then no PlantInstance placements are mutated automatically
    When I accept individual placements
    Then those placements are written through the inventory API
