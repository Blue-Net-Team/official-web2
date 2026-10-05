# competition-logo-crop

## MODIFIED Requirements

### Requirement: 竞赛 logo 上传前强制裁剪
在竞赛创建/编辑界面（CompetitionDrawer），管理员选择 logo 图片后，系统 SHALL 先弹出裁剪弹窗（正方形），用户确认后 SHALL 将裁剪生成的 blob 经预签名直传通道（`usePresignedUpload`）上传（类型 `NORMAL_IMG`），并用返回的 fileId 回填 `logoFileId`。上传原始未裁剪文件的路径 MUST NOT 存在。裁剪状态管理 SHALL 经共享 hook `useImageCropUpload` 处理，CompetitionDrawer MUST NOT 再手写裁剪状态样板代码。

#### Scenario: 选择 logo 图片后弹出裁剪
- **WHEN** 管理员在竞赛创建/编辑表单中选择一张 logo 图片
- **THEN** 系统弹出正方形裁剪弹窗，不立即上传原图

#### Scenario: 确认裁剪并上传
- **WHEN** 用户在裁剪弹窗中点击确认
- **THEN** 系统生成正方形 blob（格式与原图一致）并上传，成功后回填 `logoFileId`

#### Scenario: 取消裁剪
- **WHEN** 用户在裁剪弹窗中点击取消
- **THEN** 不上传任何文件，`logoFileId` 保持原值
