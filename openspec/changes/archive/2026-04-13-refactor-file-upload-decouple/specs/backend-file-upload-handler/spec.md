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
