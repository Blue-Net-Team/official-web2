## Context

竞赛管理页 (`/admin/competition`) 已用 `@dnd-kit` 实现表格行拖拽排序，后端配套有完整的 `PUT /sort` 批量排序链路（Command → AppService → Repository → Mapper `updateSortOrderById`）。软件资源管理页 (`/admin/resources`) 当前只能在编辑弹窗手填 `sortOrder`，后端缺少任何排序专用接口。本变更将竞赛侧的现成实现镜像到资源侧。

现状约束：
- `tb_software_resource` 已有 `sort_order` 列，实体/DO/resultMap 齐备，列表查询 `selectAllForAdmin` 已是 `ORDER BY sort_order ASC, id ASC`。
- 前端 `@dnd-kit/*` 已是依赖；`BatchSortRequestDTO`/`BatchSortItemDTO` 前端类型已存在（竞赛复用）。
- 项目 DDD 分层与返回类型约定（见 CLAUDE.md / layer-contracts）必须遵守；REST 接口需 `@RequiresPermission` 且 `value` 全局唯一。

## Goals / Non-Goals

**Goals:**
- 资源管理页支持行拖拽排序，乐观更新 + 失败回滚，交互与竞赛页一致。
- 后端提供 `PUT /api/v1/admin/software-resources/sort` 批量排序接口，逐条更新 `sort_order`。
- 新增全局唯一权限 `software-resource:sort`。
- 后端按 TDD 补单元测试。

**Non-Goals:**
- 不实现上/下移按钮（竞赛页的 `move`/`findAdjacent`/`findMaxSortOrder` 一整套）。
- 不支持跨页拖拽（与竞赛页一致，仅在当前页内重排）。
- 不改动公共 `/resources` 页面与查询逻辑（排序变化自动透传）。
- 不引入新依赖、不做 DB 迁移。

## Decisions

### 决策 1：镜像竞赛的批量排序链路，而非复用竞赛代码
逐条更新 `sort_order`（`sortItems.forEach(updateSortOrderById)`），而非单条 SQL 批量更新。
- **理由**：与竞赛实现完全一致，降低认知与维护成本；当前页最多 20 条，循环 update 的开销可忽略。
- **替代方案**：用 `CASE WHEN` 单条 SQL 批量更新 → 性能略好但偏离既有模式，收益不足，放弃。

### 决策 2：新建资源专属 `BatchSortRequestDTO`，不跨包复用竞赛的
在 `api/dto/softwareresource/` 下新建结构相同的 DTO。
- **理由**：竞赛的 DTO 位于 `api/dto/competition` 包，`@Schema` 文案也是「竞赛」；跨模块复用会造成包依赖混乱与语义错位。
- **替代方案**：抽取公共 `common` 包 DTO → 当前仅两处使用，过早抽象，放弃。

### 决策 3：存在性校验放在 AppService 层
`batchUpdateSortOrder` 在更新前对每个 id 调 `existsById` 校验，不存在则抛 `IllegalArgumentException`。
- **理由**：与竞赛 `CompetitionAppServiceImpl.batchUpdateSortOrder` 一致；保证「未知 id 不修改任何 sort_order」（整个方法 `@Transactional`，任一校验失败整体回滚）。

### 决策 4：前端表格切换为竞赛页同款结构
由 Ant Design 内置 `pagination` 改为外层 `DndContext` + `SortableContext` 包裹 Table（`pagination={false}`）+ 独立 `<Pagination>`，新增 `displayList` 乐观状态与拖拽手柄列。
- **理由**：`@dnd-kit` 需要稳定有序的行集合与受控的分页；这是竞赛页已验证的模式。
- **权限**：拖拽手柄列与 `handleDragEnd` 仅在 `isAdmin` 时启用，与现有「MEMBER 只读」逻辑一致。

## Risks / Trade-offs

- **跨页拖拽不可用** → 与竞赛页行为一致，符合预期；如后续需要再单独提变更。
- **乐观更新与后端不一致**（请求失败窗口）→ `handleDragEnd` 失败时回滚 `displayList` 到原 `data` 并提示错误。
- **权限 `value` 重复导致启动失败** → 已确认现有资源权限为 `software-resource:admin-list/create/update/delete`，`software-resource:sort` 无冲突；禁止改动 `PermissionScanner`。
- **`sortOrder` 基准与竞赛不同**：资源创建时用 `sortOrder ?? 0`（竞赛用 max+1），存量数据可能存在重复/0 值。批量排序按当前页 `currentPage * pageSize + index + 1` 重算，会规整当前页顺序，不影响功能。

## Migration Plan

无数据库迁移。部署顺序：后端先发布（提供 `/sort` 接口）再发布前端即可；前端调用新接口前后端须已上线。回滚：前端回退即恢复旧表单排序，后端新增接口与权限可保留无副作用。

## Open Questions

无。
