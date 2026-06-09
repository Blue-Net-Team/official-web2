## Context

当前 Controller 层存在以下结构问题：

1. **包结构不一致**：`v1` 根包下直接放置了 12 个 Controller（`AuthController`、`EquipmentController`、`AssessmentAnswerController` 等），同时部分 Controller 按领域放在子包中（`user/`、`achievement/`、`file/` 等），缺乏统一规则。
2. **Admin Controller 命名不统一**：`admin/` 包下 21 个 Controller 中，4 个未以 `Admin` 开头（`AuditStatisticsController`、`KnowledgeDocController`、`PermissionAdminController`、`RolePermissionAdminController`），命名风格混乱。
3. **Admin Controller 位置异常**：`AdminEnrollController` 放在 `enrollment/` 子包下，与其他 Admin Controller 的 `admin/` 包位置不一致。
4. **异常处理策略不统一**：部分 Controller 方法内使用 try-catch 返回 `ResponseMessage.error()`，部分直接抛出异常交由 `GlobalExceptionHandler` 处理。

这些问题导致新成员难以判断 Controller 应放置的位置和命名方式，增加了维护成本。

## Goals / Non-Goals

**Goals:**
- 统一 Controller 包结构规则：所有 Controller 必须按领域放入 `v1/{domain}/` 子包
- 统一 Admin Controller 命名：`AdminXxxController`
- 统一 Admin Controller 位置：全部集中在 `v1/admin/` 包下
- 统一异常处理：Controller 层不自行 try-catch，统一交由全局异常处理器
- 更新《后端开发手册》中的 Controller 层规范章节

**Non-Goals:**
- 不修改任何接口的 URL 路径
- 不修改任何接口的请求/响应参数
- 不修改 Swagger 注解的组织方式
- 不引入新的依赖或框架
- 不修改全局异常处理器的逻辑

## Decisions

### Decision 1: v1 根包下不再放置任何 Controller 文件

**选择**：所有 Controller 统一按领域/聚合根放入子包。

**理由**：
- 消除"什么时候放根下、什么时候放子包"的决策成本
- 与 converter、dto 等层的分包方式保持一致
- 避免根包随着功能增长而膨胀

**替代方案**：仅在 Controller 数量 ≥ 2 时才创建子包。拒绝原因：规则复杂，单聚合后期扩展时可能需要再迁移。

### Decision 2: Admin Controller 统一放在 `v1/admin/` 包下，不再细分

**选择**：`admin/` 包下保持扁平结构，不按领域再分包。

**理由**：
- 当前 21 个 Controller 在单包内完全可管理
- URL 路径和包结构保持一致：`/api/v1/admin/xxx` → `v1/admin/AdminXxxController`
- 避免归类争议（如 `AdminQrcodeController` 算内容还是系统？）
- 如果未来增长到 40+，届时领域边界更清晰，再拆分也不迟

**替代方案**：按 `assessment/`、`user/`、`content/` 等领域再分包。拒绝原因：增加包层级但 URL 不跟着变，物理结构和路由不一致；当前数量下收益不明显。

### Decision 3: Controller 层异常统一上抛

**选择**：Controller 方法内不自行 try-catch，统一交由 `@ControllerAdvice` 处理。

**理由**：
- `GlobalExceptionHandler` 已覆盖 `BadRequest`、`Unauthorized`、`Forbidden`、`DataNotFound`、`DataConflict` 等所有业务异常
- 减少大量重复的 try-catch 样板代码
- 异常处理逻辑集中，便于统一维护（如日志格式、错误码映射）

**例外场景**：需要返回特定 HTTP 状态码的接口（如登录失败返回 401）可以继续使用 `ResponseEntity` + try-catch。

## Risks / Trade-offs

| 风险 | 缓解措施 |
|------|----------|
| 文件移动导致 Git 历史断裂（`git blame` 追踪困难） | 这是文件重命名的固有风险，使用 `git mv` 可部分缓解；代码内容不变，不影响功能 |
| 测试类 import 路径批量更新可能遗漏 | 全局搜索替换后执行完整测试套件验证 |
| IDE 自动重构可能误改其他文件 | 分批次重构，每批执行编译和测试验证 |
| 异常处理策略变更后，某些边缘 case 行为变化 | 移除 try-catch 前先确认 `GlobalExceptionHandler` 已覆盖对应异常类型；通过集成测试验证 |

## Migration Plan

1. **批次一：包迁移** — 将 v1 根包下的 12 个 Controller 按领域迁移至子包
2. **批次二：Admin 重命名和迁移** — 重命名 4 个命名不规范的 Admin Controller，迁移 `AdminEnrollController` 至 `admin/` 包
3. **批次三：异常处理清理** — 移除 Controller 中冗余的 try-catch（需配合 `GlobalExceptionHandler` 的覆盖范围确认）
4. **批次四：测试同步** — 更新所有测试类中的 import 路径
5. **批次五：文档更新** — 更新《后端开发手册》Controller 层规范章节

Rollback：所有变更均为文件移动和代码删除，若出现问题可通过 Git 回滚。
