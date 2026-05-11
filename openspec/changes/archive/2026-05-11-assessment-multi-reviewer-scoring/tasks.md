## 1. Database and Domain Foundation

- [x] 1.1 Add `results_published_at` column to `tb_assessment_time` via Flyway migration
- [x] 1.2 Add unique index `uk_comment_answer_user` on `tb_comment(answer_id, user_id)` via Flyway migration
- [x] 1.3 Update `AssessmentTime` entity/DO/VO to include `resultsPublishedAt` field
- [x] 1.4 Update `Comment` domain entity with factory method and behavioral methods (`create`, `reconstruct`)
- [x] 1.5 Add `ADMIN_FINALIZED` to `JudgementSource` enum

## 2. Comment Repository and Domain Service

- [x] 2.1 Implement `CommentRepository` interface with `save`, `findByAnswerId`, `findById`, `existsByAnswerIdAndUserId`, `deleteById`, `update` methods
- [x] 2.2 Implement `CommentRepositoryImpl` with MyBatis-Plus `CommentMapper`
- [x] 2.3 Implement `CommentDomainService` with `addComment` (enforce one-per-user-per-answer), `listCommentsByAnswerId`, `updateComment` (verify ownership), `deleteComment` (verify ownership)
- [x] 2.4 Write unit tests for `CommentDomainService`

## 3. Admin Finalized Judgement Domain Service

- [x] 3.1 Extend `AssessmentJudgementDomainService` with `finalizeJudgement` method (insert `ADMIN_FINALIZED` record)
- [x] 3.2 Add validation: direction administrator must have commented before finalizing
- [x] 3.3 Update scoreboard/latest queries to prefer `ADMIN_FINALIZED` over `MANUAL` when both exist
- [x] 3.4 Write unit tests for finalized judgement logic

## 4. Application Layer

- [x] 4.1 Create `CommentAppService` with `addComment`, `listComments`, `updateComment`, `deleteComment` methods
- [x] 4.2 Create `AssessmentJudgementAppService.finalizeScore` command handler
- [x] 4.3 Update `AssessmentJudgementAppService.publishDecisions` to set `results_published_at`
- [x] 4.4 Update `AssessmentJudgementAppService.decideAssessment` to auto-set `results_published_at` on decision
- [x] 4.5 Write integration tests for application service layer

## 5. API Controller

- [x] 5.1 Add `POST /api/v1/admin/comments` endpoint for adding comments with `@RequiresPermission`
- [x] 5.2 Add `GET /api/v1/admin/comments?answerId={id}` endpoint for listing comments
- [x] 5.3 Add `PUT /api/v1/admin/comments/{id}` endpoint for comment owners to update their comment
- [x] 5.4 Add `DELETE /api/v1/admin/comments/{id}` endpoint for comment owners to delete their comment
- [x] 5.5 Add `POST /api/v1/admin/judgements/finalize` endpoint for direction administrator finalizing score
- [x] 5.6 Update `POST /api/v1/admin/decisions/publish` to accept optional `publish` flag and set timestamp
- [x] 5.7 Ensure all new permission values are globally unique
- [x] 5.8 Write controller-level integration tests

## 6. Frontend Admin Panel

- [x] 6.1 Add comment list UI to score panel showing all member comments with score
- [x] 6.2 Add comment form for current user (hidden if already commented)
- [x] 6.3 Add edit and delete actions on each comment visible only to the comment owner
- [x] 6.4 Add "确认最终评分" form for direction administrators with score input and comment
- [x] 6.5 Update publish button to call updated publish API and show published state
- [x] 6.6 Update `AssessmentQuestionSubmissionDTO` and related types to include `comments` and `finalizedJudgement`

## 7. Candidate Visibility and Email

- [x] 7.1 Update candidate result query to include comments and finalized score only when `results_published_at` is set
- [x] 7.2 Update candidate frontend to display comments and final score after publication

## 8. End-to-End Verification

- [x] 8.1 Run backend compilation and package (`mvnw clean compile package`)
- [x] 8.2 Build and run backend Docker image
- [x] 8.3 Start frontend dev server (check port 3000)
- [x] 8.4 Use Playwright to verify: member comments → admin finalizes → publish → candidate sees results → email sent
