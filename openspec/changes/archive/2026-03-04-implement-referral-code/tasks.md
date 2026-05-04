## 1. Database Migration

- [x] 1.1 Create migration script `V11__add_internal_referral_code_to_user.sql`
- [x] 1.2 Add `internal_referral_code VARCHAR(8)` column to `tb_user`
- [x] 1.3 Create partial unique index for non-null referral codes
- [x] 1.4 Generate referral codes for existing team members in migration

## 2. Entity Layer

- [x] 2.1 Add `internalReferralCode` field to `User` entity

## 3. Mapper Layer

- [x] 3.1 Add `selectByInternalReferralCode(String code)` method to `UserMapper` interface
- [x] 3.2 Add SQL query in `UserMapper.xml`

## 4. Domain Service

- [x] 4.1 Create `ReferralCodeGenerator` interface
- [x] 4.2 Implement `ReferralCodeGeneratorImpl` with random generation
- [x] 4.3 Implement uniqueness check with retry logic (max 10 retries)
- [x] 4.4 Implement `isValidFormat()` validation method

## 5. Bug Fix

- [x] 5.1 Fix `EnrollRepositoryImpl.getReferralUserName()` - change call from `selectByStudentId()` to `selectByInternalReferralCode()`
- [x] 5.2 Fix `EnrollRepositoryImpl.getReferralUserId()` - change call from `selectByStudentId()` to `selectByInternalReferralCode()`
- [x] Note: `selectByStudentId()` method is preserved, only the call site is changed

## 6. User Creation Logic

- [x] 6.1 Inject `ReferralCodeGenerator` into `EnrollDomainServiceImpl`
- [x] 6.2 Generate referral code when creating user from enrollment in `createUserFromEnrollment()`

## 7. Testing

- [x] 7.1 Write `ReferralCodeGeneratorImplTest` unit tests
- [x] 7.2 Update `EnrollRepositoryImplTest` to verify referral user lookup
- [x] 7.3 Run all tests to verify no regressions
