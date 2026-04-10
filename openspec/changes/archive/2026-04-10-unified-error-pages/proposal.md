## Why

前端目前没有任何自定义错误页面。用户访问不存在的路由时显示 Next.js 默认 404 页面，普通成员（CANDIDATE/未登录）访问 `/admin/*` 时直接看到完整的后台布局而无权限拦截。需要统一的错误页面系统来改善用户体验并进行前端权限控制。

## What Changes

- 新建共享 `ErrorPage` 组件和 `ErrorPageConfig` 类型定义，提供居中布局的错误页渲染（大图标 + 错误码 + 描述文案）
- 新建根级 `app/not-found.tsx`，替换 Next.js 默认 404 页面，手动引入 PublicNavBar 保持导航一致性
- 修改 `app/admin/layout.tsx`，添加基于 `getRoleLevel` 的角色检查，roleLevel < 1 时渲染 PublicNavBar + 403 错误页

## Capabilities

### New Capabilities
- `frontend-error-pages`: 统一错误页面系统，包含 ErrorPageConfig 类型、ErrorPage 共享组件、404/403 页面实现

### Modified Capabilities
<!-- 无需修改现有 spec -->

## Impact

- 前端代码：新增 3 个文件，修改 1 个文件（admin layout）
- 依赖：无新依赖，使用现有的 Ant Design Icons 和 RoleUtils
- 后端：无变更
