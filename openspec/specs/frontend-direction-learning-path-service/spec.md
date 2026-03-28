# direction-learning-path-service Specification

## Purpose
TBD - created by archiving change integrate-direction-learning-path-api. Update Purpose after archive.
## Requirements
### Requirement: API service for direction learning path

The system SHALL provide a service module to interact with backend direction learning path API.

#### Scenario: Service module structure
- **WHEN** the service is imported
- **THEN** system exposes `getLearningPath(slug)` function

#### Scenario: API endpoint configuration
- **WHEN** service calls backend
- **THEN** system uses `publicClient` to call `/directions/{slug}/learning-path`

#### Scenario: Response type definition
- **WHEN** service returns data
- **THEN** data conforms to `DirectionLearningPathDTO` type with slug, title, and learningPath array

---

### Requirement: DTO type definitions

The system SHALL define TypeScript types matching backend DTOs for direction learning path.

#### Scenario: LearningStepDTO type
- **WHEN** defining step type
- **THEN** type includes step (number), title (string), videoLink (string | null)

#### Scenario: DirectionLearningPathDTO type
- **WHEN** defining response type
- **THEN** type includes slug (string), title (string), learningPath (LearningStepDTO[])

