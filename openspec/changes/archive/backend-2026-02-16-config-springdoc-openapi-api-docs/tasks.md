## 1. 依赖与基础配置

- [x] 1.1 在 pom.xml 中添加 springdoc-openapi-starter-webmvc-ui 依赖（与 Spring Boot 3.x 兼容版本）
- [x] 1.2 在 application.yml（或对应 profile）中配置 springdoc：api-docs 与 swagger-ui 的启用开关及路径
- [x] 1.3 配置 API 元信息：title、version、description（如 Bluenet 后端 API）

## 2. OpenAPI 定义与安全方案（可选）

- [x] 2.1 如需统一 info 或安全方案，新增配置类或 @Bean 定义 OpenAPI，并声明 Bearer JWT 的 securityScheme
- [x] 2.2 对需认证的接口在文档层面应用 security（便于 Swagger UI Authorize）

## 3. 为认证接口补充文档注解

- [x] 3.1 为 AuthController 添加 @Tag，为学号登录、登出等接口添加 @Operation 与必要 @Parameter/@RequestBody 说明
- [x] 3.2 为认证相关 DTO（如 StudentIdLoginRequestDTO、UserAuthResponseDTO）添加 @Schema，补充字段说明或示例

## 4. 为用户信息接口补充文档注解

- [x] 4.1 为 UserInfoController 添加 @Tag，为获取当前用户信息接口添加 @Operation 与响应说明
- [x] 4.2 为用户信息 DTO（如 UserInfo）添加 @Schema，保证文档中响应模型可读

## 5. 验证与收尾

- [x] 5.1 启动应用，确认 /v3/api-docs 返回合法 OpenAPI JSON
- [x] 5.2 确认 Swagger UI 可打开且已文档化接口展示正确、请求/响应 schema 与实现一致
