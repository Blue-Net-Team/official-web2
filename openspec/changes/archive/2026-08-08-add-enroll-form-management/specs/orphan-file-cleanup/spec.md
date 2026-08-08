# 孤儿文件清理（orphan-file-cleanup）变更

## MODIFIED Requirements

### Requirement: System SHALL automatically clean up orphan file records
The system SHALL provide a scheduled job that scans `tb_file` periodically and removes records that are no longer needed, along with their corresponding object storage objects. Records with `type = 'enroll-form'` SHALL be excluded from the ACTIVE-orphan check: enroll-form files are not referenced by any business table (no reference table by design) and their lifecycle is managed explicitly by the enroll-form management endpoints, so the cleanup job MUST NOT delete them.

#### Scenario: PENDING file exceeds timeout
- **WHEN** a file record has `status = PENDING` and its creation time exceeds the configured pending timeout (presigned upload expiry + buffer)
- **THEN** the system SHALL delete the file record from `tb_file`
- **AND** the system SHALL attempt to delete the corresponding object from object storage

#### Scenario: REJECTED file is cleaned up
- **WHEN** a file record has `status = REJECTED`
- **THEN** the system SHALL delete the file record from `tb_file`
- **AND** the system SHALL attempt to delete the corresponding object from object storage

#### Scenario: ACTIVE file has no business references
- **WHEN** a file record has `status = ACTIVE`, its `type` is not `'enroll-form'`, and its `id` is not referenced by any of the following fields:
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

#### Scenario: ACTIVE enroll-form file is never treated as orphan
- **WHEN** a file record has `status = ACTIVE` and `type = 'enroll-form'`
- **THEN** the cleanup job SHALL NOT delete the file record regardless of business references
- **AND** the file SHALL remain downloadable until explicitly deleted via the enroll-form management endpoint
