## Why

后端目前没有统一的 OpenAPI/Swagger 文档，接口契约、请求/响应模型和说明分散在代码或口头约定中，不利于前后端协作、联调和对外对接。引入 springdoc-openapi 可基于现有 Spring MVC 接口自动生成 OpenAPI 3 文档，并支持在代码中补充描述与示例，提升可维护性与可发现性。

## What Changes

- 在项目中引入并配置 springdoc-openapi（与 Spring Boot 3.x 兼容的版本）。
- 配置 Swagger UI 与 OpenAPI JSON/YAML 端点（路径、是否启用等）。
- 为现有 REST 接口补充必要的注解与说明：`@Tag`、`@Operation`、`@Parameter`、请求/响应 schema 等，使文档可读、可用。
- 可选：统一 API 信息（标题、版本、描述）、分组或按模块展示。

## Capabilities

### New Capabilities

- `api-documentation`: 基于 springdoc-openapi 的 API 文档能力——依赖与配置、暴露的端点、以及为现有接口编写/补充文档的规范与范围。

### Modified Capabilities

- 无。不修改现有 spec 的“需求行为”，仅增加文档化实现。

## Impact

- **依赖**：新增 `springdoc-openapi` 相关 Maven 依赖。
- **配置**：新增或修改 `application*.yml` / 配置类，定义文档路径、开关、API 元信息等。
- **代码**：现有 Controller（及部分 DTO）需增加文档用注解，不改变接口 URL、方法签名或业务逻辑。
- **访问**：提供 Swagger UI 与机器可读的 OpenAPI 描述，可能需考虑安全（如仅内网或需登录）。
