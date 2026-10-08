## Why

考核答题界面（file_upload 题型）经常出现用户把**整个文件夹拖进上传区**导致上传失败的情况，而前端只弹出一句「上传失败，请重试」，不说明原因、不给出指引，用户体验差、客服成本高。同时该上传区当前**没有任何格式限制**（`allowedExtensions`/`maxFileSize` 是前端预留字段，后端从未返回），任何文件甚至可执行文件都能上传，与"提交压缩包作业"的实际业务约定不符。

## What Changes

- 考题上传区（`Assessment/QuestionDetail`）增加**压缩包白名单**校验：仅允许常见压缩包格式（`zip`、`rar`、`7z`、`tar`、`gz`、`tar.gz`、`bz2`、`xz`），点击选择（`accept` 属性）和 `beforeUpload` 拦截双重生效，不合法格式给出明确提示（如「仅支持 zip、rar、7z 等压缩包格式，请压缩后上传」）。
- 上传区增加**文件夹拖拽检测**：通过 `onDrop` + `DataTransferItem.webkitGetAsEntry().isDirectory` 识别拖入的文件夹，直接拦截并提示「不支持上传文件夹，请压缩后上传」（Firefox 无该 API 时降级为扩展名/空文件校验兜底）。
- **错误信息透传**：`customRequest` 的 catch 不再统一吞成「上传失败，请重试」，而是展示 `usePresignedUpload` 产生的具体错误信息（如「文件校验未通过」「准备上传失败」等），保留兜底文案。
- 拖拽区提示文案（`dropHintText`）同步更新为白名单 + 大小限制的实际内容，不再回退显示「所有文件格式」。

纯前端修改，不动后端、不动其他上传场景（BugReport、头像、管理端 Drawer 等维持现状）。

## Capabilities

### New Capabilities
- `assessment-archive-upload`: 考核答题界面文件上传区的格式白名单、文件夹拖拽拦截与错误信息透传行为定义。

### Modified Capabilities
<!-- 无：仓库中无相关既有 capability spec -->

## Impact

- **前端**：
  - `src/frontend/src/components/Assessment/QuestionDetail/index.tsx`（draggerProps、beforeUpload、customRequest、dropHintText）
  - `src/frontend/src/components/Assessment/QuestionDetail/FileUploadArea.tsx`（提示展示，如需）
  - 可能新增/调整 `src/frontend/src/components/Assessment/QuestionDetail/utils.ts` 中的常量与校验工具函数
- **后端**：无改动。
- **风险**：`accept` 属性仅为浏览器文件选择器过滤器，可被绕过；`beforeUpload` 为真正拦截点，但均为前端校验，恶意绕过仍可上传非压缩包（本次变更明确接受此边界，后端校验不在范围内）。
