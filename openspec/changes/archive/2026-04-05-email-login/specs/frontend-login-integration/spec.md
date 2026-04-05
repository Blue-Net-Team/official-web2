## MODIFIED Requirements

### Requirement: 前端认证服务支持邮箱登录 API
前端认证服务 SHALL 提供发送验证码和邮箱登录两个 API 调用方法。

#### Scenario: 调用发送验证码 API
- **WHEN** 前端调用 `authService.sendVerificationCode(email)`
- **THEN** 系统 SHALL 向 `POST /api/v1/auth/verification-code/send` 发送请求，携带邮箱地址

#### Scenario: 调用邮箱登录 API
- **WHEN** 前端调用 `authService.loginWithEmail(email, code)`
- **THEN** 系统 SHALL 向 `POST /api/v1/auth/login/email` 发送请求，携带邮箱和验证码

### Requirement: 前端登录页面支持邮箱登录
前端登录页面的邮箱登录 Tab SHALL 对接真实后端 API，替换当前模拟逻辑。

#### Scenario: 发送验证码成功
- **WHEN** 用户填写邮箱后点击"获取验证码"按钮
- **THEN** 前端 SHALL 调用发送验证码 API，成功后启动 60 秒倒计时，显示成功提示

#### Scenario: 发送验证码失败（频率限制）
- **WHEN** 用户在 60 秒内再次点击"获取验证码"
- **THEN** 前端 SHALL 显示后端返回的错误信息（如"发送过于频繁"）

#### Scenario: 邮箱登录成功
- **WHEN** 用户填写邮箱和验证码后点击"登录"按钮
- **THEN** 前端 SHALL 调用邮箱登录 API，成功后跳转到首页

#### Scenario: 邮箱登录失败
- **WHEN** 邮箱或验证码不正确
- **THEN** 前端 SHALL 显示错误提示"邮箱或验证码错误"
