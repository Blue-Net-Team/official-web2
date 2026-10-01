# competition-logo-crop

## Purpose

竞赛创建/编辑时 logo 图片的上传前裁剪（正方形、可配置输出尺寸），裁剪后 blob 经既有文件上传接口存储；同时提供通用的图片裁剪组件（ImageCropModal）供头像等其他场景复用。

## Requirements

### Requirement: 共享图片裁剪组件
系统 SHALL 提供共享裁剪组件 `ImageCropModal`，基于 `react-easy-crop` 实现，支持通过 props 配置输出尺寸 `outputSize`（默认 512）与裁剪宽高比 `aspect`（默认 1，即正方形）。裁剪 SHALL 仅做几何裁剪：输出格式 MUST 与原图一致（从源文件 MIME 判定），PNG 原图 MUST 保留透明通道，不得统一转为 JPEG。

#### Scenario: 默认配置裁剪正方形
- **WHEN** 调用方未传入 `outputSize` 与 `aspect`
- **THEN** 组件以 1:1 宽高比裁剪，输出 512×512 图片 blob

#### Scenario: 自定义输出尺寸与宽高比
- **WHEN** 调用方传入 `outputSize=256, aspect=1`
- **THEN** 组件输出 256×256 图片 blob

#### Scenario: PNG 原图保留透明通道
- **WHEN** 用户裁剪一张 PNG 图片（含透明区域）并确认
- **THEN** 输出仍为 PNG 格式 blob，透明信息保留

#### Scenario: JPEG 原图保持 JPEG 格式
- **WHEN** 用户裁剪一张 JPEG 图片并确认
- **THEN** 输出仍为 JPEG 格式 blob（质量 0.9）

### Requirement: 竞赛 logo 上传前强制裁剪
在竞赛创建/编辑界面（CompetitionDrawer），管理员选择 logo 图片后，系统 SHALL 先弹出裁剪弹窗（正方形），用户确认后 SHALL 将裁剪生成的 blob 经既有文件上传接口（`fileService.upload`，类型 `NORMAL_IMG`）上传，并用返回的 fileId 回填 `logoFileId`。上传原始未裁剪文件的路径 MUST NOT 存在。

#### Scenario: 选择 logo 图片后弹出裁剪
- **WHEN** 管理员在竞赛创建/编辑表单中选择一张 logo 图片
- **THEN** 系统弹出正方形裁剪弹窗，不立即上传原图

#### Scenario: 确认裁剪并上传
- **WHEN** 用户在裁剪弹窗中点击确认
- **THEN** 系统生成正方形 blob（格式与原图一致）并上传，成功后回填 `logoFileId`

#### Scenario: 取消裁剪
- **WHEN** 用户在裁剪弹窗中点击取消
- **THEN** 不上传任何文件，`logoFileId` 保持原值

### Requirement: 竞赛封面上传不受影响
竞赛封面（cover）上传流程 MUST 保持原样，不经过裁剪弹窗。

#### Scenario: 选择封面图片
- **WHEN** 管理员在竞赛表单中选择封面图片
- **THEN** 系统直接上传原图，不弹出裁剪弹窗

### Requirement: 存量 logo 不迁移
对于修改前已上传的非方形 logo，系统 SHALL NOT 做数据迁移或后端处理，展示层维持现状。

#### Scenario: 编辑含旧 logo 的竞赛
- **WHEN** 管理员编辑一个 logo 为历史非方形图片的竞赛且不更换 logo
- **THEN** 系统保留原 `logoFileId`，不强制重新裁剪
