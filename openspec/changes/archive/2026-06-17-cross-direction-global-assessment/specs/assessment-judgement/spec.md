## MODIFIED Requirements

### Requirement: Judgement result visibility (global assessment scope)

The system SHALL allow candidates to view their own judgement results and allow team members or higher roles to view candidate judgement results within their authorized scope.

For global assessments (`direction = null`):
- DIRECTION_ADMIN SHALL be able to view candidate judgement results regardless of the candidate's direction
- Team members SHALL be able to view candidate judgement results

#### Scenario: Direction admin views global assessment results
- **WHEN** a DIRECTION_ADMIN (direction=COMPUTER_VISION) requests judgement results for a global assessment (`direction = null`)
- **THEN** the system SHALL return candidate judgement results across all directions

#### Scenario: Member views global assessment results
- **WHEN** a team member requests judgement results for a global assessment within their authorized role scope
- **THEN** the system SHALL return the matching candidate judgement results

### Requirement: Assessment pass decision (global assessment scope)

The system SHALL allow direction administrators or higher roles to set the final pass decision for a candidate assessment based on the question judgement results.

For global assessments (`direction = null`), any DIRECTION_ADMIN SHALL be able to set pass decisions for any candidate regardless of the candidate's direction.

#### Scenario: Direction admin marks candidate from another direction as passed in global assessment
- **WHEN** a COMPUTER_VISION DIRECTION_ADMIN sets a pass decision for a STRUCTURAL_DESIGN candidate in a global assessment
- **THEN** the system SHALL save the decision and return success
