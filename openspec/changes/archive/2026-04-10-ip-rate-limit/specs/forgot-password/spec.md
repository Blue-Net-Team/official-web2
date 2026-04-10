## MODIFIED Requirements

### Requirement: 发送验证码接口
系统 SHALL 提供公开接口 `POST /api/v1/auth/reset-password/send-code`，接收 resetToken，向流程中已验证的邮箱发送 6 位验证码。使用 `VerificationCodeDomainService`，scene=`reset_password`。该接口 SHALL 添加 `@RateLimit(interval=60)` 注解实现 IP 级限频。

#### Scenario: 成功发送验证码
- **WHEN** 用户提交有效 resetToken，Redis 流程状态 step >= 2
- **THEN** 系统 SHALL 从 Redis 获取已验证邮箱
- **THEN** 系统 SHALL 调用 `VerificationCodeDomainService.generateCode(email, "reset_password")`
- **THEN** 系统 SHALL 更新 Redis 流程状态 step=3
- **THEN** 系统 SHALL 返回 `{ code: 200, msg: "验证码已发送" }`

#### Scenario: IP 限频拒绝
- **WHEN** 同一 IP 在 60 秒内再次请求发送验证码（无论邮箱是否相同）
- **THEN** 系统 SHALL 由 `@RateLimit` 注解拦截，返回 HTTP 429

#### Scenario: resetToken 无效或过期
- **WHEN** resetToken 在 Redis 中不存在
- **THEN** 系统 SHALL 返回 `{ code: 400, msg: "重置流程已过期，请重新开始" }`

#### Scenario: 跳步访问
- **WHEN** Redis 流程状态 step < 2
- **THEN** 系统 SHALL 返回 `{ code: 400, msg: "请先完成上一步验证" }`

## REMOVED Requirements

### Requirement: 60 秒内重复发送（邮箱维度）
**Reason**: 限频逻辑从应用层邮箱维度数据库查询迁移到 `@RateLimit` 注解的 IP 维度 Redis 限频
**Migration**: 邮箱维度限频由 `@RateLimit` 的 IP 限频替代，`VerificationCodeRepository.findLatestByEmailAndSceneWithinSeconds` 方法及其调用被移除
