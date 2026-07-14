# assessment-domain-service-refactor Specification

## Purpose
TBD - created by archiving change refactor-ddd-layer-architecture. Update Purpose after archive.
## Requirements
### Requirement: AssessmentAnswerDomainService coordinates answer submission
The system SHALL introduce `AssessmentAnswerDomainService` that encapsulates the rules for submitting and updating an answer, including direction/grade matching, time window validation, elimination checks, session deadline checks, duplicate submission prevention, team leader validation, and team member answer synchronization.

#### Scenario: Candidate submits answer during valid window
- **WHEN** a candidate invokes submit answer for an open question within the time window and without prior submission
- **THEN** the domain service SHALL create and persist an `AssessmentAnswer` entity

#### Scenario: Candidate submits after deadline
- **WHEN** a candidate invokes submit answer after the assessment end time or personal session deadline
- **THEN** the domain service SHALL reject the submission with a bad request exception

#### Scenario: Candidate from eliminated prior epoch submits
- **WHEN** a candidate invokes submit answer for an assessment time where `AssessmentDecisionDomainService` determines the user is eliminated from a prior epoch
- **THEN** the domain service SHALL reject the submission with a security exception

#### Scenario: Team leader submits file upload answer
- **WHEN** a team leader submits an answer for a team-enabled file upload question
- **THEN** the domain service SHALL create the leader answer and synchronize answer records for all team members

### Requirement: AssessmentTeamDomainService coordinates team lifecycle
The system SHALL introduce `AssessmentTeamDomainService` that encapsulates team creation, joining, leaving, leader transfer, and disbanding rules, including checks for existing personal answers, existing team answers, and submitted answers.

#### Scenario: User with personal answer tries to create team
- **WHEN** a user who has already submitted a personal answer attempts to create a team
- **THEN** the domain service SHALL reject the operation

#### Scenario: Disband team with submitted answers
- **WHEN** a team leader attempts to disband a team that has already submitted answers
- **THEN** the domain service SHALL reject the operation

### Requirement: AssessmentJudgementDomainService is split
The system SHALL move create/update/get CRUD operations of `AssessmentJudgement` to the entity and repository, keeping only `finalizeJudgement` and any concurrency-specific upsert logic in the domain service.

#### Scenario: Finalize judgement with admin source
- **WHEN** a direction admin finalizes a judgement
- **THEN** the domain service SHALL perform an atomic upsert of the admin-finalized judgement record

### Requirement: AssessmentDecisionDomainService keeps elimination rule
The system SHALL retain `isEliminatedFromPriorEpoch` in `AssessmentDecisionDomainService` and move `saveDecision`/`getDecision` CRUD to the entity and repository.

#### Scenario: Check prior epoch elimination
- **WHEN** the system checks whether a user is eliminated from an earlier epoch of the same direction and grade
- **THEN** the domain service SHALL return true if a prior elimination decision exists

### Requirement: Assessment VO entities are removed from repository interfaces
The system SHALL remove `AssessmentDecisionVO` and `AssessmentJudgementVO` from repository return types; repositories SHALL return the corresponding entities.

#### Scenario: Find assessment decision by id
- **WHEN** `AssessmentDecisionRepository.findById(id)` is invoked
- **THEN** it SHALL return `Optional<AssessmentDecision>`

