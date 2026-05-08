## Context

当前文件上传采用后端代理模式：前端 multipart → Spring Boot → `ObjectStorage.put()` → OSS。Spring Boot 限制了 `max-file-size: 5MB`，且阿里云 OSS `putObject(InputStream)` 在不指定 Content-Length 时会将整流缓冲到内存。业务需求为 80 并发 × 200MB 单文件上限，后端代理在该规模下必然导致磁盘 I/O 打满、JVM OOM、连接超时。

批量下载 ZIP 当前使用 `ByteArrayOutputStream` 全内存打包，多个 150MB+ 文件同样会 OOM。

Nginx 部署在服务器上，项目仓库中不可见配置文件，但 `client_max_body_size` 和超时参数仍可能成为瓶颈。

## Goals / Non-Goals

**Goals:**
- 支持 200MB 单文件上传，80 并发下后端不成为带宽/内存瓶颈
- 上传安全模型不降级：内容校验、权限控制、审计日志保持等价
- 批量下载 ZIP 内存占用从 Σ(文件大小) 降到固定 buffer
- 所有文件统一走预签名直传，代码路径唯一，前端逻辑统一

**Non-Goals:**
- 断点续传
- 前端直传下载（预签名 GET URL 已足够）
- 视频流、直播等大文件持续传输场景
- 修改 Nginx 配置文件（不在代码仓库内，仅提供参数建议文档）
- 后端代理上传模式（完全由预签名直传替代）

## Decisions

**1. 统一预签名直传：所有文件走 prepare → 直传 OSS → confirm**
- 原因：代码路径唯一，无需维护两套逻辑；prepare/confirm 是轻量级 JSON 接口（毫秒级），额外开销可忽略；后端完全解脱上传带宽压力
- 流程：所有上传（头像、二维码、作品、附件）统一调用 `POST /api/v1/file/prepare-upload` → 前端直传 OSS → `POST /api/v1/file/confirm-upload`
- 匿名场景：AVATAR / NORMAL_IMG 的 prepare 接口允许匿名访问，权限校验逻辑与当前一致

**2. 预签名上传流程：prepare → 前端直传 OSS → confirm**
- 原因：阿里云 OSS 预签名 PUT URL 只能约束 objectKey / Content-Type / expire，无法校验内容 hash；必须在上传后由后端确认
- callbackToken 使用 JWT，含 fileId、预期 hash、过期时间，防重放和篡改
- 确认阶段后端向 OSS 发 HEAD 请求拿 ETag（MD5），与 callbackToken 中的预期 hash 比对

**3. 下载改为 302 Redirect + 预签名 GET URL**
- 原因：权限校验逻辑已很成熟，只需在后端校验通过后生成临时 URL，文件流量不再经过后端
- 阿里云 OSS：`generatePresignedUrl` 生成带过期时间的 GET URL
- MinIO：`getPresignedObjectUrl` 同理
- 过期时间：默认 10 分钟，覆盖 150MB @ 10Mbps（约 2 分钟下载）的弱网场景

**4. 批量下载 ZIP 改为流式输出**
- 原因：`ByteArrayOutputStream` 在 150MB+ 文件面前必然 OOM
- `ZipOutputStream` 直接写入 `ServletOutputStream`，`is.transferTo(zos)` 使用 8KB buffer
- 不返回 `ByteArrayResource`，而是直接操作 `HttpServletResponse`

**5. ObjectStorage 接口扩展预签名方法**
- 新增 `getPresignedUploadUrl(FileType, String filename, String contentType, long size, Duration expiry)`
- 新增 `getPresignedDownloadUrl(FileType, String filename, Duration expiry)`
- 不破坏现有 `put`/`get`/`delete` 方法，向后兼容

**6. tb_file 增加 status 字段**
- PENDING：已生成预签名 URL，但前端尚未上传或尚未确认
- ACTIVE：上传完成且校验通过
- REJECTED：校验失败（ETag 不匹配、Content-Type 不符、大小超限）
- 现有后端代理上传直接写 ACTIVE

## Risks / Trade-offs

- **[Risk] 预签名 URL 泄露后可在过期前被任意使用** → **Mitigation**: URL 过期时间短（5~15min），objectKey 随机不可预测，且只能操作指定 key
- **[Risk] 攻击者上传合法 Content-Type 但恶意内容（如伪装成图片的脚本）** → **Mitigation**: 回调阶段增加魔数检查（jpeg/png/pdf 文件头），未来可接入异步病毒扫描
- **[Risk] 前端未回调 confirm，导致 PENDING 记录和空 OSS 对象残留** → **Mitigation**: 定时任务清理 24h 前 PENDING 记录及其对应 OSS 对象
- **[Risk] 未登录用户调用 prepare-upload 生成预签名 URL 后滥用** → **Mitigation**: 匿名调用仅限 AVATAR / NORMAL_IMG 类型，生成 URL 时记录 IP 和指纹，异常频率触发限流
- **[Trade-off] 所有上传都变成 3 步（prepare → 直传 → confirm）** → prepare/confirm 是毫秒级 JSON，额外 RTT 可忽略；换来架构统一和后端零带宽压力

## Migration Plan

1. 部署前：确认 Nginx `client_max_body_size` 和超时参数（直传模式下 Nginx 只需处理 JSON，可收紧）
2. 部署时：前端同步更新上传逻辑为三步流程；后端保留旧 `POST /api/v1/file/upload` 接口但标记为 @Deprecated
3. 回滚：前端切回旧上传接口，后端保留旧接口可用
