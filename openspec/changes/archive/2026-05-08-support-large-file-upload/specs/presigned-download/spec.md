## ADDED Requirements

### Requirement: 预签名下载接口
系统 SHALL 提供 `GET /api/v1/file/download/{fileId}` 接口，在权限校验通过后返回预签名 GET URL（或 302 Redirect），使前端能直接从 OSS 下载文件，文件流量不经过后端。

#### Scenario: 有权限用户下载文件
- **WHEN** 有权限的用户 GET `/api/v1/file/download/123`
- **THEN** 系统 SHALL 执行与当前完全相同的权限校验逻辑（WORK / ASSESSMENT_ATTACHMENT / AVATAR 等）
- **AND** 查询 tb_file 获取 filename 和 type
- **AND** 生成预签名 GET URL（过期 10 分钟）
- **AND** 返回 302 Redirect 到该 URL（或返回 JSON `{downloadUrl}`）

#### Scenario: 无权限用户下载受保护文件
- **WHEN** 无权限用户 GET `/api/v1/file/download/123`
- **THEN** 系统 SHALL 在生成预签名 URL 之前拒绝访问
- **AND** 返回 403 Forbidden（与当前行为一致）

#### Scenario: 预签名下载 URL 过期
- **WHEN** 用户在 10 分钟后访问预签名下载 URL
- **THEN** OSS SHALL 返回 403 Request has expired
- **AND** 用户需要重新调用 `/api/v1/file/download/{fileId}` 获取新 URL

### Requirement: 批量下载 ZIP 流式输出
系统 SHALL 支持批量下载打包为 ZIP，但 SHALL 使用流式输出（`ZipOutputStream` 直接写入 `ServletOutputStream`），不将文件内容缓冲到内存。

#### Scenario: 批量下载多个大文件
- **WHEN** 用户 POST `/api/v1/file/download/batch` body=`{entries: [...], zipName: "works.zip"}`
- **THEN** 系统 SHALL 对每个 entry 执行权限校验
- **AND** 对每个 entry 从 OSS 流式读取文件内容
- **AND** 使用 `ZipOutputStream` 直接写入 response output stream
- **AND** 内存占用 SHALL 不超过固定 buffer 大小（8KB）
- **AND** 返回 `Content-Type: application/zip`

#### Scenario: 批量下载包含不存在的文件
- **WHEN** 批量下载请求中包含已删除或不存在的 fileId
- **THEN** 系统 SHALL 在流式输出前校验所有文件存在性
- **AND** 如有缺失，返回 404 DataNotFound（不生成部分 ZIP）
