## Why

竞赛管理页面的排序逻辑存在两个问题：SQL 使用 `sort_order DESC` 导致数字越大越靠前（与直觉相反），新建竞赛固定 `sortOrder=0` 永远排在末尾。此外，拖拽排序只更新单个项的 sortOrder，容易产生重复值和顺序错乱。需要修复为"数字越小越靠前、排序号即位置"的语义，并提供页内拖拽 + 上移/下移按钮两种排序方式。

## What Changes

- 修复 SQL 排序方向：`sort_order DESC` → `sort_order ASC`，使 sortOrder 数值越小越靠前
- 新建竞赛时自动填充 sortOrder 为当前最大值 +1（追加到末尾）
- 新增批量更新排序接口，页内拖拽后一次性更新当前页所有项的 sortOrder
- 新增上移/下移接口，支持跨页微调排序（与相邻项交换 sortOrder）
- 移除旧的单条排序接口
- 前端管理页同时支持页内拖拽排序和每行的上移/下移按钮

## Capabilities

### New Capabilities

- `competition-sort`: 竞赛排序管理，包含批量排序（页内拖拽）和单步移动（上移/下移，可跨页）两种方式

### Modified Capabilities

（无已有 spec 需要修改）

## Impact

- **后端 SQL**：`CompetitionMapper.xml` 中两条查询的 ORDER BY 方向反转
- **后端领域服务**：`CompetitionDomainServiceImpl.createCompetition` 中 sortOrder 初始值逻辑改为查询 MAX+1
- **后端 API**：新增 `PUT /admin/competitions/sort` 批量排序接口；新增 `PUT /admin/competitions/{id}/move` 上移/下移接口；移除 `PUT /admin/competitions/{id}/sort` 旧接口
- **前端管理页**：`admin/competition/page.tsx` 拖拽逻辑修复 + 每行添加上移/下移按钮
- **前端 API 服务**：`admin-competition.service.ts` 更新排序相关方法
- **数据库**：无需迁移，仅应用层逻辑变更
