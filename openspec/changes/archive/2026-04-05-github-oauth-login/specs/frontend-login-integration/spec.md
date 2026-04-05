## MODIFIED Requirements

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
