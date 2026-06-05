## MODIFIED Requirements

### Requirement: Assessment pass decision
The system SHALL allow direction administrators or higher roles to set the final pass decision for a candidate assessment based on the question judgement results.

#### Scenario: Direction administrator marks candidate as passed
- **WHEN** a direction administrator sets a candidate assessment decision to passed
- **THEN** the system SHALL save the decision, decision maker, decision time, and optional decision comment
- **AND** if the assessment time does not yet have `results_published_at` set, the system SHALL set it to the current time

#### Scenario: Member cannot set final decision
- **WHEN** a team member attempts to set the final pass decision
- **THEN** the system SHALL reject the operation with a forbidden response

#### Scenario: Eliminated candidate from prior epoch is excluded from decision workspace
- **WHEN** a direction administrator queries the decision workspace for an assessment time
- **AND** a candidate has a `passed = false` decision for a prior epoch of the same direction and grade combination
- **THEN** the system SHALL exclude that candidate from the workspace candidate list
- **AND** the system SHALL exclude that candidate from the workspace statistics
- **AND** the system SHALL still include candidates with a `passed = false` decision for the current epoch
