## Why

导航栏已有"修改密码"菜单项（指向 `/change-password`），但该页面尚未实现，用户点击后会遇到 404。已登录用户需要一种安全的方式来修改自己的密码，同时这个流程应与"忘记密码"（未登录用户的密码重置）明确区分。

## What Changes

- 新增后端接口 `POST /api/v1/user/password/verify`：验证当前密码，验证通过后生成 Redis token
- 新增后端接口 `PUT /api/v1/user/password`：携带 Redis token 提交新密码，更新密码并吊销所有已登录 Token
- 新增 `ChangePasswordStateService`：参照 `ResetPasswordStateService` 的 Redis 状态管理模式
- 新增前端页面 `/change-password`：使用 Ant Design Steps 的两步向导（验证原密码 → 设置新密码）
- 新增前端 API 函数 `changePasswordVerify()` 和 `changePassword()`

## Capabilities

### New Capabilities
- `change-password`: 已登录用户修改密码的两步验证流程，包含原密码验证、新密码设置、Redis 状态管理

### Modified Capabilities

## Impact

- **后端接口层**：`UserProfileController` 新增两个端点
- **后端应用层**：`UserInfoService` / `UserInfoServiceImpl` 新增方法
- **后端领域层**：`UserDomainService` 新增 `changePassword()` 方法
- **后端基础设施层**：新增 `ChangePasswordStateService`（Redis）
- **前端页面**：新增 `src/frontend/src/app/(public)/(other)/change-password/page.tsx`
- **前端 API**：`user.service.ts` 新增两个 API 函数
- **无数据库变更**：复用已有的 `UserRepository.updatePassword()` 和 `UserMapper.updatePassword()`
