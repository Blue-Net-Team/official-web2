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

系统 SHALL 使用 Zustand 管理全局认证状态，并持久化到 localStorage。

#### Scenario: 认证状态结构
- **WHEN** 创建认证 Store
- **THEN** 状态 SHALL 包含 `token: string | null`
- **THEN** 状态 SHALL 包含 `userInfo: UserInfo | null`
- **THEN** 状态 SHALL 包含 `isAuthenticated: boolean`
- **THEN** 状态 SHALL 包含 `isLoading: boolean`

#### Scenario: 登录方法
- **WHEN** 调用 `login(credentials)` 方法
- **THEN** 方法 SHALL 先对密码进行哈希处理
- **THEN** 方法 SHALL 调用 `authService.login()` 发送请求
- **THEN** 登录成功时 SHALL 更新 token 和 userInfo
- **THEN** 登录成功时 SHALL 设置 isAuthenticated 为 true
- **THEN** 登录失败时 SHALL 抛出错误

#### Scenario: 登出方法
- **WHEN** 调用 `logout()` 方法
- **THEN** 方法 SHALL 调用 `authService.logout()` 发送请求
- **THEN** 方法 SHALL 清除 token 和 userInfo
- **THEN** 方法 SHALL 设置 isAuthenticated 为 false

#### Scenario: 状态持久化
- **WHEN** 认证状态发生变化
- **THEN** 系统 SHALL 自动将状态持久化到 localStorage
- **WHEN** 页面刷新或重新加载
- **THEN** 系统 SHALL 从 localStorage 恢复认证状态

### Requirement: 登录页面集成

系统 SHALL 在登录页面集成认证 API 调用，提供完整的登录交互体验，包括学号登录和 GitHub OAuth2 登录。

#### Scenario: 登录表单提交
- **WHEN** 用户填写学号和密码并点击登录按钮
- **THEN** 系统 SHALL 进行表单验证
- **THEN** 系统 SHALL 显示加载状态（按钮禁用 + loading 图标）
- **THEN** 系统 SHALL 调用认证 Store 的 login 方法

#### Scenario: 登录成功处理
- **WHEN** 登录 API 返回成功响应（code=200）
- **THEN** 系统 SHALL 显示成功提示消息
- **THEN** 系统 SHALL 跳转到首页 `/`

#### Scenario: 登录失败处理
- **WHEN** 登录 API 返回失败响应（code=401 或其他错误）
- **THEN** 系统 SHALL 显示错误提示消息
- **THEN** 系统 SHALL 保持用户在登录页面
- **THEN** 系统 SHALL 恢复按钮可点击状态

#### Scenario: 网络错误处理
- **WHEN** 网络请求超时或失败
- **THEN** 系统 SHALL 显示"网络错误，请稍后重试"提示
- **THEN** 系统 SHALL 恢复按钮可点击状态

#### Scenario: GitHub 登录
- **WHEN** 用户点击"使用 GitHub 登录"按钮
- **THEN** 系统 SHALL 调用 `GET /api/v1/auth/github` 获取授权 URL
- **THEN** 系统 SHALL 将浏览器重定向到该授权 URL

#### Scenario: GitHub 登录回调成功
- **WHEN** 页面加载时 URL 包含 `?github=success` 参数
- **THEN** 系统 SHALL 调用 `checkAuthStatus()` 刷新认证状态
- **THEN** 系统 SHALL 跳转到首页 `/`

#### Scenario: GitHub 登录回调 - 未绑定账号
- **WHEN** 页面加载时 URL 包含 `?github=unbound` 参数
- **THEN** 系统 SHALL 显示提示"请先使用学号登录，然后在个人设置中绑定 GitHub 账号"

#### Scenario: GitHub 登录回调 - 授权失败
- **WHEN** 页面加载时 URL 包含 `?github=error` 参数
- **THEN** 系统 SHALL 显示错误提示"GitHub 登录失败，请稍后重试"

### Requirement: 用户反馈

系统 SHALL 使用 Ant Design message 组件提供用户操作反馈。

#### Scenario: 加载状态提示
- **WHEN** 登录请求进行中
- **THEN** 登录按钮 SHALL 显示 loading 状态
- **THEN** 登录按钮 SHALL 处于禁用状态

#### Scenario: 成功提示
- **WHEN** 登录成功
- **THEN** 系统 SHALL 使用 `message.success()` 显示"登录成功"

#### Scenario: 错误提示
- **WHEN** 登录失败
- **THEN** 系统 SHALL 使用 `message.error()` 显示错误信息
- **THEN** 错误信息 SHALL 来自后端响应的 msg 字段
