# Proposal: migrate-remaining-uploads-to-presigned

## Why

继 unify-image-crop-upload 之后，前端仍有 6 处文件上传走旧的后端中转通道（`fileService.upload` → `POST /api/v1/file/upload`）：文件流量经过后端双倍占用带宽、无 MD5 校验/重试能力、与项目预签名直传规范（presigned-storage）不一致。本次将这 6 处全部迁移到 `usePresignedUpload` 预签名直传通道，并从前端代码中移除旧上传方法，使前端上传通道单一化。

## What Changes

- 以下 6 处上传从 `fileService.upload` 迁移到 `usePresignedUpload` 预签名直传，文件类型与表单回填逻辑保持不变：
  1. `VenueDrawer` 场地图片（NORMAL_IMG）
  2. `EquipmentDrawer` 设备图片（NORMAL_IMG）
  3. `CompetitionDrawer` 竞赛封面 cover（NORMAL_IMG，上次变更有意保留旧通道，本次迁移）
  4. `QrcodeDrawer` 管理端二维码（QRCODE）
  5. `ProfileInfo` 微信二维码（QRCODE）
  6. `AchievementDrawer` 成就附件（NORMAL_IMG）
- 迁移后从 `file.service.ts` 中删除 `upload` 方法（前端不再有旧通道调用方）。
- 每处迁移点补充/调整失败处理：预签名通道失败时抛出异常（区别于旧通道返回 `{code, msg}`），确保用户看到错误提示且上传状态复位。
- **后端 `POST /api/v1/file/upload` 接口保留不动**（对应 `unified-file-upload` spec），仅前端弃用。

## Capabilities

### New Capabilities

（无）

### Modified Capabilities

- `frontend-presigned-upload`: 新增"前端所有文件上传场景 MUST 使用预签名直传通道"的全局需求，枚举本次迁移的 6 个消费方，并声明前端不再存在旧通道调用。

## Impact

- **前端代码**：修改上述 6 个文件；`file.service.ts` 删除 `upload` 方法。
- **无后端改动**：`prepare-upload` / `confirm-upload` 及权限均已存在并已在 8 处生产使用；`POST /file/upload` 接口保留。
- **无新依赖**。
- **范围外**（另案处理）：知识库文档上传（`knowledge.service.ts` 的 `uploadDocument` / `replaceDocFile`）是域内 multipart 业务 API（后端收文件后同步解析入库），迁移需要后端改造，不属于纯前端通道替换。
