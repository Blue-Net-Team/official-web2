## ADDED Requirements

### Requirement: File metadata storage
The File entity SHALL store file metadata with type-based access control hints.

#### Scenario: File creation and type assignment
- **WHEN** creating a File record
- **THEN** type MUST be one of: avatar, normal_img, evaluation_attachment, work, qrcode
- **THEN** url SHALL contain the accessible URL path
- **THEN** name SHALL store the original filename

### Requirement: File type access control matrix
Different file types SHALL have different access control rules enforced at application layer.

#### Scenario: File type permissions
- **WHEN** file type is work
- **THEN** access SHALL be granted to: file owner OR users with role >= ROLE_MEMBER
- **WHEN** file type is evaluation_attachment
- **THEN** access SHALL be granted to: users with matching direction
- **WHEN** file type is avatar and associated with Enroll
- **THEN** access SHALL be granted to: users with role >= ROLE_MEMBER
- **WHEN** file type is avatar and associated with User
- **THEN** access SHALL be granted to: file owner OR users with any role
- **WHEN** file type is normal_img or qrcode
- **THEN** access SHALL be public

### Requirement: Introduction image management
The IntroduceImage entity SHALL manage website introduction images with categorization.

#### Scenario: Image categorization
- **WHEN** creating an IntroduceImage
- **THEN** type MUST be one of: laboratory, equipment, team_photo, direction, competition, patent, paper
- **THEN** description SHALL provide detailed information about the image content
