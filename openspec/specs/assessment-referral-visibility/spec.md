# Assessment Referral Visibility

## Requirements

### Requirement: Referral visibility in question submission list
The system SHALL display each candidate's referral status and referrer name in the admin per-question submission list (judge/score 逐题视图), and SHALL order referred candidates first within each team group.

#### Scenario: Submission list includes referral fields
- **WHEN** an admin queries the per-question submission list
- **THEN** each submission item SHALL include `internalReferralCode` (the code the candidate entered at enrollment, null if none)
- **AND** each submission item SHALL include `referralUserName` (the username of the member whose referral code matches, null if none or no match)

#### Scenario: Referred candidates ordered first within team group
- **WHEN** the submission list contains both referred and non-referred candidates in the same team
- **THEN** referred candidates SHALL be ordered before non-referred candidates within that team
- **AND** the relative order of teams SHALL remain unchanged
- **AND** candidates without a team SHALL remain ordered after all teams, with referred independent candidates ordered before non-referred independent candidates

#### Scenario: Leader ordering after referral ordering
- **WHEN** a team contains a referred candidate who is not the leader
- **THEN** the referred candidate SHALL be ordered before the leader within that team
- **AND** among candidates with the same referral status, the leader SHALL be ordered before other members, then by student ID ascending

### Requirement: Referral visibility in candidate scoreboard
The system SHALL display each candidate's referral status and referrer name in the admin candidate scoreboard (judge/score 积分榜视图), and SHALL order referred candidates first within each team group.

#### Scenario: Scoreboard includes referral fields
- **WHEN** an admin queries the candidate scoreboard for an assessment time
- **THEN** each candidate entry SHALL include `internalReferralCode` and `referralUserName` resolved from the candidate's enrollment record via student ID

#### Scenario: Referred candidates ordered first within team group
- **WHEN** the scoreboard contains both referred and non-referred candidates in the same team
- **THEN** referred candidates SHALL be ordered before non-referred candidates within that team
- **AND** the relative order of teams SHALL remain unchanged
- **AND** candidates without a team SHALL remain ordered after all teams, with referred independent candidates ordered first within the independent group

#### Scenario: Candidate without enrollment record
- **WHEN** a candidate user has no matching enrollment record (e.g., created via WPS form or admin)
- **THEN** the candidate's `internalReferralCode` and `referralUserName` SHALL be null
- **AND** the candidate SHALL be treated as non-referred for ordering

### Requirement: Referral visibility in decision workspace
The system SHALL display each candidate's referral status and referrer name in the admin decision workspace (judge/decision), and SHALL order referred candidates first.

#### Scenario: Decision candidate list includes referral fields
- **WHEN** an admin queries the decision workspace for an assessment time
- **THEN** each candidate entry SHALL include `internalReferralCode` and `referralUserName`

#### Scenario: Referred candidates ordered first
- **WHEN** the decision workspace candidate list contains both referred and non-referred candidates
- **THEN** referred candidates SHALL be ordered before non-referred candidates
- **AND** within each referral group candidates SHALL be ordered by student ID ascending

### Requirement: Invalid referral code handling in assessment views
The system SHALL treat a candidate whose referral code matches no member as NOT referred: no referral badge is displayed and the candidate is ordered as non-referred.

#### Scenario: Referral code with no matching member
- **WHEN** a candidate's enrollment record contains a referral code that matches no member's referral code
- **THEN** assessment list responses SHALL include that `internalReferralCode` value
- **AND** `referralUserName` SHALL be null
- **AND** the candidate SHALL be ordered as non-referred
- **AND** the frontend SHALL NOT display a referral badge for that candidate
