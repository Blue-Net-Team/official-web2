## MODIFIED Requirements

### Requirement: 认证服务支持邮箱登录
认证服务 SHALL 同时支持学号密码登录和邮箱验证码登录两种方式。

#### Scenario: 邮箱验证码登录成功
- **WHEN** 用户通过邮箱和验证码登录，凭证验证通过
- **THEN** 系统 SHALL 生成 JWT Token、设置 Cookie、返回用户信息，流程与学号登录一致

#### Scenario: 认证服务发送验证码
- **WHEN** 认证服务接收到发送验证码请求
- **THEN** 系统 SHALL 调用领域服务生成验证码，调用邮件服务发送，并校验发送频率

### Requirement: CSRF 白名单包含发送验证码接口
CSRF 过滤器的白名单 SHALL 包含发送验证码接口路径 `/api/v1/auth/verification-code/send`。

#### Scenario: 发送验证码请求无需 CSRF Token
- **WHEN** 未登录用户 POST 请求 `/api/v1/auth/verification-code/send`
- **THEN** CSRF 过滤器 SHALL 放行该请求
