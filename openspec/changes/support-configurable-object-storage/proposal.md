## Why

The backend currently couples file storage behavior to MinIO and uses each `FileType` value as a separate bucket. This blocks production deployment to Aliyun OSS and makes local MinIO and cloud OSS use different operational models.

MinIO has no retained data in the current environment, so the storage layout can be changed directly without a legacy object migration.

## What Changes

- Add a provider-selectable object storage configuration that can use MinIO or Aliyun OSS.
- Introduce a single-bucket storage model:
  - bucket name comes from configuration.
  - `FileType.getValue()` is used as the object key prefix inside the bucket.
  - example: `bucket=bluenet`, `key=avatar/avatar-uuid.png`.
- Refactor storage-specific operations behind an object storage strategy/adapter boundary.
- Keep file metadata and business association queries independent from the concrete storage provider.
- Update MinIO startup initialization to ensure only the configured bucket exists instead of creating one bucket per `FileType`.
- Remove MinIO-specific conditional wiring from non-MinIO business components such as QR code repository/domain service.
- Add Aliyun OSS SDK dependency and configuration support.
- **BREAKING**: Existing MinIO object layout changes from multiple `FileType` buckets to one configured bucket with `FileType` prefixes. No compatibility read path is required because the current MinIO environment has no data to preserve.

## Capabilities

### New Capabilities
- `backend-object-storage-provider`: Configurable object storage provider selection, shared bucket/key resolution, and provider-neutral storage operations.

### Modified Capabilities
- `backend-minio-file-storage`: MinIO storage SHALL use the configured single bucket and `FileType` key prefixes instead of using `FileType` values as bucket names.

## Impact

- Backend configuration:
  - replace MinIO-only enablement with provider-neutral storage settings.
  - add Aliyun OSS credentials and endpoint settings.
  - change MinIO bucket configuration to a single `storage.bucket`.
- Backend infrastructure:
  - storage configuration classes.
  - MinIO client setup and bucket initialization.
  - new Aliyun OSS client setup.
  - object storage strategy/adapter interface and implementations.
  - file repository implementation boundary.
  - storage health indicator.
- Backend dependencies:
  - add Aliyun OSS SDK.
- Tests:
  - update MinIO/Testcontainers expectations to single-bucket + prefix layout.
  - add provider selection and key resolution tests.
- Documentation and environment examples:
  - update storage environment variables.
  - document local MinIO and production Aliyun OSS configuration.
