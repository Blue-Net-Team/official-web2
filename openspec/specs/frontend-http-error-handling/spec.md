## ADDED Requirements

### Requirement: 后端错误消息优先展示

系统 SHALL 在 Axios 请求失败时优先读取并非空判断响应体中的 `ResponseMessage.msg`，且 MUST 通过 Ant Design `message.error()` 展示该消息，而不是状态码默认文案或 Axios 原始英文错误。

#### Scenario: HTTP 错误包含后端业务消息

- **WHEN** 任意 Axios 客户端收到 HTTP 400 且响应体包含 `msg: "学号不存在"`
- **THEN** 系统仅提示一次“学号不存在”
- **AND** 系统不展示 400 默认文案或 `Request failed with status code 400`

#### Scenario: 业务错误响应包含后端消息

- **WHEN** 服务端以 HTTP 200 返回 `code >= 400` 且包含非空 `msg`
- **THEN** 系统仅提示一次该后端消息

#### Scenario: 后端消息仅包含空白

- **WHEN** 错误响应中的 `msg` 为空字符串或仅包含空白字符
- **THEN** 系统将其视为缺失并进入状态码默认文案流程

### Requirement: 常见 HTTP 错误具有中文默认文案

系统 SHALL 为 400、401、403、404、405、408、409、413、415、422、429、500、502、503 和 504 提供用户可读的中文默认提示，并 MUST 在后端未提供有效消息时使用对应提示。

#### Scenario: 请求参数错误没有响应消息

- **WHEN** 请求收到 HTTP 400 且没有有效 `ResponseMessage.msg`
- **THEN** 系统提示“请求参数有误，请检查后重试”

#### Scenario: 权限不足没有响应消息

- **WHEN** 请求收到 HTTP 403 且没有有效 `ResponseMessage.msg`
- **THEN** 系统提示“没有权限执行此操作”

#### Scenario: 资源不存在没有响应消息

- **WHEN** 请求收到 HTTP 404 且没有有效 `ResponseMessage.msg`
- **THEN** 系统提示“请求的资源不存在或已被删除”

#### Scenario: 服务器错误没有响应消息

- **WHEN** 请求收到 HTTP 500 且没有有效 `ResponseMessage.msg`
- **THEN** 系统提示“服务器内部错误，请稍后重试”

#### Scenario: 非 JSON 或空错误响应

- **WHEN** 请求收到已映射状态码但响应体为空、不是 JSON 或不符合 `ResponseMessage` 结构
- **THEN** 系统安全使用该状态码的中文默认提示
- **AND** 系统不因解析响应体产生额外异常

### Requirement: 网络和未知错误具有最终兜底

系统 SHALL 区分请求超时、未收到 HTTP 响应的网络异常及其他未知失败，并 MUST 提供不暴露 Axios 英文错误的中文提示。

#### Scenario: 请求超时

- **WHEN** Axios 请求因超时失败
- **THEN** 系统提示“请求超时，请稍后重试”

#### Scenario: 网络连接异常

- **WHEN** 请求失败且未收到 HTTP 响应，同时不属于超时
- **THEN** 系统提示“网络连接异常，请检查网络后重试”

#### Scenario: 未映射的 HTTP 状态码

- **WHEN** 请求收到未配置默认文案的 HTTP 错误且没有有效后端消息
- **THEN** 系统提示“请求失败，请稍后重试”
- **AND** 系统不直接展示裸状态码或 Axios 原始英文消息

### Requirement: 错误提示不改变请求控制流

系统 MUST 在显示错误提示后继续拒绝原始请求 Promise，并 SHALL 保留现有 401 登录跳转行为，使调用方能够清理状态且不会将失败误判为成功。

#### Scenario: 提示后错误继续传播

- **WHEN** 拦截器已展示 API 错误消息
- **THEN** 请求 Promise 仍处于 rejected 状态
- **AND** 调用方 catch 或错误状态处理仍可执行

#### Scenario: 未认证请求

- **WHEN** 认证客户端收到 HTTP 401
- **THEN** 系统展示后端消息或 401 默认文案
- **AND** 系统继续执行现有登录页跳转逻辑

### Requirement: 所有 Axios 客户端行为一致且兼容 SSR

系统 SHALL 让 `apiClient` 和 `publicClient` 使用相同的错误消息解析与提示规则，并 MUST 仅在浏览器环境调用 Ant Design 消息 API。

#### Scenario: 公开客户端请求失败

- **WHEN** `publicClient` 收到无有效后端消息的 HTTP 404
- **THEN** 系统使用与 `apiClient` 相同的 404 默认提示

#### Scenario: 服务端渲染期间请求失败

- **WHEN** 请求错误处理在非浏览器环境执行
- **THEN** 系统不调用浏览器 `message.error()`
- **AND** 请求错误仍按原控制流传播

### Requirement: 页面不得重复提示 API 错误

系统 SHALL 将 Axios API 错误的用户提示职责集中在响应拦截器，页面、Hooks 和 Store MUST NOT 对同一请求错误再次调用消息 API；本地校验和非 Axios 请求错误 SHALL 保留其原有提示职责。

#### Scenario: 页面捕获已提示的 API 错误

- **WHEN** 页面 catch 到拦截器已经提示过的 Axios 错误
- **THEN** 页面仅执行必要的加载状态恢复、流程中止或错误状态保存
- **AND** 页面不再次弹出该 API 错误

#### Scenario: 本地表单校验失败

- **WHEN** 用户在提交前输入不完整或两次密码不一致
- **THEN** 页面继续显示对应的本地校验提示
- **AND** 该行为不经过 Axios 错误拦截器
