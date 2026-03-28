## MODIFIED Requirements

### Requirement: Introduction image management
The IntroduceImage entity SHALL manage website introduction images with categorization and file association.

#### Scenario: Image categorization
- **WHEN** creating an IntroduceImage
- **THEN** type MUST be one of: laboratory, equipment, team_photo, direction, competition, patent, paper
- **THEN** description SHALL provide detailed information about the image content
- **THEN** fileId MUST reference a valid File record with type normal_img
