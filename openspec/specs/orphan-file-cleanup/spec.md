## ADDED Requirements

### Requirement: System SHALL automatically clean up orphan file records
The system SHALL provide a scheduled job that scans `tb_file` periodically and removes records that are no longer needed, along with their corresponding object storage objects.

#### Scenario: PENDING file exceeds timeout
- **WHEN** a file record has `status = PENDING` and its creation time exceeds the configured pending timeout (presigned upload expiry + buffer)
- **THEN** the system SHALL delete the file record from `tb_file`
- **AND** the system SHALL attempt to delete the corresponding object from object storage

#### Scenario: REJECTED file is cleaned up
- **WHEN** a file record has `status = REJECTED`
- **THEN** the system SHALL delete the file record from `tb_file`
- **AND** the system SHALL attempt to delete the corresponding object from object storage

#### Scenario: ACTIVE file has no business references
- **WHEN** a file record has `status = ACTIVE` and its `id` is not referenced by any of the following fields:
  - `tb_user.avatar_id`
  - `tb_user.qrcode_id`
  - `tb_enroll.avatar_id`
  - `tb_achievement.file_id`
  - `tb_assessment_question.attachment_id`
  - `tb_assessment_answer.file_id`
  - `tb_qrcode.file_id`
  - `tb_competition.logo_file_id`
  - `tb_competition.cover_file_id`
  - `tb_venue.image_file_id`
  - `tb_equipment.image_file_id`
  - `tb_bug_report_image.file_id`
- **THEN** the system SHALL delete the file record from `tb_file`
- **AND** the system SHALL attempt to delete the corresponding object from object storage

### Requirement: Cleanup job SHALL run on a configurable schedule
The system SHALL execute the orphan file cleanup job according to a configurable cron expression.

#### Scenario: Default schedule execution
- **WHEN** the application is running and the cron expression triggers
- **THEN** the cleanup job SHALL execute at the configured time

### Requirement: Cleanup failures SHALL be isolated
The system SHALL ensure that a failure to clean up one file does not prevent the cleanup of other files.

#### Scenario: Object storage deletion fails for one file
- **WHEN** the cleanup job attempts to delete an object from storage and the deletion fails
- **THEN** the system SHALL log the failure
- **AND** the system SHALL continue processing remaining orphan files

#### Scenario: Database deletion fails for one file
- **WHEN** the cleanup job attempts to delete a file record and the deletion fails
- **THEN** the system SHALL log the failure
- **AND** the system SHALL skip the object storage deletion for that file
- **AND** the system SHALL continue processing remaining orphan files
