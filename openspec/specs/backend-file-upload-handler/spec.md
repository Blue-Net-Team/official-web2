## REMOVED Requirements

### Requirement: File upload with event triggering
**Reason**: 8 个业务混合上传接口被统一上传接口 `POST /api/v1/file/upload` 替代。文件上传不再触发业务逻辑和事件。
**Migration**: 使用 `POST /api/v1/file/upload` 上传文件，然后调用对应业务接口关联 fileId。

### Requirement: 上传考题作品
**Reason**: 考题作品上传改为统一接口 `POST /api/v1/file/upload?type=WORK`，业务逻辑（方向校验）移至答案提交接口。
**Migration**: 使用 `POST /api/v1/file/upload?type=WORK` 上传文件，再通过答案创建/更新接口传入 fileId。

### Requirement: File type validation
**Reason**: 文件类型验证逻辑移至统一上传接口。
**Migration**: 由统一上传接口处理。

### Requirement: File size limit
**Reason**: 文件大小限制逻辑移至统一上传接口。
**Migration**: 由统一上传接口处理。

## ADDED Requirements

### Requirement: 预签名上传确认幂等性
`POST /api/v1/file/confirm-upload` 接口 SHALL 对已处于 ACTIVE 状态的文件重复确认返回成功，而非报错。

#### Scenario: 网络超时后前端重试确认
- **WHEN** 前端首次调用 `confirm-upload` 成功，状态变为 ACTIVE，但响应因网络超时而丢失
- **AND** 前端使用相同参数再次调用 `confirm-upload`
- **THEN** 后端识别文件状态已为 ACTIVE
- **AND** 返回 200 + 与首次成功相同的 ConfirmUploadResponse
- **AND** 不抛出"文件状态无效"错误

#### Scenario: 重复确认已拒绝的文件
- **WHEN** 文件状态已为 REJECTED 时再次调用 `confirm-upload`
- **THEN** 返回 403 Forbidden "文件状态无效"
- **AND** 不执行重复清理逻辑
