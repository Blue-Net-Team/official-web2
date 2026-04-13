## MODIFIED Requirements

### Requirement: Public endpoints exempt from CSRF
系统 SHALL NOT require CSRF token for public endpoints。CSRF 白名单 SHALL 包含 `/api/v1/file/upload`（统一文件上传接口，支持未登录用户上传头像）。

#### Scenario: 统一文件上传接口免 CSRF
- **WHEN** 未认证用户 POST `/api/v1/file/upload` without CSRF token
- **THEN** CSRF validation SHALL be skipped
- **THEN** the request SHALL be processed normally

#### Scenario: 旧文件上传路径不再白名单
- **WHEN** 检查 CSRF 白名单配置
- **THEN** `/api/v1/file/upload/avatar` 不再存在于白名单中
- **AND** `/api/v1/file/upload` 存在于白名单中
