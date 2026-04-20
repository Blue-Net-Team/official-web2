## 1. Backend DTOs and Query Contracts

- [x] 1.1 Add DTOs for question scoreboard rows, question submission rows, candidate score rows, per-question score states, decision workspace rows, and decision statistics.
- [x] 1.2 Add query DTOs or request parameter handling for `assessmentTimeId`, `questionType`, `keyword`, `status`, and `decisionStatus`.
- [x] 1.3 Ensure DTO fields include candidate identity data, answer identifiers, file identifiers, submit time, display judgement, submission history, total score, max score, judged count, pending count, and decision status.

## 2. Backend Aggregation Queries

- [x] 2.1 Add repository or mapper queries for display judgement per answer and per candidate-question pair, using best judged record for algorithm questions and latest judged record for other question types.
- [x] 2.2 Add query support for question scoreboard metrics by assessment time.
- [x] 2.3 Add query support for all submissions under one question with candidate information, display judgement, and expandable judgement history.
- [x] 2.4 Add query support for candidate score matrix by assessment time.
- [x] 2.5 Add query support for decision workspace rows and statistics by assessment time.
- [x] 2.6 Enforce direction scope for DIRECTION_ADMIN aggregation queries and allow SUPER_ADMIN broader access.

## 3. Backend Service and Controller

- [x] 3.1 Extend `AssessmentJudgementService` with aggregation query methods for scoreboards and decision workspace data.
- [x] 3.2 Implement the aggregation methods in `AssessmentJudgementServiceImpl` without changing existing manual-review and decision write semantics.
- [x] 3.3 Add admin endpoints under `/api/v1/admin/assessment-judgements/scoreboard/*` for question scoreboard, question submissions, and candidate scoreboard.
- [x] 3.4 Add a admin read endpoint under `/api/v1/admin/assessment-judgements/decisions` for decision workspace data.
- [x] 3.5 Keep `POST /api/v1/admin/assessment-judgements/manual-review` as the only manual scoring write path.
- [x] 3.6 Keep `POST /api/v1/admin/assessment-judgements/decisions` as the single-candidate decision write path and verify repeated decisions overwrite correctly.

## 4. Backend Tests

- [x] 4.1 Add tests for question scoreboard aggregation with submitted, judged, and pending counts.
- [x] 4.2 Add tests for question submissions returning candidate identity, answer data, display judgement, and history selection marker.
- [x] 4.3 Add tests for candidate score matrix total score, max score, pending count, and per-question states.
- [x] 4.4 Add tests for decision workspace statistics: candidates, pending decisions, passed, and eliminated.
- [x] 4.5 Add tests that DIRECTION_ADMIN cannot read out-of-direction aggregation data.
- [x] 4.6 Run targeted backend tests for assessment judgement, assessment answer, assessment decision, and aggregation behavior.

## 5. Frontend API Types and Services

- [x] 5.1 Extend `assessment.dto.ts` with scoreboard, submission, candidate score, decision workspace, and statistics DTO types.
- [x] 5.2 Extend `admin-assessment-judgement.service.ts` with methods for question scoreboard, question submissions, candidate scoreboard, decision workspace, manual review, and decision saving.
- [x] 5.3 Reuse existing `adminAssessmentTimeService`, `adminAssessmentQuestionService`, and `fileService.downloadFile` where possible.

## 6. Score Management UI

- [x] 6.1 Create the assessment judgement score page under `/admin/assessment/judge/score`.
- [x] 6.2 Add direction and assessment time selectors, with DIRECTION_ADMIN restricted to their own direction.
- [x] 6.3 Add AntD `Tabs` for question view and candidate view.
- [x] 6.4 Implement question view with question list, submission `Table`, expandable history rows, no operation column, row click behavior, loading state, and empty state.
- [x] 6.5 Implement score `Drawer` using AntD `Drawer`, `Form`, `InputNumber`, and `Input.TextArea`.
- [x] 6.6 In the drawer, allow editable scoring only for file upload submissions and show objective judgements as read-only.
- [x] 6.7 Implement candidate view with clickable candidate rows, candidate score matrix, total score, judged count, pending count, search, loading state, and empty state.

## 7. Decision Workspace UI

- [x] 7.1 Create the assessment decision page under `/admin/assessment/judge/decision`.
- [x] 7.2 Add direction and assessment time selectors before loading decision data.
- [x] 7.3 Add statistic cards for candidates, pending decisions, passed, and eliminated.
- [x] 7.4 Implement candidate decision list with score evidence, judged count, pending count, current decision status, and search/status filters.
- [x] 7.5 Implement candidate detail panel or drawer showing question results and optional decision comment.
- [x] 7.6 Wire "通过" and "淘汰" actions to the existing decision save API and refresh the affected row/statistics after success.
- [x] 7.7 Add "发布本轮结果" entry that clearly indicates notification delivery is not implemented and does not send emails.

## 8. Navigation, Permissions, and UX Polish

- [x] 8.1 Update admin navigation so score management and decision workspace are separate assessment menu items.
- [x] 8.2 Ensure page styling follows the existing dark AntD theme and avoids extra page-level sidebar.
- [x] 8.3 Ensure table row click, drawer close, refresh, and error messages use existing project interaction patterns.
- [x] 8.4 Verify mobile behavior uses responsive tables or drawer widths without breaking the admin layout.

## 9. Verification

- [x] 9.1 Run frontend type checking for the changed frontend files.
- [x] 9.2 Run targeted backend tests for the new aggregation service/controller behavior.
- [x] 9.3 Manually verify the score workflow: select direction/time, open question view, expand algorithm histories, click row, submit file-upload score, refresh display judgement.
- [x] 9.4 Manually verify the candidate view workflow: search candidate, inspect per-question states, confirm total/pending counts.
- [x] 9.5 Manually verify the decision workflow: click pass, click eliminate, confirm automatic save, confirm statistics update, confirm publish entry does not send notifications.
