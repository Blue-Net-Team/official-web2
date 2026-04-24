## 1. Backend Core Persistence

- [x] 1.1 Inject `MessageTemplateMapper` into `MessageTemplateRegistry`
- [x] 1.2 Implement `@PostConstruct` database loading for content, subject, and enabled overrides
- [x] 1.3 Implement `updateContent()` with synchronous database upsert
- [x] 1.4 Implement `updateSubject()` with synchronous database upsert
- [x] 1.5 ~~Implement `setEnabled()` with synchronous database upsert~~ (removed: disabled feature deleted)
- [x] 1.6 Implement `getTemplateSubject()` with database override fallback
- [x] 1.7 Update `MessageTemplateAppServiceImpl.updateTemplate()` to pass subject to registry

## 2. Testing

- [x] 2.1 Update `EmailVerificationCodeTemplateTest` to inject mock `MessageTemplateMapper`
- [x] 2.2 Update `EnrollmentRejectionTemplateTest` to inject mock `MessageTemplateMapper`
- [x] 2.3 Create `MessageTemplateRegistryPersistenceTest` integration test for startup loading
- [x] 2.4 Create integration test for content edit persistence across restarts
- [x] 2.5 Create integration test for subject edit persistence across restarts
- [x] 2.6 ~~Create integration test for enabled status persistence across restarts~~ (removed)
- [x] 2.7 Create integration test for fallback to default when database record is removed
- [x] 2.8 Run full test suite `./mvnw test` and verify all pass (840 tests, 0 failures)

## 3. Frontend Verification

- [x] 3.1 Start application and edit a template content via admin UI
- [x] 3.2 Restart application and verify the edited content is retained
- [x] 3.3 ~~Disable a template via admin UI~~ (removed: disabled feature deleted)
