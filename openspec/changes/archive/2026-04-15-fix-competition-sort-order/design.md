## Context

当前竞赛排序存在三个问题：
1. SQL 使用 `ORDER BY sort_order DESC`，导致 sortOrder 数值越大越靠前，与直觉相反
2. 新建竞赛固定 `sortOrder=0`，在 DESC 排序下永远在末尾
3. 拖拽排序只更新单个项的 sortOrder，容易产生重复值

管理页面使用分页查询（PAGE_SIZE=20），需要同时支持页内拖拽和跨页排序调整。竞赛总量通常在几十条级别。

## Goals / Non-Goals

**Goals:**
- sortOrder 语义明确：数值越小越靠前，sortOrder=N 即第 N 个
- 新建竞赛自动追加到末尾
- 页内拖拽排序后批量更新所有受影响项的 sortOrder
- 上移/下移按钮支持跨页微调（与全局相邻项交换 sortOrder）
- 保持管理页分页结构不变

**Non-Goals:**
- 不支持跨页拖拽（技术上复杂且不需要）
- 不改变公开页面的分页逻辑
- 不做 sortOrder 的去重/修复历史数据的迁移脚本

## Decisions

### D1: 排序方向改为 ASC

`ORDER BY c.sort_order DESC, c.id DESC` → `ORDER BY c.sort_order ASC, c.id ASC`

**理由**：sortOrder=1 表示第 1 个，语义直觉。所有主流 CMS 和管理后台都采用此约定。

**备选**：保持 DESC 但反转前端显示顺序 — 增加心智负担，不采纳。

### D2: 新建竞赛的 sortOrder 自动递增

后端在创建时查询 `MAX(sort_order) + 1`，若无记录则从 1 开始。

**理由**：自动追加到末尾是最符合预期的行为，无需用户手动填写。

### D3: 批量排序接口用于页内拖拽

- 新增 `PUT /admin/competitions/sort`，接收 `[{id, sortOrder}]` 数组
- 移除 `PUT /admin/competitions/{id}/sort` 旧接口

**理由**：页内拖拽后需要一次性更新多个项的 sortOrder，逐条调用有部分失败风险且效率低。

### D4: 上移/下移接口用于跨页微调

- 新增 `PUT /admin/competitions/{id}/move?direction=up|down`
- 后端逻辑：找到 sortOrder 相邻的项，交换两者的 sortOrder
  - 上移：找到 `sortOrder < 当前值` 的最大一项，交换
  - 下移：找到 `sortOrder > 当前值` 的最小一项，交换
- 边界情况：已在最前/最后时返回错误提示

**理由**：上移/下移只影响两个项，操作轻量。由于按全局 sortOrder 查找相邻项，天然支持跨页（如第 2 页第一个点"上移"，会与第 1 页最后一个交换）。

**备选**：手动输入排序号 — 操作不够直观，且需要额外 UI。上移/下移按钮更简洁。

### D5: 拖拽后按页内位置重新编号

拖拽结束后，前端按当前页内新顺序计算 sortOrder：
- 基准值 = `(currentPage - 1) * PAGE_SIZE`
- 每项 sortOrder = 基准值 + 页内索引 + 1

然后批量提交当前页所有项的新 sortOrder。

**理由**：每次拖拽后重新编号整个页面，避免 sortOrder 间隙和重复。

### D6: 前端每行添加上移/下移按钮

在管理页表格每行添加上移/下移图标按钮（箭头），按钮在首行禁用上移、末行禁用下移。点击后调用 move 接口并刷新列表。

## Risks / Trade-offs

- **[批量更新时并发冲突]** → 拖拽操作由管理员单人执行，并发风险极低，不加锁
- **[移除单条排序接口]** → **BREAKING**，前端已同步修改，无外部消费者
- **[上移/下移跨页后列表刷新]** → 操作后需刷新当前页数据，可能看到项目"消失"到其他页。这是预期行为
