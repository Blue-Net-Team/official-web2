## Why

当前文件上传采用后端代理模式（前端→Spring Boot→OSS），受限于 `spring.servlet.multipart.max-file-size: 5MB`，且阿里云 OSS `putObject(InputStream)` 在不指定 Content-Length 时会将整流缓冲到内存。在 80 并发 × 200MB 单文件上限的业务场景下，后端代理会导致磁盘 I/O 打满、JVM 堆内存溢出（80 × 200MB = 16GB 峰值）、Tomcat 连接超时。批量下载 ZIP 当前使用 `ByteArrayOutputStream` 全内存打包，多个 150MB+ 文件同样会 OOM。必须将大流量（上传/下载）从后端解脱出来。

## What Changes

- **新增预签名上传流程**：后端提供 `POST /api/v1/file/prepare-upload` 接口，生成阿里云 OSS / MinIO 预签名 PUT URL，前端直传 OSS，绕过后端带宽瓶颈
- **新增上传确认接口**：`POST /api/v1/file/confirm-upload`，前端直传完成后回调，后端校验 ETag/Content-Type/大小，通过后才写入 `tb_file` 元数据
- **改造下载接口**：`GET /api/v1/file/download/{fileId}` 权限校验后返回 302 Redirect 到预签名 GET URL（阿里云）或生成临时 URL（MinIO），文件流量不再经过后端
- **改造批量下载 ZIP**：`ByteArrayOutputStream` 全内存方案改为 `ZipOutputStream` 直接写入 `ServletOutputStream` 流式输出，内存占用从 Σ(文件大小) 降到固定 8KB buffer
- **修改 Spring Multipart 限制**：`max-file-size` 和 `max-request-size` 提升至 500MB（过渡期内兼容旧接口）
- **废弃旧上传接口**：`POST /api/v1/file/upload` 标记为 @Deprecated，保留向后兼容但引导迁移至预签名流程
- **阿里云 OSS 大文件分片上传支持**：`AliyunOssObjectStorage` 在传入流可获取大小时自动使用 Multipart Upload，避免内存缓冲
- **判题服务同步改造**：`AliyunOssJudgeAssetStorage` 补充 `ObjectMetadata` Content-Length 设置（已在之前变更完成，本次补充回归验证）

## Capabilities

### New Capabilities
- `presigned-upload`: 预签名直传上传，包含 prepare-upload 生成 URL 和 confirm-upload 回调校验
- `presigned-download`: 预签名下载，权限校验后返回临时直传 URL

### Modified Capabilities
- `unified-file-upload`: 上传流程从后端代理改为统一预签名直传，旧接口标记废弃保留兼容
- `backend-file-upload-handler`: 新增预签名 URL 生成逻辑、ETag 校验、回调令牌验证
- `backend-file-download-handler`: 下载响应从 `ResponseEntity<Resource>` 改为 302 Redirect / 预签名 URL 返回
- `backend-minio-file-storage`: MinIO 预签名 URL 生成适配
- `backend-object-storage-provider`: `ObjectStorage` 接口扩展预签名 URL 相关方法

## Impact

- **API 层**：`FileUploadController` 新增两个端点，`FileDownloadController` 响应方式改变（需确认前端兼容性）
- **前端**：上传逻辑需要改为"申请 URL → 直传 OSS → 回调确认"三步；下载逻辑需支持 302 Redirect 或接收 JSON URL
- **存储层**：`ObjectStorage` 接口新增方法，两个实现类（MinIO / 阿里云 OSS）均需适配
- **安全模型**：上传内容校验从"传输中"变为"事前授权 + 事后回调校验（ETag + 魔数）"
- **Nginx 部署**：需确认 `client_max_body_size`、`proxy_read_timeout` 等参数（即使走直传，prepare/confirm 请求仍是 JSON，可收紧限制）
- **数据库**：`tb_file` 表建议增加 `status` 字段（PENDING / ACTIVE / REJECTED）用于跟踪预签名上传的中间状态
