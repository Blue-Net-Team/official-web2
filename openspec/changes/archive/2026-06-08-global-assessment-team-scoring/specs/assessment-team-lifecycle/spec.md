## ADDED Requirements

### Requirement: Team lock after submission
The system SHALL lock a team once the leader submits any FILE_UPLOAD answer, preventing member departure, leader transfer, or team disbandment.

#### Scenario: Team locks after leader submits answer
- **WHEN** a team leader successfully submits a FILE_UPLOAD answer
- **THEN** the system SHALL mark the team as locked
- **AND** any subsequent attempt by a member to leave the team SHALL be rejected with a forbidden response
- **AND** any subsequent attempt by the leader to transfer leadership SHALL be rejected
- **AND** any subsequent attempt by the leader to disband the team SHALL be rejected

#### Scenario: Locked team prevents member exit
- **WHEN** a team member attempts to call `leaveTeam` for a locked team
- **THEN** the system SHALL reject the operation with error message "队伍已提交答案，无法退出"

#### Scenario: Locked team prevents leader transfer
- **WHEN** a team leader attempts to call `transferLeader` for a locked team
- **THEN** the system SHALL reject the operation with error message "队伍已提交答案，无法转让队长"

#### Scenario: Locked team prevents disbandment
- **WHEN** a team leader attempts to call `disbandTeam` for a locked team
- **THEN** the system SHALL reject the operation with error message "队伍已提交答案，无法解散"

### Requirement: Team disbandment cleanup
The system SHALL delete all answer and judgement records associated with a team when the team is disbanded (before locking).

#### Scenario: Disbanding team cleans up answers
- **WHEN** a team leader disbands a team that has not yet submitted any answers
- **THEN** the system SHALL mark the team as disbanded (existing behavior)
- **AND** if any answer records exist with `team_id` equal to the disbanded team, the system SHALL delete those answer records
- **AND** the system SHALL delete any judgement records associated with those answers

### Requirement: Cross-team membership restriction
The system SHALL prevent a candidate from joining a new team if they already have a team-associated answer for the same assessment time.

#### Scenario: Candidate with team answer cannot join another team
- **WHEN** a candidate who has a `team_id` associated answer for an assessment time attempts to join a different team for the same assessment time
- **THEN** the system SHALL reject the operation with error message "您已有队伍答案，无法加入其他队伍"

#### Scenario: Candidate without team answer can join
- **WHEN** a candidate who has no team-associated answer for an assessment time attempts to join a team
- **THEN** the system SHALL allow the operation if all other join conditions are satisfied

### Requirement: Team rules disclosure
The system SHALL display team rules and risk warnings to users in the team management interface.

#### Scenario: Team panel shows rules
- **WHEN** a user views the team panel (`TeamPanel.tsx`)
- **THEN** the system SHALL display a rules section containing:
  - "组队仅支持文件上传题，客观题需独立作答"
  - "队长提交答案后，队伍锁定，所有成员不可退出"
  - "队长解散队伍时，所有已提交答案将被删除"
  - "退出队伍后可重新加入原队伍或其他队伍"
  - "已有答案者不能加入新队伍"

#### Scenario: Disband confirmation shows warning
- **WHEN** a team leader initiates team disbandment
- **THEN** the confirmation dialog SHALL include warning text: "队伍解散后，所有已提交答案将被删除，此操作不可撤销。"

#### Scenario: Leave confirmation shows warning
- **WHEN** a team member initiates leaving a team
- **AND** the team has not yet submitted any answers
- **THEN** the confirmation dialog SHALL include note: "退出后可重新加入本队或其他队伍"