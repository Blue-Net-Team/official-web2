## MODIFIED Requirements

### Requirement: 统一文件上传接口
系统 SHALL 提供单一文件上传接口 `POST /api/v1/file/upload`，接受 `file`（MultipartFile）和 `type`（FileType 枚举）参数，仅负责文件存储（上传到 OSS + 记录 tb_file），不涉及任何业务逻辑。此接口标记为已废弃（@Deprecated），所有上传 SHALL 迁移至预签名直传流程。

#### Scenario: 上传头像文件（未登录用户，旧接口）
- **WHEN** 未登录用户 POST `/api/v1/file/upload` params=`{file: avatar.jpg, type: AVATAR}`
- **THEN** 系统 SHALL 返回 200 并兼容旧行为（向后兼容期）
- **AND** 响应头或响应体中 SHOULD 包含 deprecation 警告

#### Scenario: 上传考题作品（已登录用户，旧接口）
- **WHEN** 已登录用户 POST `/api/v1/file/upload` params=`{file: work.zip, type: WORK}`
- **THEN** 系统 SHALL 返回 200 并兼容旧行为（向后兼容期）
- **AND** 响应中 SHOULD 提示使用新的预签名上传接口

## ADDED Requirements

### Requirement: 预签名直传成为唯一上传方式
系统 SHALL 将所有文件上传统一为预签名直传流程：`POST /api/v1/file/prepare-upload` → 前端直传 OSS → `POST /api/v1/file/confirm-upload`。

#### Scenario: 上传头像文件（未登录用户，新流程）
- **WHEN** 未登录用户 POST `/api/v1/file/prepare-upload` body=`{filename: "avatar.jpg", type: AVATAR, size: 2048000, contentType: "image/jpeg"}`
- **THEN** 系统 SHALL 校验匿名上传权限（仅限 AVATAR / NORMAL_IMG）
- **AND** 生成预签名 PUT URL 和 callbackToken
- **AND** 插入 tb_file 状态为 PENDING
- **AND** 前端直传 OSS 后回调 confirm，状态变为 ACTIVE

#### Scenario: 上传考题作品（已登录用户，新流程）
- **WHEN** 已登录用户 POST `/api/v1/file/prepare-upload` body=`{filename: "work.zip", type: WORK, size: 150000000, contentType: "application/zip"}`
- **THEN** 系统 SHALL 校验已登录权限
- **AND** 生成预签名 PUT URL 和 callbackToken
- **AND** 前端直传 OSS 后回调 confirm，状态变为 ACTIVE
