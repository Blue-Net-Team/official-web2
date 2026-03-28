## Purpose

登录功能集成，实现前端登录表单与后端认证 API 的完整对接，包括密码哈希处理、API 调用、Token 存储和全局状态管理。

## Requirements

### Requirement: 密码前端哈希处理

系统 SHALL 在发送登录请求前对用户密码进行 SHA-256 哈希处理。

#### Scenario: 密码哈希处理流程
- **WHEN** 用户提交登录表单
- **THEN** 系统 SHALL 使用 Web Crypto API 对密码进行 SHA-256 哈希
- **THEN** 哈希结果 SHALL 转换为小写十六进制字符串
- **THEN** 哈希后的密码 SHALL 作为请求体的 password 字段发送

#### Scenario: 哈希函数实现
- **WHEN** 调用密码哈希函数
- **THEN** 函数 SHALL 使用 `crypto.subtle.digest('SHA-256', data)` 方法
- **THEN** 函数 SHALL 返回 Promise<string> 类型
- **THEN** 输出格式 SHALL 为 64 位小写十六进制字符串

### Requirement: 认证状态管理

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

系统 SHALL 在登录页面集成认证 API 调用，提供完整的登录交互体验。

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
