## Why

后端已实现预签名直传能力（`prepare-upload` → 直传 OSS → `confirm-upload`），但前端所有上传调用仍走传统 `multipart/form-data` 上传接口（`POST /api/v1/file/upload`，已标记废弃）。大文件（考核作品、考题附件）经过应用服务器转存，既增加带宽压力又降低上传稳定性。前端需要接入直传能力，把文件流直接推送到对象存储。

## What Changes

- 前端 `file.service.ts` 新增 `prepareUpload` 和 `confirmUpload` API 封装
- 新增 `usePresignedUpload` Hook，统一管理预签名直传的 4 阶段状态（准备中 / 上传中 / 检查中 / 完成）及分段进度条
- 替换 4 个现有上传调用点：报名头像、Bug 截图、考题附件、考核作品上传
- `FileUploadArea` 进度条改为分段式，映射 4 个上传阶段
- 后端 `FileDomainServiceImpl.confirmUpload()` 增加对已 `ACTIVE` 状态的幂等返回，使前端在"检查中"阶段可安全重试

## Capabilities

### New Capabilities
- `frontend-presigned-upload`: 前端预签名直传能力，包含 Hook、服务封装、分段进度条组件

### Modified Capabilities
- `unified-file-upload`: 上传流程从传统 `POST /file/upload` 迁移到预签名三段式直传，进度反馈方式变更（单进度条 → 分段进度条）
- `backend-file-upload-handler`: `confirm-upload` 接口行为增加幂等性（对已激活文件重复确认返回成功而非报错）

## Impact

- 前端：`src/frontend/src/apis/services/file.service.ts`、`src/frontend/src/components/Assessment/QuestionDetail/FileUploadArea.tsx`、`useEnrollForm.ts`、`BugReportModal.tsx`、`QuestionDrawer.tsx`
- 后端：`FileDomainServiceImpl.java`（confirmUpload 幂等处理）
- 部署：MinIO / OSS CORS 需允许前端 `Origin`（已配置则无需改动）
