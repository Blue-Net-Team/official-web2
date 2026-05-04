## MODIFIED Requirements

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
