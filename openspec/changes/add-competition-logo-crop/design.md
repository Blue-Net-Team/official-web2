# Design: 竞赛 Logo 上传前裁剪

## Context

- 现状：`src/frontend/src/components/Profile/AvatarCropModal/index.tsx` 已实现完整的"选择图片 → react-easy-crop 裁剪 → canvas 输出 512×512 JPEG blob → 上传"链路，但尺寸/比例硬编码（`maxSize = 512`、1:1），组件名与目录（`Profile/`）均绑定头像语义。
- 问题：`CompetitionDrawer.handleLogoUpload` 将原始文件直接 `fileService.upload(file, 'NORMAL_IMG')`，logo 宽高比不受控，公开列表 `CompetitionCard` 以 `h-[28px] w-auto` 渲染导致宽度漂移（ref #78）。
- 约束：前端 Next.js 15 + React 19 + Ant Design 6；`react-easy-crop@^5.5.7` 已安装（AvatarCropModal 中以 class 组件 cast 为 FC 兼容 React 19，该写法需原样保留）。
- AvatarCropModal 仅一个消费方：`ProfileSidebar`，重构影响面小。

## Goals / Non-Goals

**Goals:**
- 通用化裁剪组件：`outputSize`（默认 512）、`aspect`（默认 1）可配置
- 竞赛 logo 上传强制先裁剪为正方形，再走既有上传接口
- 头像裁剪对外行为完全不变

**Non-Goals:**
- 后端任何改动（logo 对后端仍只是 `fileId`）
- 竞赛封面（cover）裁剪
- 存量非方形 logo 的迁移/展示层兜底改造
- 上传文件类型/大小的后端校验强化

## Decisions

### D1: 通用化而非复制 —— AvatarCropModal → ImageCropModal
将现有组件重构迁移为 `src/frontend/src/components/common/ImageCropModal/index.tsx`（项目已有小写 `common` 共享目录），`AvatarCropModal` 删除，`ProfileSidebar` 改引用新组件。
- 备选：复制一份 `LogoCropModal` —— 重复维护两份几乎相同的 canvas/裁剪逻辑， rejected。
- `getCroppedBlob` 中的硬编码 `maxSize = 512` 改为参数化；`outputSize` 非正方形输出时的行为由 `aspect` 决定画布的宽/高（宽 = outputSize，高 = outputSize / aspect）。
- props 扩展保持向后兼容语义：avatar 调用方不传新 props 即得到与原行为一致的结果。

### D2: aspect 也做成 prop（默认 1）
虽然本期只有正方形需求，但 `react-easy-crop` 原生支持 `aspect` prop，透传成本为零，为未来 16:9 封面裁剪预留能力。

### D3: logo 裁剪流程全部在前端完成，复用既有上传接口
裁剪产出的 blob 包装为 `File` 后直接调用现有 `fileService.upload(blobFile, 'NORMAL_IMG')`（沿用 AvatarCropModal 的处理方式），不新增后端接口、不新增文件类型枚举。
- 备选：后端裁剪/校验 —— issue 建议方案即"上传前裁剪"，且后端无图像处理依赖，rejected。

### D4: 交互形式沿用 AvatarCropModal 的 Modal + Slider 方案
Ant Design `Modal` + 缩放 `Slider`，确认后回调 blob。CompetitionDrawer 中 logo 的 `Upload` 组件 `beforeUpload` 拦截：读取文件为 objectURL → 打开裁剪弹窗 → return false 阻止默认上传；确认后自行执行上传逻辑（setLogoFileId / form 回填），取消则不作任何变更。

### D5: logo 输出尺寸取 256
logo 在竞赛卡片仅渲染 28px 高，256×256 已覆盖 2x 屏；同时低于 avatar 的 512 减少无效上传体积。`outputSize` 默认值仍为 512（保持 avatar 行为），logo 调用方显式传 256。

## Risks / Trade-offs

- [重构破坏头像裁剪] → 唯一消费方 ProfileSidebar，改动仅为引用路径；E2E 验证时回归头像上传路径。
- [Canvas toBlob 产出 JPEG 丢失透明通道] → 与现有 avatar 行为一致；logo 场景若上传 PNG 透明底会变黑/白底。可接受（本期不处理，记录为已知限制）。
- [React 19 兼容 cast] → 保留 `react-easy-crop` 的 `_Cropper as unknown as React.FC` 写法，不做升级或类型"修正"。

## Open Questions

已在前置探索中确认：组件通用化（方案 A）、`outputSize`/`aspect` 均为 props 且带默认值、存量数据不迁移）
