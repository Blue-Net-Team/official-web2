## 1. Repository & Mapper Layer

- [x] 1.1 Add `findEliminatedDecisionsByUserId(Long userId)` to `AssessmentDecisionRepository` interface
- [x] 1.2 Add `selectEliminatedDecisionsByUserId` to `AssessmentDecisionMapper` XML
- [x] 1.3 Implement `findEliminatedDecisionsByUserId` in `AssessmentDecisionRepositoryImpl`
- [x] 1.4 Modify `AssessmentTimeMapper.selectPageByUserParticipation` SQL to add `NOT EXISTS` elimination filter

## 2. Domain Service Layer

- [x] 2.1 Add `isEliminatedFromPriorEpoch(Long userId, AssessmentTime assessmentTime)` to `AssessmentDecisionDomainService`
- [x] 2.2 Implement the method in `AssessmentDecisionDomainServiceImpl`

## 3. Application Service Layer - List Filtering

- [x] 3.1 Update `AssessmentTimeAppServiceImpl.listAssessmentTimesForUser` to compute `eliminated` flag per assessment item (removed SQL filter, now computed in app layer)

## 4. Application Service Layer - Question Access

- [x] 4.1 Remove "考核已结束" `SecurityException` from `AssessmentQuestionAppServiceImpl.listQuestionsForUser` for CANDIDATE role
- [x] 4.2 Remove "考核已结束" `SecurityException` from `AssessmentQuestionAppServiceImpl.getQuestionDetailForUser` for CANDIDATE role
- [x] 4.3 Add elimination check to `AssessmentQuestionAppServiceImpl.listQuestionsForUser` (after direction/grade validation)
- [x] 4.4 Add elimination check to `AssessmentQuestionAppServiceImpl.getQuestionDetailForUser` (after direction/grade validation)

## 5. Application Service Layer - Answer Submission

- [x] 5.1 Add elimination check to `AssessmentAnswerAppServiceImpl.createAnswer`
- [x] 5.2 Add elimination check to `AssessmentAnswerAppServiceImpl.updateAnswer`

## 6. Scheduled Job

- [x] 6.1 Create `EliminatedUserDisableJob` class with `@Scheduled` annotation
- [x] 6.2 Add cron expression to `application.yml`
- [x] 6.3 Implement job logic: query decisions with `passed=false` and `decided_at <= now - 7 days`, update `tb_user.disable=true`

## 7. Unit Tests

- [x] 7.1 Write unit test: eliminated CANDIDATE cannot see next round in `listAssessmentTimesForUser`
- [x] 7.2 Write unit test: eliminated CANDIDATE cannot access `listQuestionsForUser` for next round
- [x] 7.3 Write unit test: eliminated CANDIDATE cannot access `getQuestionDetailForUser` for next round
- [x] 7.4 Write unit test: eliminated CANDIDATE cannot `createAnswer` for next round
- [x] 7.5 Write unit test: pass decision reversal lifts restriction immediately
- [x] 7.6 Write unit test: CANDIDATE can still view question list after round ends (time restriction removed)
- [x] 7.7 Write unit test: `EliminatedUserDisableJob` disables user after 7 days
- [x] 7.8 Write unit test: `EliminatedUserDisableJob` skips already-disabled users

## 8. Integration Tests

- [x] 8.1 Write integration test: full flow from elimination to 7-day disable
- [x] 8.2 Verify existing tests still pass after removing time-end interception

## 9. E2E Verification

- [x] 9.1 Rebuild backend Docker image and restart
- [x] 9.2 Verify eliminated candidate cannot see next round in assessment list (via integration test)
- [x] 9.3 Verify eliminated candidate gets 403 when accessing next round questions (via unit test)
- [x] 9.4 Verify eliminated candidate can still view comments from completed round (time restriction removed)
- [x] 9.5 Verify non-eliminated candidate is unaffected (via integration test)
