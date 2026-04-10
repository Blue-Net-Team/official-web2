## Why

当前 AdminSideBar 组件是静态的——所有用户看到相同的菜单项，无导航跳转，无权限过滤。Admin 管理后台需要根据用户角色（SUPER_ADMIN、DIRECTION_ADMIN、MEMBER）动态显示菜单项，确保不同权限的用户只能看到和访问其权限范围内的管理功能。

## What Changes

- AdminSideBar 根据用户 roleLevel 动态过滤菜单项
- 添加菜单导航功能，点击跳转到对应 `/admin/*` 路由
- QA管理菜单项灰显不可点击（后端未开发）
- Admin 布局添加权限守卫：level <= 0 的用户显示无权限提示

## Capabilities

### New Capabilities

- `admin-sidebar-permissions`: 管理后台侧边栏权限过滤与导航配置

### Modified Capabilities

（无现有 spec 需要修改）

## Impact

- `src/frontend/src/components/Admin/AdminSideBar/index.tsx` — 主要改动文件
- `src/frontend/src/app/admin/layout.tsx` — 可能需要添加权限守卫
- 依赖 `src/frontend/src/stores/authStore.ts` 和 `src/frontend/src/utils/RoleUtils.ts`
