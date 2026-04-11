## 1. 基础设施层 — Redis 状态管理

- [x] 1.1 新建 `ChangePasswordStateService`（`infrastructure/security/change/`），实现 Redis Hash 的创建、验证、更新、删除操作，key 前缀 `change_pwd:`，TTL 15 分钟
- [x] 1.2 编写 `ChangePasswordStateService` 单元测试

## 2. 领域层 — 密码修改逻辑

- [x] 2.1 在 `UserDomainService` 接口新增 `changePassword(Long userId, String rawNewPassword)` 方法
- [x] 2.2 在 `UserDomainServiceImpl` 实现密码编码 + 调用 `userRepository.updatePassword()`
- [x] 2.3 编写 `UserDomainServiceImpl.changePassword()` 单元测试

## 3. 应用层 — 编排修改密码流程

- [x] 3.1 在 `UserInfoService` 接口新增 `verifyCurrentPassword(Long userId, String currentPassword)` 和 `changePassword(Long userId, String token, String newPassword, String confirmPassword)` 方法
- [x] 3.2 在 `UserInfoServiceImpl` 实现验证原密码（创建 Redis 状态）和提交新密码（验证 token → 调用领域服务 → 吊销 Token → 删除 Redis key）的完整流程
- [x] 3.3 编写 `UserInfoServiceImpl` 修改密码相关单元测试

## 4. 接口层 — REST API

- [x] 4.1 新建 `VerifyPasswordRequestDTO` 和 `ChangePasswordRequestDTO`（含 `@Schema` 注解）
- [x] 4.2 在 `UserProfileController` 新增 `POST /api/v1/user/password/verify` 和 `PUT /api/v1/user/password` 端点，添加 `@RequiresPermission(access = AccessLevel.AUTHENTICATED)` 和 `@Operation` 注解

## 5. 前端 — API 层

- [x] 5.1 在 `user.service.ts` 新增 `verifyPassword(currentPassword)` 和 `changePassword(token, newPassword, confirmPassword)` 方法
- [x] 5.2 在 `assessment.dto.ts` 或新建 schema 文件中定义相关请求/响应类型

## 6. 前端 — 修改密码页面

- [x] 6.1 创建 `src/frontend/src/app/(public)/(other)/change-password/page.tsx`，实现两步向导（Ant Design Steps）：Step 1 验证原密码，Step 2 设置新密码
- [x] 6.2 添加样式（与 Profile 页面一致的暗色主题布局，居中表单卡片）
- [x] 6.3 实现 Step 1 底部"忘记密码"提示（引导用户退出登录后使用忘记密码流程）
- [x] 6.4 实现修改成功后自动跳转到登录页

## 7. 集成验证

- [x] 7.1 手动验证完整流程：NavBar 进入 → 验证原密码 → 设置新密码 → 强制重新登录
- [x] 7.2 验证异常场景：错误密码、token 过期、密码不一致
