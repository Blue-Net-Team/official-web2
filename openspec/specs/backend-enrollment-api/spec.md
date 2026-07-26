# Enrollment API

## ADDED Requirements

### Requirement: Enrollment list displays referral information
The system SHALL include referral information in enrollment list items, resolved in the list query itself without per-row lookups.

#### Scenario: List item with valid referral code
- **WHEN** an admin queries the enrollment list
- **AND** an enrollment has a referral code matching a member's referral code
- **THEN** that list item SHALL include `internalReferralCode` with the entered code
- **AND** that list item SHALL include `referralUserName` with the referring member's username

#### Scenario: List item with invalid or no referral code
- **WHEN** an admin queries the enrollment list
- **AND** an enrollment has no referral code, or a referral code matching no member
- **THEN** that list item SHALL include `internalReferralCode` as entered (or null)
- **AND** that list item SHALL include `referralUserName` as null

#### Scenario: List query performance
- **WHEN** an admin queries any page of the enrollment list
- **THEN** the system SHALL resolve referrer names within the paged SQL query via JOIN
- **AND** the system SHALL NOT execute per-row queries to resolve referrer names

### Requirement: Referred enrollments ordered first in list
The system SHALL order enrollments with a valid referral code (one that matches a member's referral code) before other enrollments in the admin enrollment list. Enrollments with an invalid referral code or no code are treated as non-referred.

#### Scenario: Mixed referral status ordering
- **WHEN** an admin queries the enrollment list containing referred, invalid-code, and non-referred enrollments
- **THEN** enrollments whose `internal_referral_code` matches a member's referral code SHALL appear before all other enrollments
- **AND** enrollments with an invalid or empty referral code SHALL be ordered among non-referred enrollments
- **AND** within each group enrollments SHALL be ordered by id descending

## Requirements

### Requirement: Enrollment DTOs contain avatar file ID
The system SHALL return avatar file ID instead of avatar URL in all enrollment-related DTOs.

#### Scenario: Get enrollment detail with avatar
- **WHEN** GET /api/v1/enrollments/{id} is called for enrollment with avatar
- **THEN** response SHALL include `avatarFileId` field with the file ID value
- **AND** response SHALL NOT include `avatarUrl` field

#### Scenario: Get enrollment list with avatars
- **WHEN** GET /api/v1/enrollments is called
- **THEN** each enrollment item SHALL include `avatarFileId` field
- **AND** response SHALL NOT include `avatarUrl` field

#### Scenario: Get enrollment without avatar
- **WHEN** viewing enrollment without avatar file
- **THEN** `avatarFileId` SHALL be null

### Requirement: Display referral user name in enrollment details
The system SHALL display the name of the referring team member when viewing enrollment details, if a valid referral code was provided during registration.

#### Scenario: Enrollment with valid referral code
- **WHEN** viewing enrollment details for an enrollment with referral code "ABC12345"
- **AND** a team member with referral code "ABC12345" exists
- **THEN** the response includes `referralUserName` with the team member's name
- **AND** the response includes `referralUserId` with the team member's user ID

#### Scenario: Enrollment with invalid referral code
- **WHEN** viewing enrollment details for an enrollment with referral code "NOTEXIST"
- **AND** no team member has that referral code
- **THEN** the response includes `referralUserName` as null
- **AND** the response includes `referralUserId` as null

#### Scenario: Enrollment without referral code
- **WHEN** viewing enrollment details for an enrollment without a referral code
- **THEN** the response includes `referralUserName` as null
- **AND** the response includes `referralUserId` as null
