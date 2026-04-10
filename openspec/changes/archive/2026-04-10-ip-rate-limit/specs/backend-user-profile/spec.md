## ADDED Requirements

### Requirement: 修改邮箱验证码发送接口 IP 限频
修改邮箱的验证码发送接口 `POST /api/v1/user/email/verification-code/send` SHALL 添加 `@RateLimit(interval=60)` 注解实现 IP 级限频。

#### Scenario: IP 限频拒绝
- **WHEN** 同一 IP 在 60 秒内再次请求发送修改邮箱验证码
- **THEN** 系统 SHALL 由 `@RateLimit` 注解拦截，返回 HTTP 429

## REMOVED Requirements

### Requirement: 修改邮箱验证码发送的邮箱维度限频
**Reason**: 限频逻辑从应用层邮箱维度数据库查询迁移到 `@RateLimit` 注解的 IP 维度 Redis 限频
**Migration**: `UserInfoServiceImpl.sendEmailVerificationCode()` 中的 `findLatestByEmailAndSceneWithinSeconds` 调用被移除
