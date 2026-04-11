## Why

当前管理后台的导航组件（AdminHeadBar 和 AdminSideBar）在移动端体验不佳：侧边栏展开时会挤压右侧内容空间，菜单按钮位置不合理（在侧边栏区域而非顶部导航栏），且组件分散不利于状态共享。需要重构为统一的 AdminNav 组件体系，优化移动端交互体验。

## What Changes

- 将 `AdminHeadBar` 和 `AdminSideBar` 合并到新的 `AdminNav` 文件夹中
- 移动端菜单按钮移至 Header 右侧，使用透明背景
- 移动端侧边栏改为从右侧弹出的 Drawer（覆盖层模式，不挤压内容）
- 桌面端保持原有 Sider 行为不变
- 统一管理 `isMobile`、`drawerVisible` 等共享状态

## Capabilities

### New Capabilities

- `admin-nav-mobile`: 管理后台导航组件的移动端响应式布局和交互优化

### Modified Capabilities

- `admin-sidebar-permissions`: 侧边栏权限过滤逻辑保持不变，但组件结构从独立文件调整为 AdminNav 子模块

## Impact

- **前端代码**：
  - 新增 `src/frontend/src/components/Admin/AdminNav/` 目录
  - 删除 `src/frontend/src/components/Admin/AdminHeadBar/` 目录
  - 删除 `src/frontend/src/components/Admin/AdminSideBar/` 目录
  - 修改 `src/frontend/src/app/admin/layout.tsx` 的导入路径
- **依赖**：无新增依赖，继续使用 Ant Design 的 Sider、Drawer、Menu 组件
- **向后兼容**：桌面端行为完全不变，仅优化移动端体验
