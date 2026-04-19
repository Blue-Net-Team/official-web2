## 1. Configuration and Dependencies

- [x] 1.1 Add Aliyun OSS SDK dependency to the backend build.
- [x] 1.2 Introduce provider-neutral `storage` configuration properties with `enabled`, `provider`, and `bucket`.
- [x] 1.3 Move MinIO settings under `storage.minio` while preserving all required endpoint, port, access key, secret key, and SSL fields.
- [x] 1.4 Add Aliyun OSS settings under `storage.aliyun-oss`.
- [x] 1.5 Update backend application YAML files, Docker environment defaults, and `.env.example` files for the new storage configuration.

## 2. Storage Abstraction

- [x] 2.1 Add a provider-neutral object storage interface for put, get, delete, bucket initialization, and health checks.
- [x] 2.2 Add a shared object location resolver that maps `storage.bucket` plus `FileType.getValue()` and filename into bucket and object key.
- [x] 2.3 Refactor MinIO object operations behind the provider-neutral object storage interface.
- [x] 2.4 Implement Aliyun OSS object operations behind the same interface.
- [x] 2.5 Configure Spring bean selection so exactly one object storage adapter is active for the configured provider.

## 3. File Repository Boundary

- [x] 3.1 Refactor file metadata persistence and association queries into a provider-neutral file repository implementation.
- [x] 3.2 Delegate binary save, load, and delete operations from the file repository to the selected object storage adapter.
- [x] 3.3 Ensure file upload and download APIs keep their existing request and response contracts.
- [x] 3.4 Remove MinIO-specific conditional wiring from QR code repository and domain service components.

## 4. Bucket Initialization and Health

- [x] 4.1 Change MinIO startup initialization to ensure only `storage.bucket` exists.
- [x] 4.2 Add Aliyun OSS bucket validation or creation behavior according to adapter capabilities.
- [x] 4.3 Replace MinIO-only health reporting with selected-provider storage health reporting.
- [x] 4.4 Ensure startup fails clearly when `storage.provider` is unsupported or required provider settings are missing.

## 5. Tests

- [x] 5.1 Update MinIO/Testcontainers integration setup to use single bucket plus `FileType` object key prefixes.
- [x] 5.2 Add tests for object location resolution for all `FileType` values.
- [x] 5.3 Add tests for provider selection and unsupported provider startup failure.
- [x] 5.4 Add or update file save, load, and delete tests to assert bucket and object key behavior.
- [x] 5.5 Add coverage that QR code components remain available when the selected provider is not MinIO.

## 6. Documentation and Verification

- [x] 6.1 Update storage documentation to describe local MinIO and production Aliyun OSS configuration.
- [x] 6.2 Document the single-bucket layout and `FileType` prefix convention.
- [x] 6.3 Run targeted backend tests for file storage, QR code behavior, and affected configuration.
- [x] 6.4 Run `openspec status --change support-configurable-object-storage --json` and confirm the change is apply-ready.
