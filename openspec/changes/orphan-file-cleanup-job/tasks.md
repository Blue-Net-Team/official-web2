## 0. Schema - Add created_at to tb_file

- [ ] 0.1 Create Flyway migration `V11__add_file_created_at.sql` to add `created_at TIMESTAMP NOT NULL DEFAULT NOW()` to `tb_file`
- [ ] 0.2 Add `createdAt` field to `FileDO.java`
- [ ] 0.3 Add `createdAt` field to `File.java` domain entity
- [ ] 0.4 Ensure `prepareUpload` and `saveFile` populate `createdAt` with current time

## 1. Repository Layer - Query Orphan Files

- [ ] 1.1 Add `selectOrphanFiles` method to `FileMapper.java` interface
- [ ] 1.2 Implement `selectOrphanFiles` SQL in `FileMapper.xml` (JOIN coverage for 12 business table fields, PENDING timeout based on `created_at`, REJECTED, ACTIVE unreferenced)
- [ ] 1.3 Add `findOrphanFiles()` method to `FileRepository.java` interface
- [ ] 1.4 Implement `findOrphanFiles()` in `FileRepositoryImpl.java`
- [ ] 1.5 Write unit test for `FileMapper.selectOrphanFiles` verifying SQL returns correct orphan file IDs

## 2. Job Implementation

- [ ] 2.1 Create `OrphanFileCleanupJob.java` with `@Scheduled` annotation and cron configuration
- [ ] 2.2 Implement PENDING timeout logic (presigned expiry 15min + 1h buffer)
- [ ] 2.3 Implement REJECTED cleanup logic
- [ ] 2.4 Implement ACTIVE unreferenced cleanup logic via `FileRepository.findOrphanFiles()`
- [ ] 2.5 Implement per-file exception isolation (try-catch around each file deletion)
- [ ] 2.6 Implement safe deletion order: DB record first, OSS object second
- [ ] 2.7 Add logging for cleaned up files and failures
- [ ] 2.8 Write unit test for `OrphanFileCleanupJob` with mocked `FileRepository` and `ObjectStorage`

## 3. Configuration

- [ ] 3.1 Add `job.orphan-file-cleanup.cron` to `application.yml` with default `0 0 2 * * *`

## 4. Integration Test

- [ ] 4.1 Write integration test for orphan file cleanup end-to-end flow
- [ ] 4.2 Verify PENDING files older than threshold are cleaned up
- [ ] 4.3 Verify REJECTED files are cleaned up
- [ ] 4.4 Verify ACTIVE files with no references are cleaned up
- [ ] 4.5 Verify ACTIVE files with active references are NOT cleaned up
