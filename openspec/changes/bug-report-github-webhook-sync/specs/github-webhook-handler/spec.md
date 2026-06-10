## ADDED Requirements

### Requirement: Webhook 端点可以接收 GitHub issues 事件
系统 SHALL 提供一个公开的 HTTP 端点，用于接收 GitHub 发送的 `issues` 类型 Webhook 事件。

#### Scenario: 成功接收 issues 事件
- **WHEN** GitHub 发送 `issues` 类型 Webhook 请求到端点
- **THEN** 系统接收并解析请求体中的事件类型和 Issue 数据

#### Scenario: 接收非 issues 类型事件
- **WHEN** GitHub 发送 `pull_request` 或其他类型 Webhook 请求
- **THEN** 系统返回 HTTP 200 但不进行任何业务处理

#### Scenario: 请求体格式异常
- **WHEN** Webhook 请求体不是合法的 JSON
- **THEN** 系统返回 HTTP 200 并记录错误日志，不抛出异常

### Requirement: Webhook 请求必须通过 HMAC-SHA256 签名验证
系统 SHALL 验证每个 Webhook 请求的 `X-Hub-Signature-256` header，使用配置的 `webhook-secret` 进行 HMAC-SHA256 签名比对，拒绝验证失败的请求。

#### Scenario: 签名验证通过
- **WHEN** Webhook 请求携带有效的 `X-Hub-Signature-256` header
- **THEN** 系统通过验证并继续处理事件

#### Scenario: 签名验证失败
- **WHEN** Webhook 请求携带的签名与计算结果不匹配
- **THEN** 系统返回 HTTP 401 Unauthorized，不处理事件

#### Scenario: 缺少签名 header
- **WHEN** Webhook 请求未携带 `X-Hub-Signature-256` header
- **THEN** 系统返回 HTTP 401 Unauthorized，不处理事件

#### Scenario: Webhook 配置未启用
- **WHEN** 系统未配置 `github.app.webhook-secret`
- **THEN** 系统返回 HTTP 401 Unauthorized，拒绝所有 Webhook 请求

### Requirement: Webhook 处理失败不影响响应
系统 SHALL 在 Webhook 业务处理过程中发生的任何异常（如数据库查询失败、BugReport 不存在）均不向上传播为 HTTP 错误响应。

#### Scenario: 对应的 BugReport 不存在
- **WHEN** 接收到 `issues.closed` 事件，但平台中无对应 `github_issue_number` 的记录
- **THEN** 系统记录警告日志，仍返回 HTTP 200

#### Scenario: 数据库更新失败
- **WHEN** 状态更新过程中数据库异常
- **THEN** 系统记录错误日志，仍返回 HTTP 200，GitHub 不会重试
