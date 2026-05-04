## Context

- 项目为 Spring Boot 3.x（Java 21），现有 REST 接口集中在 `AuthController`（/api/v1/auth）、`UserInfoController`（/v1/user/info）等，统一使用 `ApiResponse<T>` 包装与 `@RequiresPermission` 做权限控制。
- 当前无 OpenAPI/Swagger 文档，接口契约靠代码与口头约定，需要可浏览的文档与机器可读的 OpenAPI 描述。

## Goals / Non-Goals

**Goals:**

- 引入 springdoc-openapi（与 Spring Boot 3 兼容），提供 Swagger UI 与 OpenAPI 3.0 JSON/YAML 端点。
- 通过配置与注解为现有接口生成清晰、可用的文档（接口说明、请求/响应模型、必要参数与安全要求）。
- 文档与现有接口行为一致，不改变 URL、请求体或业务逻辑。

**Non-Goals:**

- 不替换或重写现有 Controller 的业务实现；不引入新的 API 版本或路径。
- 不在此变更中实现文档端点的访问控制（可后续在安全层统一配置）。

## Decisions

1. **选用 springdoc-openapi 而非 springfox**
   - springdoc 原生支持 OpenAPI 3、Spring Boot 3 与 Jakarta 命名空间，维护活跃。springfox 对 Boot 3 支持不完整，故选用 springdoc-openapi（如 `springdoc-openapi-starter-webmvc-ui`）。
   - 依赖范围：仅文档相关依赖，版本与当前 Boot 版本兼容。

2. **文档端点路径与开关**
   - 默认暴露：`/v3/api-docs`（JSON）、可选 `/v3/api-docs.yaml`、Swagger UI 路径如 `/swagger-ui.html` 或 `/swagger-ui/index.html`。
   - 通过 `springdoc.api-docs.enabled` / `springdoc.swagger-ui.enabled` 等配置项控制，便于在测试/生产按需关闭或限制访问。

3. **文档内容来源与注解策略**
   - 以“代码即文档”为主：在 Controller 上使用 `@Tag` 描述模块，在方法上使用 `@Operation` 描述接口，在参数/请求体上使用 `@Parameter` / `@RequestBody` 与 schema 关联。
   - 对已存在的 DTO（如 `StudentIdLoginRequestDTO`、`UserAuthResponseDTO`、`UserInfo`）尽量通过 `@Schema` 补充字段说明与示例，保证文档中请求/响应模型可读。
   - 统一响应包装：在文档中体现 `ApiResponse<T>` 结构（或通过全局 schema 解析），使“实际返回结构”与文档一致。

4. **API 元信息**
   - 在配置或 `@OpenAPIDefinition` 中设置 title、version、description（如“Bluenet 后端 API”），便于在 Swagger UI 头部展示。

5. **安全与 JWT 在文档中的体现**
   - 在 OpenAPI 中声明 Bearer JWT 安全方案（如 `securitySchemes` + `security`），便于在 Swagger UI 中“Authorize”后调用需认证的接口；不在本变更中实现实际鉴权逻辑变更。

## Risks / Trade-offs

- **风险**：文档端点暴露可能泄露接口列表与参数信息。
  **缓解**：通过配置在不需要的环境关闭或通过现有安全层限制文档路径（后续迭代）。

- **风险**：注解遗漏或与实现不一致导致文档过时。
  **缓解**：在 tasks 中明确“为现有接口补充文档”的清单，并约定后续新增接口必须带 `@Operation`/`@Tag`。

- **取舍**：先覆盖现有少量 Controller，不在此变更中为所有未来模块预置分组；若后续模块增多再考虑 `@GroupedOpenApi` 或按 tag 分组。

## Migration Plan

1. 在 `pom.xml` 中增加 springdoc-openapi 依赖；在 `application.yml`（或 profile 内）增加 `springdoc.*` 配置与 API 元信息。
2. 可选：新增配置类或 `@Bean` 仅当需要自定义 OpenAPI 实例或安全方案时使用。
3. 逐个 Controller 添加 `@Tag`、`@Operation`、`@Parameter`/`@Schema`，并补充 DTO 的 `@Schema`；验证 Swagger UI 与 `/v3/api-docs` 输出正确。
4. 无需数据迁移或停机；若在生产关闭文档端点，仅配置即可。

## Open Questions

- 文档端点是否需要在生产环境完全关闭，还是通过网关/安全仅允许内网或特定角色访问，留待与团队确认后写入配置或后续变更。
