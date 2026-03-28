## ADDED Requirements

### Requirement: College reference data
The College entity SHALL provide reference data for user and enrollment college affiliation.

#### Scenario: College data structure
- **WHEN** creating a College
- **THEN** name SHALL store the college name
- **THEN** it SHALL be referenced by User.college_id
- **THEN** it SHALL be referenced by Enroll.college_id
- **THEN** it SHALL NOT have deleted field (soft delete not required for reference data)
