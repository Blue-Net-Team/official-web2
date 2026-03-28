## ADDED Requirements

### Requirement: Springdoc-OpenAPI 依赖与配置

系统 SHALL 引入 springdoc-openapi 与 Spring Boot 3.x 兼容的依赖，并 SHALL 通过配置文件提供文档端点开关、路径及 API 元信息（标题、版本、描述）。

#### Scenario: 依赖引入后构建通过

- **WHEN** 在项目中添加 springdoc-openapi 的 Maven 依赖并执行构建
- **THEN** 构建成功且无与 springdoc 相关的版本冲突

#### Scenario: 配置生效后文档端点可访问

- **WHEN** 应用启动且 springdoc 配置启用文档与 Swagger UI
- **THEN** `/v3/api-docs` 返回 OpenAPI 3.0 JSON，Swagger UI 页面可访问并展示 API 列表

---

### Requirement: 暴露 OpenAPI 与 Swagger UI 端点

系统 SHALL 暴露 OpenAPI 3.0 文档端点（如 `/v3/api-docs`），并 SHALL 提供 Swagger UI 页面供浏览器查看与调试接口。

#### Scenario: 获取 OpenAPI JSON

- **WHEN** 客户端请求配置的 OpenAPI 文档路径（如 GET /v3/api-docs）
- **THEN** 返回符合 OpenAPI 3.0 规范的 JSON，包含已扫描到的路径与 schema

#### Scenario: 打开 Swagger UI

- **WHEN** 用户访问配置的 Swagger UI 路径（如 /swagger-ui.html 或 /swagger-ui/index.html）
- **THEN** 页面加载并展示当前可用的 API 分组与接口列表

---

### Requirement: 现有接口在文档中可发现且带说明

系统 SHALL 为现有 REST Controller 及其接口方法补充文档化注解，使生成的 OpenAPI 文档中包含接口说明、HTTP 方法、路径、请求/响应模型及必要参数说明。

#### Scenario: 认证相关接口出现在文档中

- **WHEN** 打开 Swagger UI 或查看 OpenAPI 描述
- **THEN** 存在与 /api/v1/auth 下接口（如学号登录、登出）对应的 path 与 operation，且带有可读的 summary/description

#### Scenario: 用户信息接口出现在文档中

- **WHEN** 打开 Swagger UI 或查看 OpenAPI 描述
- **THEN** 存在与 /v1/user/info 下接口（如获取当前用户信息）对应的 path 与 operation，且带有可读的 summary/description

#### Scenario: 请求与响应模型在文档中有 schema

- **WHEN** 查看任一已文档化接口的请求体或响应体
- **THEN** 文档中展示的 requestBody/response 与现有 DTO（如 StudentIdLoginRequestDTO、UserAuthResponseDTO、UserInfo）对应，且关键字段有描述或示例

---

### Requirement: API 元信息与可选安全方案声明

系统 SHALL 在 OpenAPI 文档中提供 API 级别的 title、version 与 description；对于需要 Bearer JWT 的接口，SHALL 在 OpenAPI 中声明相应的安全方案，以便在 Swagger UI 中支持“Authorize”后发起请求。

#### Scenario: 文档头部显示 API 信息

- **WHEN** 打开 Swagger UI 或查看 OpenAPI info 节点
- **THEN** 包含 title、version 及简短 description（如“Bluenet 后端 API”）

#### Scenario: Bearer JWT 安全方案可配置

- **WHEN** 在 OpenAPI 定义中配置了 Bearer JWT 的 securityScheme 并应用到需认证的接口
- **THEN** Swagger UI 提供 Authorize 入口，用户可输入 token 后调用需认证的接口
