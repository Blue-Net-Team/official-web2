## Why

当前个人资料页面的邮箱设置区域（ProfileInfo 组件）仅展示已绑定邮箱，"修改邮箱"按钮处于禁用状态，用户无法自行更换绑定邮箱。用户可能因毕业、更换常用邮箱等原因需要更新绑定邮箱，需要提供安全的邮箱变更流程。

## What Changes

- 新增修改邮箱功能：通过三步验证流程（验证原邮箱 → 填写新邮箱 → 验证新邮箱）完成邮箱变更
- 新增前端 `ChangeEmailModal` 组件，使用 Ant Design Modal + Steps 实现分步表单
- 新增前端 `user.service` 中的修改邮箱 API 调用方法
- 启用 ProfileInfo 中已禁用的"修改邮箱"按钮，点击后打开 Modal
- 后端新增修改邮箱接口（`PUT /api/v1/user/email`），要求验证原邮箱和新邮箱的验证码
- 后端扩展验证码发送接口，支持不同场景（scene）的验证码，避免验证码被跨场景滥用

## Capabilities

### New Capabilities
- `change-email`: 用户通过验证码验证原邮箱和新邮箱后安全修改绑定邮箱的完整流程

### Modified Capabilities

（无现有 capability 需要修改）

## Impact

- **前端**：`src/components/Profile/ProfileInfo/` 新增 `ChangeEmailModal.tsx`；修改 `ProfileInfo/index.tsx` 启用修改按钮并集成 Modal
- **前端 API 层**：`user.service.ts` 新增 `changeEmail` 方法；`auth.service.ts` 可能需扩展 `sendVerificationCode` 参数
- **后端 API**：新增 `PUT /api/v1/user/email` 接口（需认证）
- **后端验证码**：现有 `VerificationCodeDomainService` 可能需扩展场景区分能力
- **数据库**：无表结构变更，仅更新 `tb_user.email` 字段
