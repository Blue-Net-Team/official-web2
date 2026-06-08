## MODIFIED Requirements

### Requirement: Comments are visible to authorized users
The system SHALL expose all comments for a file upload answer to authorized users without role-based filtering.

#### Scenario: Member views comments for an answer
- **WHEN** an authorized member requests comments for a file upload answer
- **THEN** the system SHALL return all comments including commenter identity, content, score, and timestamp
- **AND** comments from direction administrators and super administrators SHALL be included

#### Scenario: Candidate views comments after publication
- **WHEN** a candidate requests comments for their answer after the assessment results have been published
- **THEN** the system SHALL return all comments for that answer including those from administrators

## REMOVED Requirements

### Requirement: Comments are visible to authorized team members
**Reason**: Replaced by unfiltered comment visibility; role-based filtering is removed.
**Migration**: All authorized users now see all comments regardless of the commenter's role.
