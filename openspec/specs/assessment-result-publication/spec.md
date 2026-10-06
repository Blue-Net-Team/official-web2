## MODIFIED Requirements

### Requirement: Publishing triggers email notification
The system SHALL send email notifications to decided candidates when results are published, with support for global assessments showing "全局" as the direction label. Exception: candidates who pass the global final assessment SHALL receive the admission welcome email instead of the result notification email.

#### Scenario: Publish sends emails to decided candidates
- **WHEN** results are published for an assessment time
- **AND** there are candidates with decisions for that assessment time
- **THEN** the system SHALL send an HTML email to each decided candidate containing their pass/eliminate result
- **AND** the email SHALL display the direction label as "全局" when the assessment time's direction is null
- **AND** the email SHALL display the appropriate direction description when direction is not null
- **AND** the system SHALL return the count of emails sent

#### Scenario: Publish with no decided candidates
- **WHEN** results are published for an assessment time with no decided candidates
- **THEN** the system SHALL set the publication timestamp without sending emails
- **AND** the system SHALL return count 0

#### Scenario: Global final round pass is excluded from result notification
- **WHEN** results are published for a global assessment that is the final round (max epoch)
- **AND** a candidate has passed
- **THEN** the system SHALL NOT send the result notification email to that candidate
- **AND** the system SHALL send the admission welcome email instead

### Requirement: Global assessment final round uses correct result text
When results are published for a global assessment that is the final round (max epoch), the result notification email SHALL use result text "淘汰" instead of "未通过" for eliminated candidates. The "录取" result text for passed candidates no longer applies, as those candidates receive the admission welcome email instead of a result notification.

#### Scenario: Final round eliminated uses correct result text
- **WHEN** results are published for a global assessment that is the final round (max epoch)
- **AND** a candidate has been eliminated
- **THEN** the email SHALL use result text "淘汰" instead of "未通过"
