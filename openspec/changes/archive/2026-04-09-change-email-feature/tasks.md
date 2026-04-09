## 1. 后端 - 验证码场景扩展

- [x] 1.1 扩展 `SendVerificationCodeRequestDTO`，新增可选 `scene` 字段（默认 `login`）
- [x] 1.2 修改 `VerificationCodeDomainService` 的 Redis key 格式为 `verification-code:{scene}:{email}`，验证和发送方法均需传入 scene 参数
- [x] 1.3 修改 `VerificationCodeRepository` 实现，Redis 操作使用带 scene 的 key
- [x] 1.4 新增 `POST /api/v1/user/email/verification-code/send` 认证接口，接收 `{ email, scene }` 参数，用于修改邮箱场景发送验证码
- [x] 1.5 更新 `AuthController` 中 `sendVerificationCode` 接口，向后兼容（scene 默认 `login`）

## 2. 后端 - 修改邮箱接口

- [x] 2.1 创建 `ChangeEmailRequestDTO`（包含 `originalEmailVerifyCode`、`newEmail`、`newEmailVerifyCode`）
- [x] 2.2 在 `UserService` / `UserServiceImpl` 中实现 `changeEmail` 方法：验证原邮箱验证码、验证新邮箱验证码、校验新邮箱唯一性、更新邮箱
- [x] 2.3 在 `UserController` 中新增 `PUT /api/v1/user/email` 接口，需 `@RequiresPermission(access = AccessLevel.AUTHENTICATED)`
- [x] 2.4 编写 `ChangeEmail` 相关单元测试（验证码校验、邮箱唯一性、并发冲突等场景）

## 3. 前端 - API 层

- [x] 3.1 在 `user.service.ts` 中新增 `changeEmail(data: ChangeEmailRequestDTO)` 方法
- [x] 3.2 在 `user.service.ts` 中新增 `sendEmailVerificationCode(data: { email: string; scene: string })` 方法
- [x] 3.3 在 `types/profile.ts` 或 `apis/schema/type.ts` 中新增相关类型定义

## 4. 前端 - ChangeEmailModal 组件

- [x] 4.1 创建 `src/components/Profile/ProfileInfo/ChangeEmailModal.tsx` 组件骨架
- [x] 4.2 实现 Step 1（验证原邮箱）：显示脱敏邮箱、验证码输入 + 发送按钮 + 60 秒倒计时
- [x] 4.3 实现 Step 2（填写新邮箱）：新邮箱输入框 + 格式校验 + 与原邮箱相同校验
- [x] 4.4 实现 Step 3（验证新邮箱）：显示脱敏新邮箱、验证码输入 + 发送按钮 + 60 秒倒计时
- [x] 4.5 实现步骤导航（上一步/下一步/确认修改）和表单状态管理
- [x] 4.6 实现暗色主题样式，与 Profile 页面风格一致

## 5. 前端 - 集成到 ProfileInfo

- [x] 5.1 修改 `ProfileInfo/index.tsx`，启用"修改邮箱"按钮（移除 `disabled`）
- [x] 5.2 添加 `ChangeEmailModal` 的 `open` 状态管理，点击按钮打开 Modal
- [x] 5.3 修改成功后调用 `onUpdate?.()` 刷新用户信息

## 6. 验证

- [x] 6.1 前端构建通过（`pnpm build`）- lint 通过，build 的类型错误是预存在的 AchievementCard 问题
- [x] 6.2 后端构建通过（`mvn compile`）
- [x] 6.3 端到端手动测试完整修改邮箱流程
