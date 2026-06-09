## Why

当前 Controller 层的包结构、类命名和异常处理策略缺乏统一规范，导致同一项目中出现多种组织方式：部分 Controller 直接放在 `v1` 根包下，部分按领域分子包；Admin Controller 命名风格不一致（`AdminXxx`、`XxxAdmin`、`无Admin前缀` 并存）；异常处理逻辑分散在 Controller 和全局处理器两处。这些不一致增加了代码维护成本和新成员的理解负担。

## What Changes

- **统一包结构**：所有 Controller 按领域/聚合根放入 `v1/` 下的子包中，`v1` 根包不再直接放置任何 Controller 文件。
- **统一 Admin Controller 命名**：所有管理端 Controller 类名统一为 `AdminXxxController` 格式，并集中放置在 `v1/admin/` 包下。
- **迁移位置异常的 Admin Controller**：将 `enrollment/AdminEnrollController` 迁移至 `admin/AdminEnrollController`。
- **统一异常处理策略**：Controller 层不再自行 try-catch 返回错误响应，统一交由 `@ControllerAdvice`（`GlobalExceptionHandler`）处理；仅保留需要返回特殊 HTTP 状态码（如登录 401）的少数场景。
- **更新开发手册**：在《后端开发手册》中补充 Controller 层包结构和命名规范章节。

## Capabilities

### New Capabilities
- `controller-layer-conventions`: Controller 层包结构、命名和异常处理规范定义。

### Modified Capabilities
- 无。本次变更为纯代码结构重构，不修改任何业务需求或接口行为。

## Impact

- **后端代码**：涉及约 30+ 个 Controller 文件的包路径调整、4 个 Admin Controller 类名重命名、部分 Controller 中冗余 try-catch 的移除。
- **集成测试**：测试类中的 import 路径需要同步更新。
- **Swagger 文档**：由于 URL 路径和接口签名不变，Swagger 文档不受影响。
- **前端**：接口 URL 不变，前端无感知。
