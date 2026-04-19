## Context

The backend currently stores file metadata in the database and stores binary objects through MinIO. The existing `MinioFileRepository` mixes database metadata operations with MinIO object operations, and MinIO-specific conditional wiring is also used by QR code components that are not inherently MinIO-specific.

The current storage layout uses one bucket per `FileType`: `avatar`, `normal-img`, `assessment-attachment`, `work`, and `qrcode`. The target deployment needs provider-level configuration so the same application can run against local MinIO or Aliyun OSS. The current MinIO environment has no retained objects, so the object layout can change directly.

## Goals / Non-Goals

**Goals:**
- Support selecting MinIO or Aliyun OSS through configuration.
- Use one configured bucket for all object storage providers.
- Store objects under a `FileType.getValue()` prefix inside the configured bucket.
- Keep file metadata persistence and business association queries independent from provider-specific SDK code.
- Keep local MinIO behavior and production Aliyun OSS behavior consistent.
- Remove MinIO-specific enablement from QR code repository/domain service wiring.

**Non-Goals:**
- No legacy multi-bucket MinIO object compatibility path.
- No database schema migration for existing file metadata.
- No frontend API contract changes for file upload/download.
- No runtime per-request provider switching.
- No new business-level file permission model.

## Decisions

### Use provider-neutral storage configuration

Introduce a `storage` configuration namespace as the source of truth:

```yaml
storage:
  enabled: true
  provider: minio
  bucket: bluenet
  minio:
    endpoint: localhost
    port: 9000
    accessKey: admin
    secretKey: admin1234
    useSSL: false
  aliyun-oss:
    endpoint: oss-cn-example.aliyuncs.com
    accessKeyId: ""
    accessKeySecret: ""
```

Rationale: `minio.enabled` describes one provider, not the storage capability. A provider-neutral root avoids leaking MinIO terminology into Aliyun OSS mode and prevents non-MinIO components from being disabled accidentally.

Alternative considered: keep `minio.enabled` and add `aliyun-oss.enabled`. This creates ambiguous states when both are true or false and spreads selection logic across configuration classes.

### Use a single bucket with `FileType` key prefixes

All providers use:

```text
bucket = storage.bucket
objectKey = fileType.getValue() + "/" + filename
```

Example:

```text
bucket: bluenet
key: avatar/avatar-uuid.png
key: work/work-uuid.zip
key: qrcode/qrcode-uuid.png
```

Rationale: one bucket is simpler to configure for Aliyun OSS permissions, CORS, lifecycle rules, and CDN integration. It also keeps MinIO and OSS behavior identical.

Alternative considered: preserve one bucket per `FileType`. This matches the current MinIO implementation but carries unnecessary cloud bucket management overhead and keeps the design tied to the old MinIO shape.

### Split object storage operations from file metadata repository

Introduce a provider-neutral storage port for binary object operations. The file repository should own database file metadata and association queries, then delegate binary save/load/delete to the selected storage adapter.

```text
FileDomainService
       |
       v
FileRepository
  - file table CRUD
  - assessment association queries
       |
       v
ObjectStorage
  - put/get/delete/ensureBucket/health
       |
       +-- MinIO adapter
       +-- Aliyun OSS adapter
```

Rationale: duplicating the entire `FileRepository` for each provider would also duplicate unrelated database queries such as assessment answer/question lookups.

Alternative considered: add `AliyunOssFileRepository implements FileRepository`. This is faster initially but makes storage provider choice leak into database repository responsibilities.

### Initialize only the configured bucket

Startup initialization SHALL ensure `storage.bucket` exists for the selected provider. It SHALL NOT create one bucket per `FileType`.

Rationale: `FileType` now defines object key prefixes, not bucket names.

### Health check follows selected provider

Replace MinIO-only health behavior with a storage health indicator that reports the selected provider and validates connectivity using the selected adapter.

Rationale: actuator health should remain meaningful when the provider is Aliyun OSS.

## Risks / Trade-offs

- Existing MinIO objects become unreadable after the layout change -> accepted because the current MinIO environment has no data to preserve.
- Aliyun OSS bucket creation may require permissions not granted to the runtime key -> make bucket auto-creation provider-aware and fail with a clear startup error, or document pre-created bucket requirements for production.
- Single bucket reduces physical separation between file types -> keep logical separation through `FileType` prefixes and existing application access checks.
- Configuration migration may break deployments that only define `MINIO_*` variables -> update `.env.example`, Docker config, and docs in the same change; consider compatibility aliases only if deployment needs them.
- SDK-specific exceptions may leak into domain/application code -> translate provider exceptions at the adapter boundary.

## Migration Plan

1. Introduce provider-neutral configuration and selected adapter wiring.
2. Change MinIO initialization to ensure the configured single bucket.
3. Change object key resolution to use `FileType` prefixes.
4. Add Aliyun OSS adapter and dependency.
5. Update tests, Docker/env examples, and documentation.
6. Deploy with `storage.provider=minio` locally and `storage.provider=aliyun-oss` in production-like environments.

Rollback: switch code/config back to the previous MinIO-only implementation before any production file data is written with the new layout. Once production data exists under the new layout, rollback requires moving objects back to the old per-`FileType` buckets.

## Open Questions

- Should production Aliyun OSS bucket creation be attempted automatically, or should the bucket be pre-created and only validated at startup?
- Should old `MINIO_*` environment variable names remain supported as compatibility aliases under the new `storage.minio` configuration?
