## ADDED Requirements

### Requirement: IntroduceImage competition association
The IntroduceImage entity SHALL support association with a specific competition.

#### Scenario: IntroduceImage with competition association
- **WHEN** creating an IntroduceImage record with type=COMPETITION
- **THEN** competition_id MAY reference a valid Competition record
- **THEN** competition_id SHALL be null when type is not COMPETITION

#### Scenario: IntroduceImage sorting
- **WHEN** creating an IntroduceImage record
- **THEN** sort_order SHALL default to 0
- **THEN** sort_order SHALL be used for ordering images within the same category

### Requirement: IntroduceImage query by competition
The system SHALL provide query operations for IntroduceImage by competition.

#### Scenario: List introduce images by competition
- **WHEN** requesting introduce image list with type=COMPETITION and competitionId parameter
- **THEN** system SHALL filter introduce images where type equals COMPETITION
- **THEN** system SHALL further filter where competition_id equals the provided competitionId
- **THEN** images SHALL be sorted by sort_order ASC
- **THEN** each item SHALL include id, type, description, fileId, fileUrl, competitionId, sortOrder

#### Scenario: Invalid competitionId parameter
- **WHEN** requesting introduce image list with type not equal to COMPETITION but competitionId parameter is provided
- **THEN** system SHALL return validation error
- **THEN** error message SHALL indicate competitionId parameter is only valid when type=COMPETITION

### Requirement: IntroduceImage count by competition
The system SHALL provide count operation for IntroduceImage by competition.

#### Scenario: Count images by competition
- **WHEN** requesting count of images for a competition
- **THEN** system SHALL return the count of IntroduceImage records where type=COMPETITION and competition_id equals the provided competitionId
