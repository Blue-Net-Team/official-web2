# Proposal: unify-smoked-glass-tabs

## Why

站点多个公开页面的 Tab 导航（团队成员方向筛选、资源库方向筛选、Profile 页 Tab）由各自手写的 button + Tailwind 实现，样式、交互和动效互不统一，也偏离全站烟色玻璃（SmokedGlassCard）视觉体系。已封装 `SmokedGlassTabs` 组件（antd Tabs API + 烟色玻璃胶囊 + 平移指示器），需要将其推广为全站统一的 Tab 组件。

## What Changes

- 新增统一 Tab 组件 `src/frontend/src/components/SmokedGlassTabs/`（本次变更前已完成开发并通过演示页验证）
  - API 与 antd Tabs 对齐（`items` / `activeKey` / `onChange`），外加 `accent` 主题色 prop
  - 局部 ConfigProvider 控制颜色 token；CSS Module 承载烟色玻璃材质与滑动胶囊几何
  - 滑动指示器在 Tab 间平移（类 sigmoid 缓动 `cubic-bezier(0.4, 0, 0.2, 1)`，无过冲），文字高亮带颜色过渡
- 替换以下页面的自研 Tab 为 `SmokedGlassTabs`：
  - Profile 页（`components/Profile/ProfileTabs` → 使用新组件或直接替换调用处）
  - 团队成员页（`components/Members/Members.tsx` 方向筛选条）
  - 资源库页（`app/(public)/(other)/resources/page.tsx` 方向筛选 nav）
- 迁移后删除被替换的自研 Tab 实现（如 `ProfileTabs`）
- 保留资源库页 URL 驱动的 Tab 状态（`?tab=` searchParam，SSR 场景），仅替换视觉组件
- 移除样式演示页 `app/(public)/(other)/tabs-demo`（或保留至验收后删除，见 tasks）

## Capabilities

### New Capabilities

- `smoked-glass-tabs`: 全站统一 Tab 导航组件的渲染规范（胶囊容器材质、滑动指示器行为、颜色 token、可访问性）与使用约定

### Modified Capabilities

- `software-resource-library`: 方向筛选 Tab 的视觉呈现由自研圆角按钮改为 `SmokedGlassTabs`，交互行为（点击切换、URL 同步）不变
- `frontend-user-profile`: Profile 页 Tab 导航的视觉呈现由渐变按钮组改为 `SmokedGlassTabs`，计数徽标与切换行为不变
- `frontend-members-list`: 团队成员页方向筛选的视觉呈现由自研筛选条改为 `SmokedGlassTabs`，计数与分页重置行为不变

## Impact

- **代码**：仅前端展示层，涉及
  - `src/frontend/src/components/SmokedGlassTabs/`（新增，已存在）
  - `src/frontend/src/components/Profile/ProfileTabs/`（删除或改造）
  - `src/frontend/src/components/Members/Members.tsx`
  - `src/frontend/src/app/(public)/(other)/resources/page.tsx`
  - `src/frontend/src/app/(public)/(other)/tabs-demo/page.tsx`（演示页，验收后删除）
- **API / 数据流**：无改动，不改变任何请求、权限、路由逻辑
- **依赖**：无新增依赖（antd v6 已有）
- **风险**：低，纯视觉替换；资源库页为 SSR + searchParam 驱动，需验证切换后仍正确触发服务端重新渲染
