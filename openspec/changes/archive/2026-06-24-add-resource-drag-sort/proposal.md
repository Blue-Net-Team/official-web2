## Why

管理后台「软件资源管理」(`/admin/resources`) 目前只能通过编辑表单里手填 `sortOrder` 数字来调整顺序，操作繁琐且不直观。竞赛管理 (`/admin/competition`) 已经用 `@dnd-kit` 实现了表格行拖拽排序，体验更好。资源管理应对齐同样的交互。

## What Changes

- 资源管理表格支持**行拖拽排序**：管理员拖动行后，前端乐观重排并调用后端批量排序接口持久化新顺序，失败回滚。
- 新增后端**批量排序接口** `PUT /api/v1/admin/software-resources/sort`，接收 `{ items: [{ id, sortOrder }] }`，逐条更新 `sort_order`。
- 新增全局唯一权限 `software-resource:sort`（显示名「调整软件资源排序」，`AccessLevel.PROTECTED`）。
- 前端资源页表格由 Ant Design 内置分页改为竞赛页同款 `DndContext` + `SortableContext` + 独立 `<Pagination>` 模式，新增拖拽手柄列；拖拽列与排序接口仅对 `isAdmin` 开放。
- 不实现上/下移按钮（竞赛页的 `move` 能力），仅做拖拽，保持最小范围。

## Capabilities

### New Capabilities
<!-- 无新增独立 capability -->

### Modified Capabilities
- `software-resource-library`: 在既有「Admin resource management」能力上新增「通过拖拽进行批量排序」的行为与对应的批量排序接口/权限要求。原「Member updates sort order」(表单手填) 行为保留。

## Impact

- **后端**（`src/backend`，对照竞赛现成实现镜像）：
  - `SoftwareResourceCommands`：新增 `SortItemCommand`、`BatchUpdateSortOrderCommand`
  - `SoftwareResourceRepository` 接口 + `SoftwareResourceRepositoryImpl`：新增 `existsById`、`batchUpdateSortOrder`、`SortItem` record
  - `SoftwareResourceMapper` 接口 + XML：新增 `updateSortOrderById`
  - `SoftwareResourceAppService` 接口 + Impl：新增 `batchUpdateSortOrder`
  - 新增 DTO `api/dto/softwareresource/BatchSortRequestDTO`
  - `AdminSoftwareResourceController`：新增 `PUT /sort` 端点 + 权限 `software-resource:sort`
  - 测试：Repository / AppService 单元测试
- **前端**（`src/frontend`）：
  - `src/app/admin/resources/page.tsx`：引入 `@dnd-kit`（已是依赖）、`DraggableRow`、乐观列表、`handleDragEnd`、拖拽手柄列
  - `src/apis/services/admin-software-resource.service.ts`：新增 `batchUpdateSortOrder`（复用现有 `BatchSortRequestDTO` 类型）
- **数据库**：无迁移（`sort_order` 字段、实体、DO、resultMap 均已存在）。
- **依赖**：无新增（`@dnd-kit/*` 已在 `package.json`）。
- **破坏性变更**：无。
