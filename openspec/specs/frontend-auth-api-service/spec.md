### Requirement: API 配置统一管理

系统 SHALL 提供统一的 API 配置模块，从环境变量读取后端地址配置并拼接完整的 API 基础地址。

#### Scenario: 读取环境变量配置
- **WHEN** 应用启动时
- **THEN** 系统从 `.env` 文件读取 `BACKEND_HOST`、`BACKEND_PORT`、`SSL_ENABLED`、`API_PREFIX` 配置项

#### Scenario: 拼接 HTTP 地址
- **WHEN** `SSL_ENABLED` 为 `false`
- **THEN** API 基础地址格式为 `http://{BACKEND_HOST}:{BACKEND_PORT}{API_PREFIX}`

#### Scenario: 拼接 HTTPS 地址
- **WHEN** `SSL_ENABLED` 为 `true`
- **THEN** API 基础地址格式为 `https://{BACKEND_HOST}:{BACKEND_PORT}{API_PREFIX}`

### Requirement: 认证类型定义

系统 SHALL 提供与后端 DTO 一致的 TypeScript 类型定义，包括枚举类型和接口类型。

#### Scenario: 枚举类型定义
- **WHEN** 定义用户角色、方向、性别等枚举
- **THEN** 类型值 SHALL 与后端 Java 枚举的序列化值完全一致

#### Scenario: 响应类型定义
- **WHEN** 定义 `ResponseMessage<T>` 类型
- **THEN** 类型结构 SHALL 包含 `code`（number）、`msg`（string）、`data`（T | null）三个字段

#### Scenario: 登录请求类型定义
- **WHEN** 定义 `StudentIdLoginRequestDTO` 类型
- **THEN** 类型结构 SHALL 包含 `studentId`（string）、`password`（string）两个字段

#### Scenario: 登录响应类型定义
- **WHEN** 定义 `UserAuthResponseDTO` 类型
- **THEN** 类型结构 SHALL 包含 `token`（string）、`userInfo`（UserInfo）两个字段

### Requirement: 学号登录接口

系统 SHALL 提供学号登录 API 调用函数，该函数为公开接口，无需认证头。

#### Scenario: 登录成功
- **WHEN** 调用登录函数并传入正确的学号和密码
- **THEN** 函数 SHALL 发送 POST 请求到 `/auth/login/student-id`
- **THEN** 请求 SHALL 不携带 Authorization 头
- **THEN** 返回值类型 SHALL 为 `ResponseMessage<UserAuthResponseDTO>`
- **THEN** 响应 code 为 200 时，data 包含 token 和 userInfo

#### Scenario: 登录失败
- **WHEN** 调用登录函数并传入错误的学号或密码
- **THEN** 返回值 code SHALL 为 401
- **THEN** msg SHALL 包含错误原因描述

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

### Requirement: 用户退出接口

系统 SHALL 提供用户退出 API 调用函数，该函数需要认证头。

#### Scenario: 退出成功
- **WHEN** 调用退出函数且用户已登录
- **THEN** 函数 SHALL 发送 POST 请求到 `/auth/logout`
- **THEN** 请求 SHALL 自动携带 Authorization: Bearer <token> 头
- **THEN** 返回值类型 SHALL 为 `ResponseMessage<void>`

#### Scenario: 未认证退出
- **WHEN** 调用退出函数但用户未登录或 token 无效
- **THEN** 响应 code SHALL 为 401
- **THEN** 响应拦截器 SHALL 清除本地 token 并可跳转登录页

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

### Requirement: 服务函数认证标注

每个 API 服务函数 SHALL 明确注释是否需要认证头，并使用对应的 axios 客户端实例。

#### Scenario: 公开接口使用 publicClient
- **WHEN** 函数为公开接口（无需认证）
- **THEN** 函数 SHALL 使用 `publicClient` 实例发送请求
- **THEN** 函数注释 SHALL 标注"公开接口，无需认证头"

#### Scenario: 认证接口使用 apiClient
- **WHEN** 函数需要认证
- **THEN** 函数 SHALL 使用 `apiClient` 实例发送请求
- **THEN** 函数注释 SHALL 标注"需要认证头"
