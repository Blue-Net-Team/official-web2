## Purpose

学院参考数据管理，为用户和报名提供学院关联信息。

## MODIFIED Requirements

### Requirement: College reference data
The College entity SHALL provide reference data for user and enrollment college affiliation.

#### Scenario: College data structure
- **WHEN** creating a College
- **THEN** name SHALL store the college name
- **THEN** it SHALL be referenced by User.college_id
- **THEN** it SHALL be referenced by Enroll.college_id
- **THEN** it SHALL NOT have deleted field (soft delete not required for reference data)

#### Scenario: College name uniqueness
- **WHEN** creating or updating a College
- **THEN** name SHALL be unique across all colleges
- **THEN** system SHALL reject duplicate college names

#### Scenario: College deletion constraint
- **WHEN** deleting a College
- **THEN** system SHALL check for associated Users
- **THEN** system SHALL check for associated Enrollments
- **THEN** deletion SHALL be rejected if associations exist
