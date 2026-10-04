## Why

报名页是全站最后仍使用旧磨砂玻璃配方（半透明底色 + backdrop-blur、无深色渐变保底）的页面之一，正是 `smoked-glass-card` 规范中 Issue #70 证伪的缺陷形态——在 backdrop-filter 不生效的环境（如微信 Android XWEB 内核）中卡片会退化为灰糊色块。项目已有全站唯一的玻璃组件 `SmokedGlassCard`，报名页需要完成迁移并收敛装饰风格。

## What Changes

- 报名页主表单卡片（`EnrollForm`）迁移至 `SmokedGlassCard`，删除顶部三色渐变条与四角括号线装饰，保留入场动画与内部表单结构
- 方向选择卡片（`DirectionSidebar`）迁移至 `SmokedGlassCard hoverable accent`：删除横向平移动画，改用组件自带的 translateY 浮起动画；hover 时边框与文字按方向主题色高亮
- 报名表下载卡片（`EnrollFormDownloadCard`）迁移至 `SmokedGlassCard tone="soft"`
- 咨询群二维码卡片（`ConsultationQrcode`）迁移至 `SmokedGlassCard tone="soft"`
- `SmokedGlassCard` 组件本身**不改动**，本次为纯消费方迁移

## Capabilities

### New Capabilities

（无）

### Modified Capabilities

- `frontend-enroll-page`: 页面卡片的玻璃材质与视觉装饰要求变更——所有卡片改用 `SmokedGlassCard` 组件（深色渐变保底），方向卡片 hover/选中态改用 accent 主题色语言，主卡片移除冗余装饰元素

## Impact

- **前端文件**：
  - `src/frontend/src/components/Enroll/EnrollForm.tsx`
  - `src/frontend/src/components/Enroll/DirectionSidebar.tsx`
  - `src/frontend/src/components/Enroll/EnrollFormDownloadCard/index.tsx`
  - `src/frontend/src/components/Enroll/ConsultationQrcode/index.tsx`
  - `src/frontend/src/app/(public)/(other)/enroll/styles.module.css`（方向卡片 hover 文字高亮样式）
- **无后端影响、无 API 变更、无依赖变更**
- **组件契约**：`SmokedGlassCard` 的 props（tone/hoverable/accent/radius/className/style）完全覆盖本次需求，无需扩展
