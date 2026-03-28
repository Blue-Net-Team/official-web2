## Purpose

File event listener handling synchronization of file records with business entities (User, Enroll, AssessmentAnswer, AssessmentQuestion, QRCode).

## Requirements

### Requirement: Avatar save synchronization
The system SHALL update the User table's avatar_id field when an avatar file is saved.

#### Scenario: User avatar update
- **WHEN** FileSaveEvent with type AVATAR and businessType "user_avatar" is received
- **THEN** system SHALL update User.avatar_id to the new file ID
- **AND** system SHALL execute within a transaction

#### Scenario: Enroll avatar update
- **WHEN** FileSaveEvent with type AVATAR and businessType "enroll_avatar" is received
- **THEN** system SHALL update Enroll.avatar_id to the new file ID
- **AND** system SHALL execute within a transaction

### Requirement: QRCode save synchronization
The system SHALL update User table or QRCode table based on QRCode type when a QRCode file is saved.

#### Scenario: User WeChat QRCode update
- **WHEN** FileSaveEvent with type QRCODE and businessType "user_wechat_qrcode" is received
- **THEN** system SHALL update User.wechat_qrcode to the new file URL
- **AND** system SHALL create/update QRCode table record
- **AND** system SHALL execute within a transaction

#### Scenario: Other QRCode save
- **WHEN** FileSaveEvent with type QRCODE and businessType other than user_wechat_qrcode is received
- **THEN** system SHALL create/update QRCode table record only
- **AND** system SHALL execute within a transaction

### Requirement: Work file save synchronization
The system SHALL update the AssessmentAnswer table's file_id field when a work file is saved.

#### Scenario: Answer file attachment
- **WHEN** FileSaveEvent with type WORK is received
- **THEN** system SHALL update AssessmentAnswer.file_id to the new file ID
- **AND** system SHALL set AssessmentAnswer.submit_time to current time
- **AND** system SHALL execute within a transaction

### Requirement: Assessment attachment save synchronization
The system SHALL update the AssessmentQuestion table's attachment_id field when an attachment file is saved.

#### Scenario: Question attachment update
- **WHEN** FileSaveEvent with type ASSESSMENT_ATTACHMENT is received
- **THEN** system SHALL update AssessmentQuestion.attachment_id to the new file ID
- **AND** system SHALL execute within a transaction

### Requirement: Event handling error management
The system SHALL handle errors during event processing gracefully.

#### Scenario: Business record not found
- **WHEN** FileSaveEvent references a non-existent business record
- **THEN** system SHALL log an error
- **AND** system SHALL NOT propagate exception to prevent event bus disruption

#### Scenario: Database update failure
- **WHEN** database update fails during event handling
- **THEN** system SHALL rollback transaction
- **AND** system SHALL log an error with context
- **AND** system SHALL throw exception to trigger transaction rollback
