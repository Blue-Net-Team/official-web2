## ADDED Requirements

### Requirement: 淘汰后立即限制考生参与后续考核
The system SHALL prevent a CANDIDATE who has been eliminated (`passed = false`) in any prior epoch of the same direction+grade combination from accessing subsequent assessment rounds.

A CANDIDATE is considered eliminated for a given `(direction, grade)` when there exists an `AssessmentDecision` with `passed = false` for any `AssessmentTime` matching that direction and grade.

#### Scenario: Eliminated candidate can see next round but marked eliminated
- **WHEN** a CANDIDATE eliminated in epoch=1 calls `GET /api/v1/assessment-times`
- **THEN** the response SHALL include all assessment times matching direction+grade, including epoch > 1 and epoch = 0
- **AND** assessment times with epoch > 1 (or epoch = 0) for the same direction+grade SHALL have `eliminated = true`
- **AND** the frontend card UI SHALL display "已被淘汰" and be disabled
- **AND** the response SHALL still include epoch=1 with `eliminated = false` (the round they participated in)

#### Scenario: Eliminated candidate cannot enter next round question list
- **WHEN** a CANDIDATE eliminated in epoch=1 attempts to access `GET /api/v1/assessment-questions?assessmentTimeId={epoch2_id}`
- **THEN** the system SHALL reject with a 403 forbidden response

#### Scenario: Eliminated candidate cannot view next round question detail
- **WHEN** a CANDIDATE eliminated in epoch=1 attempts to access `GET /api/v1/assessment-questions/{question_id}` where the question belongs to epoch=2
- **THEN** the system SHALL reject with a 403 forbidden response

#### Scenario: Eliminated candidate cannot submit answers for next round
- **WHEN** a CANDIDATE eliminated in epoch=1 attempts to `POST /api/v1/assessment-answers` for a question in epoch=2
- **THEN** the system SHALL reject with a 403 forbidden response

#### Scenario: Eliminated candidate can still view comments for completed round
- **WHEN** a CANDIDATE eliminated in epoch=1 calls `GET /api/v1/assessment-answers?questionId={epoch1_question_id}`
- **THEN** the system SHALL return their answer and member comments normally

#### Scenario: Pass decision reverses elimination restriction
- **WHEN** an administrator changes a candidate's decision from `passed=false` to `passed=true`
- **THEN** the elimination restriction SHALL be immediately lifted
- **AND** the candidate SHALL be able to see and participate in subsequent rounds

### Requirement: 7天后自动禁用被淘汰考生账号
The system SHALL automatically disable (`disable = true`) any user who has been eliminated (`passed = false`) for more than 7 days.

The 7-day grace period SHALL be calculated from `tb_assessment_decision.decided_at`.

#### Scenario: Eliminated candidate disabled after 7 days
- **WHEN** a candidate was eliminated with `decided_at = 2026-06-01 10:00:00`
- **AND** the scheduled job runs on `2026-06-08 10:00:01`
- **THEN** the system SHALL set `tb_user.disable = true` for that user

#### Scenario: Eliminated candidate not disabled before 7 days
- **WHEN** a candidate was eliminated with `decided_at = 2026-06-01 10:00:00`
- **AND** the scheduled job runs on `2026-06-07 23:59:59`
- **THEN** the system SHALL NOT modify `tb_user.disable`

#### Scenario: Already disabled user is skipped
- **WHEN** a candidate was already disabled (`disable = true`) before the job runs
- **THEN** the job SHALL skip that user (idempotent operation)

#### Scenario: Reversed pass decision prevents disable
- **WHEN** a candidate's decision was changed from `passed=false` to `passed=true` before the 7-day mark
- **THEN** the scheduled job SHALL NOT disable that user
