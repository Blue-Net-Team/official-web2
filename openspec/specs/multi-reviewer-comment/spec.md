## ADDED Requirements

### Requirement: Team members can comment and score file upload answers
The system SHALL allow users with team member or higher permission to add a comment and an optional reference score to a file upload answer.

#### Scenario: Member adds comment to file upload answer
- **WHEN** a team member submits a comment with optional score for a file upload answer
- **THEN** the system SHALL save the comment with the member's user id, the answer id, content, score, and timestamp
- **AND** return the saved comment

#### Scenario: Member attempts to comment on non-file-upload answer
- **WHEN** a team member submits a comment for a single choice, multiple choice, or algorithm answer
- **THEN** the system SHALL reject the operation with a bad request response

#### Scenario: Candidate attempts to add comment
- **WHEN** a candidate attempts to add a comment to any answer
- **THEN** the system SHALL reject the operation with a forbidden response

### Requirement: One comment per user per answer
The system SHALL enforce that a user can add at most one comment to a given answer.

#### Scenario: Member adds second comment to same answer
- **WHEN** a team member attempts to add a comment to an answer they have already commented on
- **THEN** the system SHALL reject the operation with a conflict response

### Requirement: Comments are visible to authorized team members
The system SHALL expose all comments for a file upload answer to users with team member or higher permission within the same direction.

#### Scenario: Member views comments for an answer
- **WHEN** an authorized member requests comments for a file upload answer
- **THEN** the system SHALL return all comments including commenter identity, content, score, and timestamp

#### Scenario: Candidate attempts to view comments before publication
- **WHEN** a candidate requests comments for their answer before the assessment results have been published
- **THEN** the system SHALL return an empty list or forbidden response

#### Scenario: Candidate views comments after publication
- **WHEN** a candidate requests comments for their answer after the assessment results have been published
- **THEN** the system SHALL return all comments for that answer

### Requirement: Comment owners can update or delete their own comments
The system SHALL allow a user to update or delete a comment they previously added, but SHALL prevent direction administrators from modifying or deleting comments made by others.

#### Scenario: Comment owner updates their comment
- **WHEN** a team member submits an update to a comment they previously created
- **THEN** the system SHALL update the comment content and score
- **AND** return the updated comment

#### Scenario: Comment owner deletes their comment
- **WHEN** a team member requests deletion of a comment they previously created
- **THEN** the system SHALL remove the comment record
- **AND** return a success response

#### Scenario: Non-owner attempts to update a comment
- **WHEN** a user attempts to update a comment created by another user
- **THEN** the system SHALL reject the operation with a forbidden response

#### Scenario: Non-owner attempts to delete a comment
- **WHEN** a user attempts to delete a comment created by another user
- **THEN** the system SHALL reject the operation with a forbidden response
