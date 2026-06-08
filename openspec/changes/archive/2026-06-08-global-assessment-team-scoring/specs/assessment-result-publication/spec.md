## MODIFIED Requirements

### Requirement: Publishing triggers email notification
The system SHALL send email notifications to decided candidates when results are published, with support for global assessments showing "全局" as the direction label.

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

#### Scenario: Global assessment final round uses correct result text
- **WHEN** results are published for a global assessment that is the final round (max epoch)
- **AND** a candidate has passed
- **THEN** the email SHALL use result text "录取" instead of "通过"
- **AND** when a candidate has been eliminated, the email SHALL use result text "淘汰" instead of "未通过"