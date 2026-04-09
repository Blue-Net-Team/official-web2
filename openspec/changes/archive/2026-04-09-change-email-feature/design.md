## Context

个人资料页面（Profile）的邮箱设置区域目前只展示已绑定邮箱，"修改邮箱"按钮被禁用。项目已有邮箱验证码发送机制（用于邮箱登录），后端 `VerificationCodeDomainService` 通过 Redis 存储验证码，有效期 5 分钟，60 秒内同一邮箱只能发送一次。

当前验证码发送接口 `POST /api/v1/auth/verification-code/send` 为公开接口，仅接收 `{ email }` 参数，无场景区分。这存在安全隐患：理论上可将登录验证码用于修改邮箱。

技术栈约束：
- 前端：Next.js 15 + React 19 + Ant Design 6 + Zustand + TailwindCSS
- 后端：Spring Boot 3.5.10 + Java 21 + DDD 四层架构
- 现有 Modal 模式参考：`AvatarCropModal`（通过 `open` prop 控制显隐）
- 现有验证码模式参考：登录页 `sendVerificationCode`（60 秒倒计时）

## Goals / Non-Goals

**Goals:**
- 提供安全的三步邮箱修改流程：验证原邮箱 → 填写新邮箱 → 验证新邮箱
- 复用现有验证码基础设施，避免重复造轮子
- UI 与现有 Profile 页面暗色主题风格一致
- 验证码按场景隔离，防止跨场景滥用

**Non-Goals:**
- 不实现邮箱解绑功能（仅支持换绑）
- 不实现密码修改功能（独立功能）
- 不修改现有登录验证码流程的行为

## Decisions

### 1. 验证码场景隔离方案

**决策**：在现有验证码系统中引入 `scene`（场景）参数，Redis key 格式从 `verification-code:{email}` 变为 `verification-code:{scene}:{email}`。

**理由**：最小改动方案。现有登录验证码逻辑不变（scene 默认为 `login`），新增 `change-email-original` 和 `change-email-new` 两个场景。修改邮箱接口在验证时指定 scene，确保验证码不被跨场景使用。

**替代方案**：为修改邮箱创建独立的验证码服务和接口 → 拒绝，过度设计，违反 DRY 原则。

### 2. 前端 UI 方案

**决策**：使用 Ant Design `Modal` + `Steps` 组件实现分步弹窗表单，创建独立组件 `ChangeEmailModal.tsx`。

**理由**：
- Modal 弹窗不离开当前页面，体验流畅
- Steps 组件直观展示三步进度
- 与项目已有 Modal 模式（AvatarCropModal、ExperienceSection Modal）保持一致
- 独立组件便于维护和测试

### 3. API 设计

**决策**：新增单一接口 `PUT /api/v1/user/email`，一次性提交所有验证信息。

请求体：
```json
{
  "originalEmailVerifyCode": "123456",
  "newEmail": "new@example.com",
  "newEmailVerifyCode": "654321"
}
```

**理由**：
- 后端在一个事务中完成所有验证和更新，避免中间状态
- 前端分步收集信息，最终一次性提交
- 减少网络往返

**替代方案**：分三步调接口（验证原邮箱 → 提交新邮箱 → 验证新邮箱）→ 拒绝，增加复杂度和网络开销，且需维护中间状态。

### 4. 验证码发送接口扩展

**决策**：扩展现有 `POST /api/v1/auth/verification-code/send` 接口，增加可选 `scene` 字段（默认 `login`）。同时新增需要认证的 `POST /api/v1/user/email/verification-code/send` 接口，用于发送修改邮箱场景的验证码。

**理由**：
- 修改邮箱时发送验证码到原邮箱需要确认是本人操作，应使用需要认证的接口
- 发送到新邮箱的验证码也应通过认证接口发送
- 原有公开接口保持不变，不影响登录流程

## Risks / Trade-offs

- **[验证码有效期 5 分钟]** → 如果用户在三步流程中操作时间过长，原邮箱验证码可能过期。缓解：前端在 Modal 中明确提示验证码有效期，且验证码输入框旁提供重新发送按钮。
- **[并发修改]** → 用户同时在多个设备修改邮箱可能导致冲突。缓解：后端在更新时校验当前邮箱是否与原邮箱一致，不一致则拒绝。
- **[邮箱已被其他用户绑定]** → 新邮箱可能已被其他账号使用。缓解：后端在修改前检查新邮箱唯一性，返回明确错误信息。
