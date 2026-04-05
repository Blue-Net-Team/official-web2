## ADDED Requirements

### Requirement: 系统生成并发送邮箱验证码
系统 SHALL 在用户请求时生成 6 位随机数字验证码，存储到 `tb_verify_code` 表，并通过邮件发送到指定邮箱。验证码有效期为 5 分钟。

#### Scenario: 成功发送验证码
- **WHEN** 用户请求发送验证码到已填写但未注册的邮箱 `test@example.com`
- **THEN** 系统 SHALL 生成 6 位数字验证码，存储到数据库（target=test@example.com, expire_at=当前时间+5分钟），通过邮件发送验证码，返回成功响应（不暴露邮箱是否注册）

#### Scenario: 成功发送验证码到已注册邮箱
- **WHEN** 用户请求发送验证码到已注册邮箱 `user@example.com`
- **THEN** 系统 SHALL 生成 6 位数字验证码，存储到数据库，通过邮件发送验证码，返回成功响应

#### Scenario: 60秒内重复发送
- **WHEN** 用户在 60 秒内再次请求发送验证码到同一邮箱
- **THEN** 系统 SHALL 拒绝请求，返回错误提示"发送过于频繁，请稍后再试"

#### Scenario: 邮箱格式无效
- **WHEN** 用户提交的邮箱格式不合法（如 `invalid-email`）
- **THEN** 系统 SHALL 拒绝请求，返回参数校验错误

### Requirement: 邮箱验证码登录认证
系统 SHALL 支持通过邮箱和验证码进行登录。登录成功后设置 JWT Cookie + CSRF Token Cookie，行为与学号登录完全一致。

#### Scenario: 邮箱和验证码正确
- **WHEN** 用户提交已注册邮箱 `user@example.com` 和正确的 6 位验证码
- **THEN** 系统 SHALL 验证通过，生成 JWT Token 写入白名单，设置 auth_token 和 csrf_token Cookie，返回 `{ csrfToken, userInfo }`，并将该验证码标记为已使用

#### Scenario: 验证码错误
- **WHEN** 用户提交正确的邮箱但验证码不正确
- **THEN** 系统 SHALL 返回 401 错误"邮箱或验证码错误"

#### Scenario: 验证码已过期
- **WHEN** 用户提交的验证码已超过 5 分钟有效期
- **THEN** 系统 SHALL 返回 401 错误"邮箱或验证码错误"

#### Scenario: 验证码已被使用
- **WHEN** 用户提交的验证码已被使用（used_at 不为空）
- **THEN** 系统 SHALL 返回 401 错误"邮箱或验证码错误"

#### Scenario: 邮箱未注册
- **WHEN** 用户提交的邮箱在系统中不存在
- **THEN** 系统 SHALL 返回 401 错误"邮箱或验证码错误"

#### Scenario: 账户已禁用
- **WHEN** 用户提交的邮箱对应账户已被禁用
- **THEN** 系统 SHALL 返回 401 错误"账户已被禁用"

### Requirement: 验证码发送接口为公开接口
发送验证码接口 SHALL 无需认证，但需加入 CSRF 白名单。

#### Scenario: 未登录用户发送验证码
- **WHEN** 未登录用户调用发送验证码接口
- **THEN** 系统 SHALL 正常处理请求，无需 CSRF Token

### Requirement: 邮箱登录接口为公开接口
邮箱验证码登录接口 SHALL 无需认证，需加入 CSRF 白名单（遵循 `/api/v1/auth/login/**` 模式）。

#### Scenario: 未登录用户邮箱登录
- **WHEN** 未登录用户调用邮箱登录接口
- **THEN** 系统 SHALL 正常处理请求，无需 CSRF Token
