## MODIFIED Requirements

### Requirement: 系统生成并发送邮箱验证码
系统 SHALL 在用户请求时生成 6 位随机数字验证码，存储到 `tb_verify_code` 表，并通过邮件发送到指定邮箱。验证码有效期为 5 分钟。该接口 SHALL 添加 `@RateLimit(interval=60)` 注解实现 IP 级限频。

#### Scenario: 成功发送验证码
- **WHEN** 用户请求发送验证码到已填写但未注册的邮箱 `test@example.com`
- **THEN** 系统 SHALL 生成 6 位数字验证码，存储到数据库（target=test@example.com, expire_at=当前时间+5分钟），通过邮件发送验证码，返回成功响应（不暴露邮箱是否注册）

#### Scenario: 成功发送验证码到已注册邮箱
- **WHEN** 用户请求发送验证码到已注册邮箱 `user@example.com`
- **THEN** 系统 SHALL 生成 6 位数字验证码，存储到数据库，通过邮件发送验证码，返回成功响应

#### Scenario: IP 限频拒绝
- **WHEN** 同一 IP 在 60 秒内再次请求发送验证码
- **THEN** 系统 SHALL 由 `@RateLimit` 注解拦截，返回 HTTP 429

#### Scenario: 邮箱格式无效
- **WHEN** 用户提交的邮箱格式不合法（如 `invalid-email`）
- **THEN** 系统 SHALL 拒绝请求，返回参数校验错误

## REMOVED Requirements

### Requirement: 60秒内重复发送（邮箱维度）
**Reason**: 限频逻辑从应用层邮箱维度数据库查询迁移到 `@RateLimit` 注解的 IP 维度 Redis 限频
**Migration**: `VerificationCodeRepository.findLatestByEmailWithinSeconds` 方法及其调用被移除
