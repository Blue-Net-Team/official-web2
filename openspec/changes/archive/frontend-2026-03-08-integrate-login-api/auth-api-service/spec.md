## Purpose

扩展现有认证 API 服务规范，增加密码哈希处理说明和错误处理规范。此为对现有 `auth-api-service` 规范的增量修改。

## Delta Type: EXTEND

此规范扩展 `openspec/specs/auth-api-service/spec.md`，不修改现有需求，仅添加新需求。

## Requirements

### Requirement: 密码哈希处理说明

扩展登录请求说明，明确密码字段需先进行 SHA-256 哈希处理。

#### Scenario: 登录请求密码处理
- **WHEN** 调用 `authService.login(credentials)` 方法
- **THEN** 方法内部 SHALL NOT 直接发送原始密码
- **THEN** 方法 SHALL 调用密码哈希工具函数处理密码
- **THEN** 哈希后的密码 SHALL 作为请求体的 password 字段

**注意**: 此要求扩展原有规范中的"学号登录接口"场景，不改变接口签名，仅改变内部实现。

### Requirement: 错误响应处理规范

扩展错误处理规范，明确各类错误场景的处理方式。

#### Scenario: 认证失败错误（401）
- **WHEN** 后端返回 code=401
- **THEN** 错误信息 SHALL 从响应的 msg 字段获取
- **THEN** 常见错误信息包括"学号或密码错误"、"账户已被禁用"

#### Scenario: 网络超时错误（408）
- **WHEN** 请求超时
- **THEN** axios 拦截器 SHALL 返回 `{ code: 408, msg: '请求超时', data: null }`
- **THEN** 调用方 SHALL 处理此错误并提示用户

#### Scenario: 服务器错误（5xx）
- **WHEN** 后端返回 5xx 状态码
- **THEN** 系统 SHALL 显示"服务器错误，请稍后重试"
- **THEN** 系统 SHALL 记录错误日志

### Requirement: 登出接口调用规范

明确登出接口的调用时机和错误处理。

#### Scenario: 主动登出
- **WHEN** 用户点击登出按钮
- **THEN** 系统 SHALL 调用 `authService.logout()` 方法
- **THEN** 无论 API 调用成功与否，系统 SHALL 清除本地认证状态

#### Scenario: Token 失效登出
- **WHEN** axios 响应拦截器检测到 401 错误
- **THEN** 拦截器 SHALL 自动清除 localStorage 中的 token
- **THEN** 拦截器 SHALL 重定向到登录页面

#### Scenario: 登出 API 失败处理
- **WHEN** 登出 API 调用失败
- **THEN** 系统 SHALL 仍然清除本地认证状态
- **THEN** 系统 SHALL 不阻塞用户登出流程
