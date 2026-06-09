## ADDED Requirements

### Requirement: Controller 包结构规范
所有 REST Controller 类 MUST 按领域/聚合根放置在 `com.bluenet.web.api.controller.v1` 下的子包中。`v1` 根包禁止直接存放任何 Controller 文件。

#### Scenario: 按领域分包的 Controller
- **WHEN** 开发者创建新的 Controller 类
- **THEN** 该类必须放置在 `v1/{domain}/` 子包中，其中 `{domain}` 为该 Controller 所属的领域或聚合根名称

#### Scenario: Admin Controller 集中管理
- **WHEN** 开发者创建管理端 Controller 类
- **THEN** 该类必须放置在 `v1/admin/` 包下，不得与其他业务 Controller 混放在同一领域子包中

### Requirement: Controller 类命名规范
所有管理端 Controller 类名 MUST 统一使用 `AdminXxxController` 格式。普通业务 Controller MUST 使用 `XxxController` 格式。

#### Scenario: Admin Controller 命名
- **WHEN** 开发者创建管理端 Controller 类
- **THEN** 类名必须以 `Admin` 开头、以 `Controller` 结尾，如 `AdminUserController`

#### Scenario: 普通 Controller 命名
- **WHEN** 开发者创建普通业务 Controller 类
- **THEN** 类名不得包含 `Admin` 前缀，格式为 `XxxController`，如 `UserController`

### Requirement: Controller 异常处理规范
Controller 层方法 MUST NOT 自行 try-catch 并返回 `ResponseMessage.error()`。所有业务异常 MUST 统一交由 `@ControllerAdvice` 全局异常处理器处理。仅允许在需要返回非 200 HTTP 状态码的场景（如登录接口返回 401）中使用 try-catch。

#### Scenario: 统一异常处理
- **WHEN** Controller 方法调用应用服务时发生业务异常
- **THEN** 异常应直接向上抛出，由 `GlobalExceptionHandler` 统一转换为标准响应格式

#### Scenario: 特殊 HTTP 状态码
- **WHEN** 接口需要返回非 200 HTTP 状态码（如 401 Unauthorized）
- **THEN** Controller 层可以使用 try-catch 并返回 `ResponseEntity` 以设置特定状态码
